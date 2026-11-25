package com.guarderia.estadias_service.dto.response;

import com.guarderia.estadias_service.domain.EstadoEstadia;
import com.guarderia.estadias_service.domain.ModalidadEstadia;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record EstadiaResponse(
        Long id,
        Long clienteId,
        String clienteKeycloakId,
        Long mascotaId,
        String mascotaNombre,
        String mascotaEspecie,
        String mascotaTamanio,
        ModalidadEstadia modalidad,
        boolean requiereRetiro,
        boolean requiereDevolucion,
        Long direccionTrasladoId,
        EstadoEstadia estado,
        LocalDate fechaIngresoPlanificada,
        LocalDate fechaEgresoPlanificada,
        LocalDateTime fechaRecepcionReal,
        LocalDateTime fechaEntregaReal,
        BigDecimal costoServicios,
        BigDecimal costoTraslado,
        BigDecimal costoTotal,
        String observaciones,
        List<EstadiaServicioResponse> servicios
) {
}