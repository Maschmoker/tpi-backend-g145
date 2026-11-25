package com.guarderia.logistica_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public final class ConfiguracionDtos {
    private ConfiguracionDtos() {}

    public record ConfiguracionResponse(Long id, Double guarderiaLatitud, Double guarderiaLongitud, String guarderiaDireccion,
            Double radioCoberturaKm, BigDecimal tarifaPorKm, BigDecimal costoMinimoTraslado,
            Integer capacidadDiariaTraslados, Double radioAgrupacionKm, Double velocidadPromedioKmh, LocalDateTime updatedAt) {}

    public record ConfiguracionRequest(Double guarderiaLatitud, Double guarderiaLongitud, String guarderiaDireccion,
            Double radioCoberturaKm, BigDecimal tarifaPorKm, BigDecimal costoMinimoTraslado,
            Integer capacidadDiariaTraslados, Double radioAgrupacionKm, Double velocidadPromedioKmh) {}
}