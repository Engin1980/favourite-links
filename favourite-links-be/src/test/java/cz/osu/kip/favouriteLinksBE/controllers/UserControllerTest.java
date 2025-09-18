package cz.osu.kip.favouriteLinksBE.controllers;

import cz.osu.kip.favouriteLinksBE.exceptions.EntityAlreadyExistsException;
import cz.osu.kip.favouriteLinksBE.mappers.UserMapper;
import cz.osu.kip.favouriteLinksBE.model.db.UserEntity;
import cz.osu.kip.favouriteLinksBE.model.dto.UserCreateDto;
import cz.osu.kip.favouriteLinksBE.model.dto.UserDto;
import cz.osu.kip.favouriteLinksBE.services.KeycloakService;
import cz.osu.kip.favouriteLinksBE.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

/* Ukázka mock-level testu */

@ActiveProfiles("test")
@Tag("unit-test")
public class UserControllerTest {

  @InjectMocks
  private UserController userController;

  @Mock
  private UserService userService;

  @Mock
  private KeycloakService keycloakService;

  @Mock
  private UserMapper userMapper;

  @BeforeEach
  void init() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void createUser_whenEntityAlreadyExists_thenReturnsError() {
    UserCreateDto newUser = new UserCreateDto("test@osu.cz", "test", false);

    // definice mock chování
    Mockito
        .doThrow(new EntityAlreadyExistsException(UserEntity.class, "email == 'teest@osu.cz'"))
        .when(userService).getUserByEmail(org.mockito.ArgumentMatchers.any());

    try {
      userController.createUser(newUser);
      org.junit.jupiter.api.Assertions.fail("This should throw an exception as user already exists");
    } catch (Exception ex) {
      // otestuj, že to je správná výjimka
      System.out.println("ERR: " + ex.getClass().getSimpleName());
      assertTrue(ex instanceof EntityAlreadyExistsException);
    }
  }

  @Test
  void createUser_successful() {
    UserEntity newUser = new UserEntity();
    newUser.setEmail("testSucc@osu.cz");
    newUser.setKeycloakId("abcd-keycloak-id");

    // definice mock chování
    Mockito
        .doNothing()
        .when(userService).createUser(any(UserEntity.class));
    Mockito
        .doAnswer(q -> "abcd")
        .when(keycloakService).createUser(
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.any(),
            org.mockito.ArgumentMatchers.anyBoolean());
    Mockito
        .doAnswer(q -> new UserDto(1, "a", "test@osu.cz", false))
        .when(userMapper).to(org.mockito.ArgumentMatchers.any());
    Mockito
        .doAnswer(q -> {
          UserEntity ret = new UserEntity();
          ret.setEmail("test@osu.cz");
          ret.setKeycloakId("abcd");
          return ret;
        })
        .when(userMapper).fromCreate(org.mockito.ArgumentMatchers.any());

    // samotný test
    userController.createUser(new UserCreateDto("test@osu.cz", "password", false));
  }
}