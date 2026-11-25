package com.guarderia.estadias_service.dto.response;

import java.math.BigDecimal;

public record DetalleServicioCotizado(
        Long servicioCatalogoId,
        String nombre,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
}