package com.guarderia.clientes_service.dto;

import jakarta.validation.constraints.NotNull;

public record DireccionPrincipalRequest(@NotNull Boolean esPrincipal) {
}