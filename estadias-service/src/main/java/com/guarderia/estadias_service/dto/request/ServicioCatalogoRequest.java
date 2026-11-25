package com.guarderia.estadias_service.dto.request;

import com.guarderia.estadias_service.domain.TipoServicio;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record ServicioCatalogoRequest(
        @NotBlank @Size(max = 100) String nombre,
        @Size(max = 500) String descripcion,
        @NotNull TipoServicio tipoServicio,
        @NotNull @DecimalMin("0.00") BigDecimal precioPequeno,
        @NotNull @DecimalMin("0.00") BigDecimal precioMediano,
        @NotNull @DecimalMin("0.00") BigDecimal precioGrande,
        @NotBlank @Size(max = 255) String especiesAplicables,
        boolean activo
) {
}