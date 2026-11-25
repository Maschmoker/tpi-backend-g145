package com.guarderia.estadias_service.dto.response;

import com.guarderia.estadias_service.domain.EstadoServicioCuidado;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record EstadiaServicioResponse(
        Long id,
        Long servicioCatalogoId,
        String nombreServicio,
        BigDecimal precioAplicado,
        EstadoServicioCuidado estado,
        String responsableKeycloakId,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFinalizacion
) {
}