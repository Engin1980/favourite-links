package cz.osu.kip.favouriteLinksBE.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNullApi;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.*;
import java.util.stream.Collectors;

public class KeycloakRealmRoleConverter implements Converter<Jwt, AbstractAuthenticationToken> {

  private final JwtAuthenticationConverter delegate = new JwtAuthenticationConverter();

  @Override
  public AbstractAuthenticationToken convert(Jwt jwt) {
    Collection<GrantedAuthority> authorities = extractRealmRoles(jwt);

    // necháme původní converter vygenerovat token
    AbstractAuthenticationToken token = delegate.convert(jwt);

    // spojíme jeho authority (např. z "scope") s našimi Keycloak rolemi
    Collection<GrantedAuthority> combined = new HashSet<>(token.getAuthorities());
    combined.addAll(authorities);

    return new org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken(
        jwt,
        combined,
        getUsername(jwt)
    );
  }

  private Collection<GrantedAuthority> extractRealmRoles(Jwt jwt) {
    List<String> roles = jwt.getClaim("roles");

    if (roles == null) {
      return Collections.emptyList();
    }

    return roles.stream()
        .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
        .collect(Collectors.toSet());
  }

  private String getUsername(Jwt jwt) {
    return jwt.getClaimAsString("preferred_username"); // můžeš změnit podle potřeby
  }
}

