package com.guarderia.commons.geo;

import java.util.Objects;

/**
 * Cálculo de distancia geodésica aproximada mediante la fórmula de Haversine.
 *
 * <p><strong>Importante:</strong> Haversine calcula la distancia en línea recta sobre una
 * esfera; <em>no</em> representa una distancia real por calles. En {@code logistica-service}
 * solo debe usarse como contingencia cuando Google Maps Platform no esté disponible, y la
 * ruta o cotización resultante debe marcarse como estimada.</p>
 */
public final class Haversine {

    /** Radio medio terrestre en kilómetros (IUGG). */
    public static final double RADIO_TIERRA_KM = 6371.0088;

    private Haversine() {
    }

    /**
     * Distancia en kilómetros entre dos coordenadas.
     *
     * @throws NullPointerException si alguna coordenada es {@code null}
     */
    public static double distanciaKm(Coordenada origen, Coordenada destino) {
        Objects.requireNonNull(origen, "La coordenada de origen es obligatoria");
        Objects.requireNonNull(destino, "La coordenada de destino es obligatoria");

        double latitudOrigen = Math.toRadians(origen.latitud());
        double latitudDestino = Math.toRadians(destino.latitud());
        double deltaLatitud = latitudDestino - latitudOrigen;
        double deltaLongitud = Math.toRadians(destino.longitud() - origen.longitud());

        double a = Math.pow(Math.sin(deltaLatitud / 2.0), 2)
                + Math.cos(latitudOrigen) * Math.cos(latitudDestino)
                * Math.pow(Math.sin(deltaLongitud / 2.0), 2);

        // Protección numérica ante redondeos en puntos casi antipodales.
        a = Math.min(1.0, Math.max(0.0, a));

        double c = 2.0 * Math.atan2(Math.sqrt(a), Math.sqrt(1.0 - a));
        return RADIO_TIERRA_KM * c;
    }

    /**
     * Distancia en metros entre dos coordenadas.
     *
     * @throws NullPointerException si alguna coordenada es {@code null}
     */
    public static double distanciaMetros(Coordenada origen, Coordenada destino) {
        return distanciaKm(origen, destino) * 1000.0;
    }
}
