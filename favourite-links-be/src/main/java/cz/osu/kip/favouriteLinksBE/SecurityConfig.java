package cz.osu.kip.favouriteLinksBE;

import cz.osu.kip.favouriteLinksBE.security.KeycloakRealmRoleConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@org.springframework.context.annotation.Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        //.csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.POST, "/v1/auth/login").permitAll()
            .requestMatchers(HttpMethod.POST, "/v1/users/").permitAll()
            .requestMatchers(HttpMethod.POST, "/v1/users/refresh").permitAll()
            .requestMatchers("/v1/users/list").hasRole("admin")
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 -> oauth2
            .jwt(jwt -> jwt.jwtAuthenticationConverter(new KeycloakRealmRoleConverter())));

    return http.build();
  }
}