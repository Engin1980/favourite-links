package cz.osu.kip.favouriteLinksBE.controllers;

import cz.osu.kip.favouriteLinksBE.GlobalCleanupExtension;
import cz.osu.kip.favouriteLinksBE.model.dto.UserCreateDto;
import cz.osu.kip.favouriteLinksBE.model.dto.UserDto;
import cz.osu.kip.favouriteLinksBE.services.KeycloakService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/* Ukázka integračního testu s reálným spuštěním aplikace a voláním přes HTTP klienta
   - před spuštěním se smažou všichni uživatelé v Keycloak (GlobalCleanupExtension)
   - používá náhodný port
   - používá profil "test" (application-test.properties)
 */

@ExtendWith(GlobalCleanupExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test") // použije application-test.properties
@Tag("integration-test")
public class UserControllerIT {

  @Autowired
  private TestRestTemplate restTemplate;

  @Test
  void userSimpleLifecycle() {
    // připravíme request objekt
    String userEmail = "test@osu.cz";
    String adminEmail = "test-admin@osu.cz";
    UserCreateDto newUser = new UserCreateDto(userEmail, "test", false);
    UserCreateDto newAdmin = new UserCreateDto(adminEmail, "test", true);

    // vytvoření
    ResponseEntity<Void> response =
        restTemplate.postForEntity("/v1/users", newUser, Void.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    response = restTemplate.postForEntity("/v1/users", newAdmin, Void.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

    // získání všech běžným uživatelem
    ResponseEntity<List<UserDto>> usersResponse;
    usersResponse = restTemplate.exchange(
        "/v1/users/list",
        org.springframework.http.HttpMethod.GET,
        null,
        new org.springframework.core.ParameterizedTypeReference<List<UserDto>>() {
        });
    assertThat(usersResponse.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);

    // přihlášení admina
    HttpEntity<AuthController.LoginRequestDto> loginRequestEntity = new HttpEntity<>(new AuthController.LoginRequestDto(adminEmail, "test"));
    ResponseEntity<AuthController.LoginResponseDto> loginResponse = restTemplate.exchange(
        "/v1/auth/login",
        org.springframework.http.HttpMethod.POST,
        loginRequestEntity,
        AuthController.LoginResponseDto.class);

    // získání všech uživatelů
    HttpHeaders headers = new HttpHeaders();
    headers.setBearerAuth(loginResponse.getBody().accessToken());  // Authorization: Bearer <token>
    HttpEntity<Void> withTokenHttpEntity = new HttpEntity<>(headers);
    usersResponse = restTemplate.exchange(
        "/v1/users/list",
        org.springframework.http.HttpMethod.GET,
        withTokenHttpEntity,
        new org.springframework.core.ParameterizedTypeReference<List<UserDto>>() {
        });
    assertEquals(HttpStatus.OK, usersResponse.getStatusCode());
    assertNotNull(usersResponse.getBody());
    assertEquals(2, usersResponse.getBody().size());

    List<UserDto> users = usersResponse.getBody();
    assertThat(users.stream().filter(q -> q.email().equals(userEmail)).count() == 1).isTrue();
    assertThat(users.stream().filter(q -> q.email().equals(adminEmail)).count() == 1).isTrue();

    UserDto createdUser;
    createdUser = users.stream().filter(q -> q.email().equals(userEmail)).findFirst().orElseThrow();
    assertEquals(createdUser.email(), newUser.email());
    assertFalse(createdUser.isAdmin());

    createdUser = users.stream().filter(q -> q.email().equals(adminEmail)).findFirst().orElseThrow();
    assertEquals(createdUser.email(), newAdmin.email());
    assertTrue(createdUser.isAdmin());

    // smazání uživatele
    for (UserDto user : users) {
      response = restTemplate.exchange(
          "/v1/users/" + createdUser.id(),
          HttpMethod.DELETE,
          withTokenHttpEntity,
          Void.class);
      assertEquals(HttpStatus.OK, response.getStatusCode());
    }
  }
}