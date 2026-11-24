package com.guarderia.commons.security;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;

/**
 * Convierte los roles del claim {@code realm_access.roles} de Keycloak en autoridades
 * de Spring Security con prefijo {@code ROLE_}.
 *
 * <p>Al implementar {@link Converter}{@code <Jwt, Collection<GrantedAuthority>>} sirve
 * tanto para {@code JwtAuthenticationConverter} (servlet) como para
 * {@code ReactiveJwtAuthenticationConverter} (WebFlux):</p>
 *
 * <pre>{@code
 * var servlet = new JwtAuthenticationConverter();
 * servlet.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
 *
 * var reactivo = new ReactiveJwtAuthenticationConverter();
 * reactivo.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
 * }</pre>
 *
 * <p>No se registra como componente: cada servicio decide explícitamente cómo cablearlo.</p>
 */
public final class KeycloakRealmRoleConverter
        implements Converter<Jwt, Collection<GrantedAuthority>> {

    @Override
    public Collection<GrantedAuthority> convert(Jwt jwt) {
        return KeycloakRealmRoles.extraer(jwt).stream()
                .map(rol -> (GrantedAuthority) new SimpleGrantedAuthority(RolesGuarderia.autoridad(rol)))
                .toList();
    }
}
