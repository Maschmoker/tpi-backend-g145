package com.guarderia.commons.geo;

/**
 * Coordenada geográfica (latitud/longitud en grados decimales).
 *
 * <p>El constructor compacto valida finitud y rangos, de modo que un objeto
 * {@code Coordenada} siempre representa un punto válido.</p>
 *
 * <p>Al usar tipos primitivos, un JSON con un componente ausente deserializa a {@code 0.0}.
 * Los DTOs de cada servicio deben validar la presencia de la coordenada antes de construirla
 * (por ejemplo, con {@code @NotNull} sobre el objeto del DTO).</p>
 *
 * @param latitud  grados decimales entre -90 y 90
 * @param longitud grados decimales entre -180 y 180
 */
public record Coordenada(double latitud, double longitud) {

    public Coordenada {
        if (!Double.isFinite(latitud) || latitud < -90.0 || latitud > 90.0) {
            throw new IllegalArgumentException("La latitud debe ser un número finito entre -90 y 90");
        }
        if (!Double.isFinite(longitud) || longitud < -180.0 || longitud > 180.0) {
            throw new IllegalArgumentException("La longitud debe ser un número finito entre -180 y 180");
        }
    }
}
