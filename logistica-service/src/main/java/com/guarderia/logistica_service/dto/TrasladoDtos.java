package com.guarderia.logistica_service.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class TrasladoDtos {
    private TrasladoDtos() {}

    public record CotizacionTrasladoRequest(
            Long estadiaId, Long direccionId,
            @NotNull Double origenLat, @NotNull Double origenLng,
            Double destinoLat, Double destinoLng,
            @NotNull LocalDate fecha, boolean idaYVuelta) {}

    public record CotizacionTrasladoResponse(BigDecimal costoTraslado, Double distanciaKm, Integer duracionMin, boolean estimado) {}

    public record CrearTrasladoRequest(
            @NotNull Long estadiaId, @NotNull Long clienteId, @NotNull String tipo,
            @NotNull Long direccionId, @NotNull Double latitud, @NotNull Double longitud,
            @NotNull LocalDate fechaProgramada, String direccionTexto) {}

    public record TrasladoResponse(Long id, String estado) {}
}