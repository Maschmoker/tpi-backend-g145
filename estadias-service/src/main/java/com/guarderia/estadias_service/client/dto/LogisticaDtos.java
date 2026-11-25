package com.guarderia.estadias_service.client.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public final class LogisticaDtos {
    private LogisticaDtos() {
    }

    public record CotizacionTrasladoRequest(Long estadiaId, Long direccionId, Double origenLat, Double origenLng,
                                           Double destinoLat, Double destinoLng, LocalDate fecha, boolean idaYVuelta) {
    }

    public record CotizacionTrasladoResponse(BigDecimal costoTraslado, Double distanciaKm, Integer duracionMin,
                                            boolean estimado) {
    }

    public record CrearTrasladoRequest(Long estadiaId, String tipo, Long direccionId, Double latitud, Double longitud,
                                       LocalDate fechaProgramada) {
    }

    public record TrasladoResponse(Long id, String estado) {
    }
}