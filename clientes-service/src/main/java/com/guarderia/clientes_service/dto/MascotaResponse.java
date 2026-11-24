package com.guarderia.clientes_service.dto;

import com.guarderia.clientes_service.domain.Especie;
import com.guarderia.clientes_service.domain.TamanioMascota;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MascotaResponse(
        Long id,
        Long clienteId,
        String nombre,
        Especie especie,
        String raza,
        TamanioMascota tamanio,
        BigDecimal pesoKg,
        String observaciones,
        boolean activa,
        LocalDateTime createdAt
) {
}