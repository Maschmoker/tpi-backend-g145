package com.guarderia.commons.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class KeycloakRealmRoleConverterTest {

    private final KeycloakRealmRoleConverter converter = new KeycloakRealmRoleConverter();

    @Test
    void convierteLosRolesDelCatalogoEnAutoridadesConPrefijo() {
        Jwt jwt = jwtConRoles("CLIENTE", "ADMIN");

        assertThat(nombres(converter.convert(jwt)))
                .containsExactlyInAnyOrder("ROLE_CLIENTE", "ROLE_ADMIN");
    }

    @Test
    void ignoraRolesDesconocidosYRolesDeInfraestructura() {
        Jwt jwt = jwtConRoles("offline_access", "uma_authorization", "default-roles-guarderia", "SUPERUSER");

        assertThat(converter.convert(jwt)).isEmpty();
    }

    @Test
    void conservaSoloLosRolesConocidosCuandoHayMezcla() {
        Jwt jwt = jwtConRoles("CHOFER", "offline_access", "ADMIN");

        assertThat(nombres(converter.convert(jwt)))
                .containsExactlyInAnyOrder("ROLE_CHOFER", "ROLE_ADMIN");
    }

    @Test
    void devuelveVacioCuandoFaltaElClaimRealmAccess() {
        Jwt jwt = Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "usuario-prueba")
                .build();

        assertThat(converter.convert(jwt)).isEmpty();
    }

    @Test
    void devuelveVacioCuandoRealmAccessNoEsUnMapa() {
        Jwt jwt = jwtConRealmAccess("no-es-un-mapa");

        assertThat(converter.convert(jwt)).isEmpty();
    }

    @Test
    void devuelveVacioCuandoRolesNoEsUnaColeccion() {
        Jwt jwt = jwtConRealmAccess(Map.of("roles", "CLIENTE"));

        assertThat(converter.convert(jwt)).isEmpty();
    }

    @Test
    void ignoraElementosNoStringYDuplicados() {
        Jwt jwt = jwtConRealmAccess(Map.of("roles", List.of("ADMIN", 5, "ADMIN", "CHOFER")));

        assertThat(nombres(converter.convert(jwt)))
                .containsExactlyInAnyOrder("ROLE_ADMIN", "ROLE_CHOFER");
    }

    @Test
    void esSensibleAMayusculasComoLosRolesDeKeycloak() {
        Jwt jwt = jwtConRoles("cliente");

        assertThat(converter.convert(jwt)).isEmpty();
    }

    @Test
    void toleraTokenNulo() {
        assertThat(converter.convert(null)).isEmpty();
    }

    @Test
    void tieneDevuelveTrueSoloParaRolesPresentesDelCatalogo() {
        Jwt jwt = jwtConRoles("PERSONAL_CUIDADO");

        assertThat(KeycloakRealmRoles.tiene(jwt, "PERSONAL_CUIDADO")).isTrue();
        assertThat(KeycloakRealmRoles.tiene(jwt, "ADMIN")).isFalse();
        assertThat(KeycloakRealmRoles.tiene(jwt, "offline_access")).isFalse();
    }

    @Test
    void seIntegraConElJwtAuthenticationConverterDeSpringSecurity() {
        JwtAuthenticationConverter conversionServlet = new JwtAuthenticationConverter();
        conversionServlet.setJwtGrantedAuthoritiesConverter(converter);

        AbstractAuthenticationToken autenticacion = conversionServlet.convert(jwtConRoles("CHOFER"));

        // Spring Security 7 agrega además una autoridad FACTOR_BEARER propia del token.
        assertThat(autenticacion.getAuthorities())
                .extracting(GrantedAuthority::getAuthority)
                .contains("ROLE_CHOFER");
    }

    private static Jwt jwtConRoles(String... roles) {
        return jwtConRealmAccess(Map.of("roles", List.of(roles)));
    }

    private static Jwt jwtConRealmAccess(Object realmAccess) {
        return Jwt.withTokenValue("token")
                .header("alg", "none")
                .claim("sub", "usuario-prueba")
                .claim(KeycloakRealmRoles.CLAIM_REALM_ACCESS, realmAccess)
                .build();
    }

    private static List<String> nombres(Collection<GrantedAuthority> autoridades) {
        return autoridades.stream().map(GrantedAuthority::getAuthority).toList();
    }
}
