package cz.osu.kip.favouriteLinksBE.services;

import cz.osu.kip.favouriteLinksBE.exceptions.AppException;
import cz.osu.kip.favouriteLinksBE.exceptions.ServiceException;
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
    private static final String CLIENT_CREDENTIALS = "client_credentials";
    private final static String PASSWORD = "password";
    private final String realm;
    private final String serverUrl;
    private final String adminClientId;
    private final String aminClientIdSecret;
    private final String userClientId;

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

    public String createUser(String email, String password) {
        String adminToken = getAdminAccessToken();

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

        URI url = createUri(String.format("%s/admin/realms/%s/users", serverUrl, realm));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(url)
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + adminToken)
                .POST(HttpRequest.BodyPublishers.ofString(userJson))
                .build();

        HttpResponse<String> response = getHttpResponse(request,
                e -> new ServiceException(this, String.format("Failed to create user with email: %s", email), e));
        return switch (response.statusCode()) {
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
    }

    private URI createUri(String url) {
        URI ret;
        try {
            ret = new URI(url);
        } catch (URISyntaxException e) {
            throw new AppException("Failed to create URL for user creation.", e);
        }
        return ret;
    }

    private String getAdminAccessToken() {
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", CLIENT_CREDENTIALS);
        params.put("client_id", adminClientId);
        params.put("client_secret", aminClientIdSecret);

        String form = convertParamsToFormBody(params);

        URI url = createUri(String.format("%s/realms/%s/protocol/openid-connect/token",
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

    public void deleteUser(String userId) {
        String adminToken = getAdminAccessToken();
        URI url = createUri(String.format("%s/admin/realms/%s/users/%s", serverUrl, realm, userId));
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

    public LoginResult login(String email, String password) {
        Map<String, String> params = new HashMap<>();
        params.put("grant_type", PASSWORD);
        params.put("client_id", userClientId);
        params.put("username", email);
        params.put("password", password);

        String form = convertParamsToFormBody(params);

        URI uri = createUri(String.format("%s/realms/%s/protocol/openid-connect/token",
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

        URI uri = createUri(String.format(
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