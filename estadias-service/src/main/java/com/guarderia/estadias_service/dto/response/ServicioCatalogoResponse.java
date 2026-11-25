package com.guarderia.estadias_service.dto.response;

import com.guarderia.estadias_service.domain.TipoServicio;

import java.math.BigDecimal;
import java.util.List;

public record ServicioCatalogoResponse(
        Long id,
        String nombre,
        String descripcion,
        TipoServicio tipoServicio,
        BigDecimal precioPequeno,
        BigDecimal precioMediano,
        BigDecimal precioGrande,
        List<String> especiesAplicables,
        boolean activo
) {
}