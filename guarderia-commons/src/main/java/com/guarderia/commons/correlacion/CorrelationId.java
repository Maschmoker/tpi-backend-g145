package com.guarderia.commons.correlacion;

import java.util.regex.Pattern;

/**
 * Constantes del identificador de correlación usado para trazar una solicitud a través
 * del Gateway y los microservicios.
 */
public final class CorrelationId {

    /** Encabezado HTTP de correlación. */
    public static final String HEADER = "X-Correlation-ID";

    /** Clave usada en el MDC de SLF4J (patrón Log4j2: {@code %X{correlationId}}). */
    public static final String MDC_KEY = "correlationId";

    /** Atributo de request donde el filtro publica el valor normalizado. */
    public static final String ATRIBUTO_REQUEST = "com.guarderia.correlationId";

    public static final int LONGITUD_MAXIMA = 128;

    /**
     * Formato aceptado: alfanuméricos, punto, guion, guion bajo y dos puntos.
     * Se excluyen espacios, saltos de línea y cualquier carácter de control para evitar
     * inyección en logs o encabezados.
     */
    public static final Pattern FORMATO_VALIDO =
            Pattern.compile("[A-Za-z0-9._:-]{1," + LONGITUD_MAXIMA + "}");

    private CorrelationId() {
    }
}
