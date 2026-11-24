package com.guarderia.commons.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Extrae los roles de negocio desde el claim {@code realm_access.roles} de un JWT emitido
 * por Keycloak.
 *
 * <p>Es deliberadamente independiente del stack servlet/reactivo: solo depende de
 * {@link Jwt}, por lo que puede usarse tanto en servicios MVC como en el Gateway WebFlux.</p>
 *
 * <p>El extractor aplica una lista blanca con {@link RolesGuarderia#TODOS}: roles propios
 * de Keycloak ({@code offline_access}, {@code uma_authorization},
 * {@code default-roles-guarderia}) y cualquier rol desconocido se ignoran.</p>
 */
public final class KeycloakRealmRoles {

    public static final String CLAIM_REALM_ACCESS = "realm_access";
    public static final String CLAIM_ROLES = "roles";

    private KeycloakRealmRoles() {
    }

    /**
     * Devuelve los roles del catálogo presentes en el token, sin duplicados.
     *
     * <p>Nunca lanza excepción por claims ausentes o con tipos inesperados: en esos casos
     * devuelve un conjunto vacío.</p>
     *
     * @param jwt token validado por Spring Security; puede ser {@code null}
     * @return conjunto inmutable de roles (posiblemente vacío)
     */
    public static Set<String> extraer(Jwt jwt) {
        if (jwt == null) {
            return Set.of();
        }

        Object realmAccess = jwt.getClaims().get(CLAIM_REALM_ACCESS);
        if (!(realmAccess instanceof Map<?, ?> mapaRealm)) {
            return Set.of();
        }

        Object roles = mapaRealm.get(CLAIM_ROLES);
        if (!(roles instanceof Collection<?> coleccion)) {
            return Set.of();
        }

        Set<String> resultado = new LinkedHashSet<>();
        for (Object rol : coleccion) {
            if (rol instanceof String texto && RolesGuarderia.esRolDelDominio(texto)) {
                resultado.add(texto);
            }
        }
        return Collections.unmodifiableSet(resultado);
    }

    /**
     * Indica si el token contiene el rol indicado del catálogo.
     */
    public static boolean tiene(Jwt jwt, String rol) {
        return RolesGuarderia.esRolDelDominio(rol) && extraer(jwt).contains(rol);
    }
}
