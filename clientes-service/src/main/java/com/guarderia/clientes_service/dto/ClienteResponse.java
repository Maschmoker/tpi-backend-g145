package com.guarderia.clientes_service.dto;

import java.time.LocalDateTime;

public record ClienteResponse(
        Long id,
        String keycloakId,
        String nombre,
        String apellido,
        String email,
        String telefono,
        boolean activo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}