package cz.osu.kip.favouriteLinksBE;

import cz.osu.kip.favouriteLinksBE.services.KeycloakService;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;

public class GlobalCleanupExtension implements BeforeAllCallback {

  @Override
  public void beforeAll(ExtensionContext context) throws Exception {
    // získej Spring kontext
    ApplicationContext applicationContext = SpringExtension.getApplicationContext(context);
    // získej instanci služby
    KeycloakService keycloakService = applicationContext.getBean(KeycloakService.class);

    var users = keycloakService.getAllUsers();
    for (var user : users) {
      keycloakService.deleteUser(user.id());
    }
    System.out.println("✅ Vymazáni všichni uživatelé před spuštěním testů");
  }
}
