package com.guarderia.estadias_service.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record CotizacionResponse(
        int dias,
        BigDecimal costoServicios,
        BigDecimal costoTraslado,
        BigDecimal costoTotal,
        List<DetalleServicioCotizado> detalle
) {
}