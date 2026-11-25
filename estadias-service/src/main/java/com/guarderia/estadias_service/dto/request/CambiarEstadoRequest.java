package com.guarderia.estadias_service.dto.request;

import jakarta.validation.constraints.Size;

public record CambiarEstadoRequest(
        @Size(max = 500) String observacion
) {
}