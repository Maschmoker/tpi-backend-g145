package com.guarderia.clientes_service.dto;

import com.guarderia.clientes_service.domain.Especie;
import com.guarderia.clientes_service.domain.TamanioMascota;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record MascotaRequest(
        @NotBlank @Size(max = 100) String nombre,
        @NotNull Especie especie,
        @Size(max = 100) String raza,
        @NotNull TamanioMascota tamanio,
        @DecimalMin("0.01") @DecimalMax("999.99") BigDecimal pesoKg,
        @Size(max = 1000) String observaciones
) {
}