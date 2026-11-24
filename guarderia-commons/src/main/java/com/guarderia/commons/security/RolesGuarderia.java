package com.guarderia.commons.security;

import java.util.Objects;
import java.util.Set;

/**
 * Roles de negocio definidos en el realm de Keycloak de la guardería.
 *
 * <p>Los nombres coinciden exactamente con los roles del realm (mayúsculas y guion bajo)
 * definidos en {@code docker/keycloak/realm-export.json}. En Spring Security se exponen
 * con el prefijo {@value #PREFIJO_AUTORIDAD} para poder usar {@code hasRole("CLIENTE")}.</p>
 */
public final class RolesGuarderia {

    public static final String CLIENTE = "CLIENTE";
    public static final String RECEPCIONISTA = "RECEPCIONISTA";
    public static final String PERSONAL_CUIDADO = "PERSONAL_CUIDADO";
    public static final String CHOFER = "CHOFER";
    public static final String ADMIN = "ADMIN";

    /**
     * Catálogo cerrado de roles del proyecto. El conversor JWT solo otorga autoridades
     * para estos roles e ignora cualquier otro rol presente en el realm.
     */
    public static final Set<String> TODOS =
            Set.of(CLIENTE, RECEPCIONISTA, PERSONAL_CUIDADO, CHOFER, ADMIN);

    public static final String PREFIJO_AUTORIDAD = "ROLE_";

    private RolesGuarderia() {
    }

    /**
     * Construye el nombre de autoridad esperado por Spring Security.
     *
     * @param rol rol del catálogo, por ejemplo {@link #CLIENTE}
     * @return la autoridad con prefijo, por ejemplo {@code ROLE_CLIENTE}
     */
    public static String autoridad(String rol) {
        Objects.requireNonNull(rol, "El rol es obligatorio");
        return PREFIJO_AUTORIDAD + rol;
    }

    /**
     * Indica si el valor recibido pertenece al catálogo de roles del proyecto.
     */
    public static boolean esRolDelDominio(String rol) {
        return rol != null && TODOS.contains(rol);
    }
}
