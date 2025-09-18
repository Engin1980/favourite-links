package cz.osu.kip.favouriteLinksBE.security;

import cz.osu.kip.favouriteLinksBE.services.KeycloakService;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

@org.springframework.context.annotation.Configuration
public class SecurityConfig {

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable) // jen pro JWT v hlavičce, ne pro JWT v cookies!!!
        .authorizeHttpRequests(auth -> auth
            .requestMatchers(HttpMethod.POST, "/v1/auth/login").permitAll()
            .requestMatchers(HttpMethod.POST, "/v1/users", "/v1/users/").permitAll()
            .requestMatchers(HttpMethod.POST, "/v1/users/refresh").permitAll()
            .requestMatchers(HttpMethod.GET, "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
            .requestMatchers("/v1/users/list").hasAuthority(KeycloakService.Roles.ADMIN)
            .anyRequest().authenticated()
        )
        .oauth2ResourceServer(oauth2 ->
            oauth2.jwt(
                jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter())
            ));

    return http.build();
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
    authoritiesConverter.setAuthorityPrefix(""); // nechceme u rolí prefix
    authoritiesConverter.setAuthoritiesClaimName("roles"); // kde v JWT jsou role?

    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
    return converter;
  }
}