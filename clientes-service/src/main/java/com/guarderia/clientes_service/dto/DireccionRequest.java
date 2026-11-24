package com.guarderia.clientes_service.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DireccionRequest(
        @NotBlank @Size(max = 200) String calle,
        @Size(max = 20) String numero,
        @Size(max = 100) String barrio,
        @Size(max = 20) String piso,
        @Size(max = 20) String departamento,
        @Size(max = 100) String ciudad,
        @DecimalMin("-90.0") @DecimalMax("90.0") Double latitud,
        @DecimalMin("-180.0") @DecimalMax("180.0") Double longitud,
        boolean esPrincipal
) {
}