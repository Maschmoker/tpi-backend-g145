package com.guarderia.clientes_service.dto;

import java.time.LocalDateTime;

public record DireccionResponse(
        Long id,
        Long clienteId,
        String calle,
        String numero,
        String barrio,
        String piso,
        String departamento,
        String ciudad,
        Double latitud,
        Double longitud,
        boolean esPrincipal,
        LocalDateTime createdAt
) {
}