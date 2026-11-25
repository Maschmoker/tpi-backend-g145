package com.guarderia.logistica_service.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public final class RutaDtos {
    private RutaDtos() {}

    public record GenerarRutaRequest(@NotNull LocalDate fecha, @NotNull String tipo) {}

    public record ParadaResponse(Long id, Integer orden, Long trasladoId, Double distanciaDesdeAnteriorKm, Integer duracionDesdeAnteriorMin) {}

    public record RutaResponse(Long id, LocalDate fecha, String tipo, String estado, Double distanciaTotalKm,
            Integer duracionTotalMin, boolean estimada, String choferKeycloakId, List<ParadaResponse> paradas) {}
}