package com.guarderia.commons.correlacion;

import org.slf4j.MDC;

import java.util.UUID;

/**
 * Utilidades para generar, validar y propagar el {@code X-Correlation-ID}.
 *
 * <p>Es independiente de servlet y de Reactor: puede usarse tanto desde el filtro MVC como
 * desde un {@code WebFilter} del Gateway.</p>
 */
public final class CorrelationIdUtils {

    private CorrelationIdUtils() {
    }

    /**
     * Genera un identificador nuevo (UUID en formato estándar).
     */
    public static String generar() {
        return UUID.randomUUID().toString();
    }

    /**
     * Normaliza un valor recibido por encabezado.
     *
     * <p>Si es {@code null}, está en blanco o no cumple {@link CorrelationId#FORMATO_VALIDO}
     * se genera uno nuevo, de modo que nunca se refleja un valor potencialmente malicioso.</p>
     */
    public static String normalizar(String valor) {
        if (valor == null) {
            return generar();
        }
        String limpio = valor.trim();
        if (limpio.isEmpty() || !CorrelationId.FORMATO_VALIDO.matcher(limpio).matches()) {
            return generar();
        }
        return limpio;
    }

    /**
     * Devuelve el identificador activo en el MDC del hilo actual, o {@code null}.
     */
    public static String actual() {
        return MDC.get(CorrelationId.MDC_KEY);
    }

    /**
     * Normaliza el valor y lo publica en el MDC del hilo actual.
     */
    public static void establecer(String valor) {
        MDC.put(CorrelationId.MDC_KEY, normalizar(valor));
    }

    /**
     * Elimina el identificador del MDC del hilo actual.
     */
    public static void limpiar() {
        MDC.remove(CorrelationId.MDC_KEY);
    }
}
