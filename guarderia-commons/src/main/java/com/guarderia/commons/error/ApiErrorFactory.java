package com.guarderia.commons.error;

import org.springframework.http.HttpStatus;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Fábrica del contrato {@link ApiError}.
 *
 * <p>Los códigos definidos aquí son técnicos y comunes a todos los servicios. Los códigos
 * de dominio (por ejemplo, transición de estado inválida o recurso no encontrado) los define
 * cada microservicio en su propia capa de aplicación.</p>
 */
public final class ApiErrorFactory {

    public static final String CODIGO_VALIDACION = "VALIDACION";
    public static final String CODIGO_CUERPO_ILEGIBLE = "CUERPO_ILEGIBLE";
    public static final String CODIGO_PARAMETRO_FALTANTE = "PARAMETRO_FALTANTE";
    public static final String CODIGO_METODO_NO_SOPORTADO = "METODO_NO_SOPORTADO";
    public static final String CODIGO_MEDIA_TYPE_NO_SOPORTADO = "MEDIA_TYPE_NO_SOPORTADO";
    public static final String CODIGO_ERROR_INTERNO = "ERROR_INTERNO";

    private ApiErrorFactory() {
    }

    public static ApiError crear(HttpStatus estado, String codigo, String mensaje, String ruta,
                                 String correlationId, List<ApiErrorDetalle> detalles) {
        Objects.requireNonNull(estado, "El estado HTTP es obligatorio");
        return new ApiError(Instant.now(), estado.value(), codigo, mensaje, ruta, correlationId, detalles);
    }

    public static ApiError crear(HttpStatus estado, String codigo, String mensaje, String ruta,
                                 String correlationId) {
        return crear(estado, codigo, mensaje, ruta, correlationId, List.of());
    }

    public static ApiError validacion(String ruta, String correlationId, List<ApiErrorDetalle> detalles) {
        return crear(HttpStatus.BAD_REQUEST, CODIGO_VALIDACION,
                "La solicitud contiene datos inválidos", ruta, correlationId, detalles);
    }

    public static ApiError interno(String ruta, String correlationId) {
        return crear(HttpStatus.INTERNAL_SERVER_ERROR, CODIGO_ERROR_INTERNO,
                "Ocurrió un error inesperado", ruta, correlationId);
    }
}
