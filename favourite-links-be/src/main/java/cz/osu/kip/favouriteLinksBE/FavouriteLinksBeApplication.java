package cz.osu.kip.favouriteLinksBE;

import cz.osu.kip.favouriteLinksBE.model.db.UserEntity;
import cz.osu.kip.favouriteLinksBE.services.KeycloakService;
import cz.osu.kip.favouriteLinksBE.services.UserService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Optional;

@SpringBootApplication
public class FavouriteLinksBeApplication {

  public static void main(String[] args) {
    SpringApplication.run(FavouriteLinksBeApplication.class, args);
  }

  @Bean
  public CommandLineRunner createGenericAdminIfRequired(
      @Autowired UserService userService,
      @Autowired KeycloakService keycloakService,
      @Autowired Logger logger,
      @Value("${app.generic-admin-ensure-exists}") boolean createGenericAdminIfRequired,
      @Value("${app.generic-admin-email}") String genericAdminEmail,
      @Value("${app.generic-admin-password}") String genericAdminPassword
  ) {
    return args -> {
      if (!createGenericAdminIfRequired) return;
      Optional<UserEntity> admin = userService.getUserByEmail(genericAdminEmail);
      if (admin.isPresent()) return;

      logger.info("Generic admin is required and not found, creating one.");

      String keycloakId;
      try {
        keycloakId = keycloakService.createUser(genericAdminEmail, genericAdminPassword, true);
      } catch (Exception e) {
        logger.error("Failed to create generic admin in Keycloak. Aborting.", e);
        return;
      }
      UserEntity adminUser = new UserEntity();
      adminUser.setEmail(genericAdminEmail);
      adminUser.setKeycloakId(keycloakId);
      try {
        userService.createUser(adminUser);
      } catch (Exception e) {
        logger.error("Failed to create generic admin in database. Aborting.", e);
        try {
          keycloakService.deleteUser(keycloakId);
        } catch (Exception ex) {
          logger.error("Failed to clean up generic admin in Keycloak after database failure. Manual cleanup may be required.", ex);
        }
        return;
      }

      logger.info("Generic admin has been created.");
    };
  }

}
