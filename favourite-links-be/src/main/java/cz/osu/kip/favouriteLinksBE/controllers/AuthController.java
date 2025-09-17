package cz.osu.kip.favouriteLinksBE.controllers;

import cz.osu.kip.favouriteLinksBE.services.KeycloakService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("v1/auth")
public class AuthController {

    private final KeycloakService keycloakService;

    public AuthController(KeycloakService keycloakService) {
        this.keycloakService = keycloakService;
    }

    @PostMapping("login")
    public LoginResponseDto login(@RequestBody LoginRequestDto data) {
        KeycloakService.LoginResult tokens = keycloakService.login(data.email, data.password);
        return new LoginResponseDto(tokens.accessToken(), tokens.refreshToken());
    }

    @PostMapping("refresh")
    public LoginResponseDto refresh(@RequestBody String refreshToken){
        KeycloakService.LoginResult tokens = keycloakService.refreshToken(refreshToken);
        return new LoginResponseDto(tokens.accessToken(), tokens.refreshToken());
    }

    public record LoginRequestDto(String email, String password) {
    }

    public record LoginResponseDto(String accessToken, String refreshToken) {
    }
}
