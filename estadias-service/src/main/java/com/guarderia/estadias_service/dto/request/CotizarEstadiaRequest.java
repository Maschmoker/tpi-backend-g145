package com.guarderia.estadias_service.dto.request;

import com.guarderia.estadias_service.domain.ModalidadEstadia;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CotizarEstadiaRequest(
        @NotNull Long mascotaId,
        @NotNull ModalidadEstadia modalidad,
        boolean requiereRetiro,
        boolean requiereDevolucion,
        Long direccionTrasladoId,
        @NotNull @FutureOrPresent LocalDate fechaIngresoPlanificada,
        @NotNull @FutureOrPresent LocalDate fechaEgresoPlanificada,
        @NotEmpty List<@NotNull Long> serviciosCatalogoIds,
        @Size(max = 1000) String observaciones
) {
}