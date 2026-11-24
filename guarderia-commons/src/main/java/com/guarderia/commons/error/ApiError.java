package com.guarderia.commons.error;

import java.time.Instant;
import java.util.List;

/**
 * Estructura estándar de error de la API de la guardería.
 *
 * <p>Se serializa a JSON y es el cuerpo de todas las respuestas de error, tanto técnicas
 * (validadas por {@link ManejadorErroresBase}) como de dominio (definidas por cada
 * microservicio).</p>
 *
 * @param timestamp     instante en que se generó el error (UTC)
 * @param status        código HTTP
 * @param codigo        clave estable de máquina, por ejemplo {@code VALIDACION}
 * @param mensaje       descripción legible
 * @param ruta          path de la solicitud, sin query string
 * @param correlationId identificador de correlación de la solicitud
 * @param detalles      detalles de validación; nunca {@code null}
 */
public record ApiError(
        Instant timestamp,
        int status,
        String codigo,
        String mensaje,
        String ruta,
        String correlationId,
        List<ApiErrorDetalle> detalles
) {

    public ApiError {
        detalles = (detalles == null || detalles.isEmpty()) ? List.of() : List.copyOf(detalles);
    }
}
