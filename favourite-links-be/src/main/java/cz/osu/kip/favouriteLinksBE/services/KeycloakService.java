package cz.osu.kip.favouriteLinksBE.services;

import cz.osu.kip.favouriteLinksBE.exceptions.AppException;
import cz.osu.kip.favouriteLinksBE.exceptions.ServiceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class KeycloakService {

  public enum Role {
    ADMIN, USER
  }

  public static class SpringBootUserRoles {
    public final static String ADMIN = "ADMIN";
    public final static String USER = "USER";

  }

  public static class KeycloakUserRoles {
    public final static String KEYCLOAK_ADMIN = "be_role_admin";
    public final static String KEYCLOAK_USER = "be_role_user";
  }

  class KeycloakUtils {
    public String getAdminAccessToken() {
      Map<String, String> params = new HashMap<>();
      params.put("grant_type", CLIENT_CREDENTIALS);
      params.put("client_id", adminClientId);
      params.put("client_secret", aminClientIdSecret);

      String form = convertParamsToFormBody(params);

      URI url = otherUtils.createUri(String.format("%s/realms/%s/protocol/openid-connect/token",
          serverUrl, realm));

      HttpRequest request = HttpRequest.newBuilder()
          .uri(url)
          .header("Content-Type", "application/x-www-form-urlencoded")
          .POST(HttpRequest.BodyPublishers.ofString(form))
          .build();

      HttpResponse<String> response = getHttpResponse(request,
          e -> new AppException("Failed to get admin access token.", e));

      if (response.statusCode() != 200) {
        throw new AppException("Failed to get admin access token: " + response.body());
      }

      Map<String, String> tokenData = parseJson(response.body());
      return tokenData.get("access_token");
    }

    public String getRoleId(String roleName, String adminToken) {
      URI url = otherUtils.createUri(String.format("%s/admin/realms/%s/roles/%s",
          serverUrl, realm, roleName));

      HttpRequest request = HttpRequest.newBuilder()
          .uri(url)
          .header("Authorization", "Bearer " + adminToken)
          .GET()
          .build();

      HttpResponse<String> response = getHttpResponse(request,
          e -> new ServiceException(this, String.format("Failed to get role %s", roleName), e));

      if (response.statusCode() == 404) {
        return null;
      }

      if (response.statusCode() != 200) {
        throw new ServiceException(this,
            String.format("Getting role failed with status %d: %s",
                response.statusCode(), response.body()));
      }

      Map<String, String> roleData = parseJson(response.body());
      return roleData.get("id");
    }

    public void assignRoleToUser(String userId, Role role, String adminToken) {
      String roleString = switch (role) {
        case ADMIN -> KeycloakUserRoles.KEYCLOAK_ADMIN;
        case USER -> KeycloakUserRoles.KEYCLOAK_USER;
      };
      // Získání ID role podle názvu
      String roleId = keycloakUtils.getRoleId(roleString, adminToken);
      if (roleId == null) {
        throw new ServiceException(this, String.format("Role '%s' not found in Keycloak", roleString));
      }

      // Přiřazení role uživateli
      String roleJson = String.format("""
          [{
              "id": "%s",
              "name": "%s"
          }]
          """, roleId, roleString);

      URI url = otherUtils.createUri(String.format("%s/admin/realms/%s/users/%s/role-mappings/realm",
          serverUrl, realm, userId));

      HttpRequest request = HttpRequest.newBuilder()
          .uri(url)
          .header("Content-Type", "application/json")
          .header("Authorization", "Bearer " + adminToken)
          .POST(HttpRequest.BodyPublishers.ofString(roleJson))
          .build();

      HttpResponse<String> response = getHttpResponse(request,
          e -> new ServiceException(this, String.format("Failed to assign role to user %s", userId), e));

      if (response.statusCode() != 204) {
        throw new ServiceException(this,
            String.format("Assigning role failed with status %d: %s",
                response.statusCode(), response.body()));
      }
    }

    public void removeRoleFromUser(String userId, Role role, String adminToken) {
      String roleString = switch (role) {
        case ADMIN -> KeycloakUserRoles.KEYCLOAK_ADMIN;
        case USER -> KeycloakUserRoles.KEYCLOAK_USER;
      };

      // Získání ID role podle názvu
      String roleId = keycloakUtils.getRoleId(roleString, adminToken);
      if (roleId == null) {
        throw new ServiceException(this, String.format("Role '%s' not found in Keycloak", roleString));
      }

      // Odebrání role uživateli
      String roleJson = String.format("""
          [{
              "id": "%s",
              "name": "%s"
          }]
          """, roleId, roleString);

      URI url = otherUtils.createUri(String.format("%s/admin/realms/%s/users/%s/role-mappings/realm",
          serverUrl, realm, userId));

      HttpRequest request = HttpRequest.newBuilder()
          .uri(url)
          .header("Content-Type", "application/json")
          .header("Authorization", "Bearer " + adminToken)
          .method("DELETE", HttpRequest.BodyPublishers.ofString(roleJson))
          .build();

      HttpResponse<String> response = getHttpResponse(request,
          e -> new ServiceException(this, String.format("Failed to remove role from user %s", userId), e));

      if (response.statusCode() != 204) {
        throw new ServiceException(this,
            String.format("Removing role failed with status %d: %s",
                response.statusCode(), response.body()));
      }
    }

    public String createUser(String email, String password, String adminToken) {
      String userJson = otherUtils.createUserJsonBody(email, password);

      URI url = otherUtils.createUri(String.format("%s/admin/realms/%s/users", serverUrl, realm));

      HttpRequest request = HttpRequest.newBuilder()
          .uri(url)
          .header("Content-Type", "application/json")
          .header("Authorization", "Bearer " + adminToken)
          .POST(HttpRequest.BodyPublishers.ofString(userJson))
          .build();

      HttpResponse<String> response = getHttpResponse(request,
          e -> new ServiceException(this, String.format("Failed to create user with email: %s", email), e));
      String keycloakUserId = switch (response.statusCode()) {
        case 201 -> {
          // Úspěšně vytvořeno, získáme ID z Location header
          String location = response.headers().firstValue("Location").orElse("");
          yield location.replaceAll(".*/([^/]+)$", "$1");
        }
        case 409 -> throw new AppException(String.format("Keycloak user '%s' already exists.", email));
        default -> throw new ServiceException(this,
            String.format("Creating user in Keycloak failed with status %d: %s",
                response.statusCode(), response.body()));
      };
      return keycloakUserId;
    }

    public void deleteUser(String userId, String adminToken) {
      URI url = otherUtils.createUri(String.format("%s/admin/realms/%s/users/%s", serverUrl, realm, userId));
      HttpRequest request = HttpRequest.newBuilder()
          .uri(url)
          .header("Authorization", "Bearer " + adminToken)
          .DELETE()
          .build();

      HttpResponse<String> response = getHttpResponse(request,
          e -> new ServiceException(this, String.format("Failed to delete user with id: %s", userId), e));

      switch (response.statusCode()) {
        case 204:
          // Úspěšně smazáno
          return;
        case 404:
          // Uživatel neexistuje - ignorujeme jako v původní implementaci
          return;
        default:
          throw new ServiceException(this,
              String.format("Deleting user in Keycloak failed with status %d: %s",
                  response.statusCode(), response.body()));
      }
    }

    public void deleteUserIgnoreFail(String keycloakUserId, String adminToken) {
      try {
        deleteUser(keycloakUserId, adminToken);
      } catch (Exception ex) {
        logger.error("Failed to delete keycloak user '{}' in ignore-fail mode.", keycloakUserId);
      }
    }
  }

  static class OtherUtils {
    public String createUserJsonBody(String email, String password) {
      String userJson = String.format("""
          {
              "username": "%s",
              "email": "%s",
              "enabled": true,
              "emailVerified": true,
              "credentials": [{
                  "type": "password",
                  "value": "%s",
                  "temporary": false
              }]
          }
          """, email, email, password);
      return userJson;
    }

    public URI createUri(String url) {
      URI ret;
      try {
        ret = new URI(url);
      } catch (URISyntaxException e) {
        throw new AppException("Failed to create URL for user creation.", e);
      }
      return ret;
    }
  }

  private static final String CLIENT_CREDENTIALS = "client_credentials";
  private final static String PASSWORD = "password";
  private final String realm;
  private final String serverUrl;
  private final String adminClientId;
  private final String aminClientIdSecret;
  private final String userClientId;
  private final KeycloakUtils keycloakUtils = new KeycloakUtils();
  private final OtherUtils otherUtils = new OtherUtils();
  private final Logger logger = LoggerFactory.getLogger(this.getClass());

  public KeycloakService(@Value("${keycloak.server-url}") String serverUrl,
                         @Value("${keycloak.realm}") String realm,
                         @Value("${keycloak.admin-client-id}") String adminClientId,
                         @Value("${keycloak.admin-client-id-secret}") String aminClientIdSecret,
                         @Value("${keycloak.user-client-id}") String userClientId) {
    this.serverUrl = serverUrl;
    this.realm = realm;
    this.adminClientId = adminClientId;
    this.aminClientIdSecret = aminClientIdSecret;
    this.userClientId = userClientId;
  }

  private static Map<String, String> parseJson(String json) {
    Map<String, String> map = new HashMap<>();

    // Odstranění úvodní a koncové složené závorky a případných mezer
    String content = json.trim();
    if (content.startsWith("{")) content = content.substring(1);
    if (content.endsWith("}")) content = content.substring(0, content.length() - 1);

    // Rozdělení podle čárky na jednotlivé páry "key":"value"
    String[] pairs = content.split(",");

    for (String pair : pairs) {
      String[] kv = pair.split(":", 2);
      if (kv.length != 2) continue;

      String key = kv[0].trim();
      String value = kv[1].trim();

      // Odstranění uvozovek z klíče a hodnoty
      if (key.startsWith("\"") && key.endsWith("\"")) {
        key = key.substring(1, key.length() - 1);
      }
      if (value.startsWith("\"") && value.endsWith("\"")) {
        value = value.substring(1, value.length() - 1);
      }

      map.put(key, value);
    }

    return map;
  }

  private static HttpResponse<String> getHttpResponse(HttpRequest request, Function<Exception, AppException> exceptionHandler) {
    HttpResponse<String> response;
    try (var httpClient = HttpClient.newHttpClient()) {
      response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    } catch (Exception e) {
      if (e instanceof IOException || e instanceof InterruptedException) {
        throw exceptionHandler.apply(e);
      } else throw new AppException("Failed to invoke http request.", e);
    }
    return response;
  }

  private static String convertParamsToFormBody(Map<String, String> params) {
    String form = params.entrySet().stream()
        .map(e -> String.format("%s=%s",
            e.getKey(),
            URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8)))
        .reduce((a, b) -> a + "&" + b)
        .orElse("");
    return form;
  }

  public String createUser(String email, String password, boolean isAdmin) {
    String adminToken = keycloakUtils.getAdminAccessToken();

    String keycloakUserId;
    try {
      logger.info("Creating keycloak user '{}'", email);
      keycloakUserId = keycloakUtils.createUser(email, password, adminToken);
    } catch (Exception e) {
      logger.error("Failed to create keycloak user '{}'", email, e);
      throw e;
    }

    try {
      Role role = isAdmin ? Role.ADMIN : Role.USER;
      keycloakUtils.assignRoleToUser(keycloakUserId, role, adminToken);
    } catch (Exception e) {
      logger.error("Failed to assign role to keycloak user '{}', cleaning up.", email, e);
      keycloakUtils.deleteUserIgnoreFail(keycloakUserId, adminToken);
      throw e;
    }

    return keycloakUserId;
  }

  public void deleteUser(String userId) {
    String adminToken = keycloakUtils.getAdminAccessToken();
    keycloakUtils.deleteUser(userId, adminToken);
  }

  public LoginResult login(String email, String password) {
    Map<String, String> params = new HashMap<>();
    params.put("grant_type", PASSWORD);
    params.put("client_id", userClientId);
    params.put("username", email);
    params.put("password", password);

    String form = convertParamsToFormBody(params);

    URI uri = otherUtils.createUri(String.format("%s/realms/%s/protocol/openid-connect/token",
        serverUrl, realm));

    HttpRequest request = HttpRequest.newBuilder()
        .uri(uri)
        .header("Content-Type", "application/x-www-form-urlencoded")
        .POST(HttpRequest.BodyPublishers.ofString(form))
        .build();

    HttpResponse<String> response = getHttpResponse(request,
        e -> new ServiceException(this, String.format("Failed to login user with email: %s", email), e));

    if (response.statusCode() != 200) {
      throw new ServiceException(this,
          String.format("Login failed with status %d: %s",
              response.statusCode(), response.body()));
    }

    Map<String, String> tokenData = parseJson(response.body());
    String accessToken = tokenData.get("access_token");
    String refreshToken = tokenData.get("refresh_token");

    return new LoginResult(accessToken, refreshToken);
  }

  public LoginResult refreshToken(String refreshToken) {
    Map<String, String> params = new HashMap<>();
    params.put("refresh_token", refreshToken);
    params.put("grant_type", "refresh_token");
    params.put("client_id", this.userClientId);

    String form = convertParamsToFormBody(params);

    URI uri = otherUtils.createUri(String.format(
        "%s/realms/%s/protocol/openid-connect/token",
        this.serverUrl, this.realm));

    HttpRequest request = HttpRequest.newBuilder()
        .uri(uri)
        .header("Content-Type", "application/x-www-form-urlencoded")
        .POST(HttpRequest.BodyPublishers.ofString(form))
        .build();

    HttpResponse<String> response = getHttpResponse(request,
        e -> new AppException("Failed to send request to Keycloak for token refresh.", e));
    if (response.statusCode() != 200) {
      throw new AppException("Failed to refresh token: " + response.body());
    }

    Map<String, String> map = parseJson(response.body());

    String newAccessToken = map.get("access_token");
    String newRefreshToken = map.get("refresh_token");

    LoginResult ret = new LoginResult(newAccessToken, newRefreshToken);
    return ret;
  }

  public record LoginResult(String accessToken, String refreshToken) {
  }
}