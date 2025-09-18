package cz.osu.kip.favouriteLinksBE.controllers;

import cz.osu.kip.favouriteLinksBE.exceptions.AppException;
import cz.osu.kip.favouriteLinksBE.mappers.UserMapper;
import cz.osu.kip.favouriteLinksBE.model.db.UserEntity;
import cz.osu.kip.favouriteLinksBE.model.dto.UserCreateDto;
import cz.osu.kip.favouriteLinksBE.model.dto.UserDto;
import cz.osu.kip.favouriteLinksBE.services.KeycloakService;
import cz.osu.kip.favouriteLinksBE.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("v1/users")
public class UserController {

  private final KeycloakService keycloakService;
  private final UserService userService;
  private final UserMapper userMapper;
  private final Logger logger = LoggerFactory.getLogger(UserController.class);

  public UserController(
      @Autowired KeycloakService keycloakService,
      @Autowired UserService userService,
      @Autowired UserMapper userMapper) {
    this.keycloakService = keycloakService;
    this.userService = userService;
    this.userMapper = userMapper;
  }

  @PostMapping
  public void createUser(@RequestBody UserCreateDto data) {
    String keycloakId = null;
    try {
      keycloakId = createKeycloakUser(data);
      createDatabaseUser(data, keycloakId);
    } catch (Exception e) {
      if (keycloakId != null)
        deleteKeycloakUser(keycloakId);
      throw new AppException("Failed to create user in database: " + e.getMessage(), e);

    }

    // TODO send email with activation link
  }

  @GetMapping("/list")
  public List<UserDto> listUsers() {
    List<UserEntity> dbUsers = userService.getAllUsers();
    List<KeycloakService.KeycloakUser> kcUsers = keycloakService.getAllUsers();

    Map<String, UserEntity> dbUsersMap = dbUsers.stream()
        .collect(java.util.stream.Collectors.toMap(UserEntity::getKeycloakId, u -> u));

    List<UserDto> userDtos = kcUsers.stream()
        .map(kcUser -> {
          UserEntity dbUser = dbUsersMap.get(kcUser.id());
          if (dbUser == null)
            throw new AppException("Failed to find Db-User for keycloak user: " + kcUser.email());
          return new UserDto(dbUser.getId(), kcUser.id(), kcUser.email(), kcUser.role() == KeycloakService.Role.ADMIN);
        }).toList();
    return userDtos;
  }

  private void deleteKeycloakUser(String keycloakId) {
    try {
      keycloakService.deleteUser(keycloakId);
    } catch (Exception e) {
      logger.error("Failed to delete keycloak-user.", e);
    }
  }

  private String createKeycloakUser(UserCreateDto data) {
    String keycloakId;
    try {
      keycloakId = keycloakService.createUser(data.email(), data.password(), data.isAdmin());
    } catch (Exception e) {
      throw new AppException(
          "Failed to create user in Keycloak: " + e.getMessage(), e);
    }
    return keycloakId;
  }

  private void createDatabaseUser(UserCreateDto data, String keycloakId) {
    UserEntity entity = userMapper.fromCreate(data);
    entity.setKeycloakId(keycloakId);
    userService.createUser(entity);
  }

  @DeleteMapping("{id}")
  public void deleteUser(@PathVariable int id) {
    Optional<UserEntity> user = userService.getUserById(id);
    if (user.isPresent()) {
      keycloakService.deleteUser(user.get().getKeycloakId());
      userService.deleteUser(id);
    }
  }
}
