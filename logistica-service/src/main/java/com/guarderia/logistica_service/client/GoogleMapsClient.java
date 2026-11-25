package com.guarderia.logistica_service.client;

import com.guarderia.commons.geo.Coordenada;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;

@Component
public class GoogleMapsClient {

    private static final Logger log = LoggerFactory.getLogger(GoogleMapsClient.class);

    private final RestClient restClient;
    private final String apiKey;
    private final boolean enabled;
    private final String geocodingBaseUrl;
    private final String distanceMatrixBaseUrl;

    public GoogleMapsClient(@org.springframework.beans.factory.annotation.Qualifier("googleMapsRestClient") RestClient restClient,
                            @Value("${google.maps.api-key:}") String apiKey,
                            @Value("${google.maps.enabled:true}") boolean enabled,
                            @Value("${google.maps.geocoding.base-url}") String geocodingBaseUrl,
                            @Value("${google.maps.distance-matrix.base-url}") String distanceMatrixBaseUrl) {
        this.restClient = restClient;
        this.apiKey = apiKey == null ? "" : apiKey.trim();
        this.enabled = enabled;
        this.geocodingBaseUrl = geocodingBaseUrl;
        this.distanceMatrixBaseUrl = distanceMatrixBaseUrl;
    }

    public boolean disponible() {
        return enabled && !apiKey.isBlank();
    }

    public Optional<Coordenada> geocodificar(String direccion) {
        if (!disponible() || direccion == null || direccion.isBlank()) return Optional.empty();
        try {
            String url = geocodingBaseUrl + "?address=" + URLEncoder.encode(direccion, StandardCharsets.UTF_8)
                    + "&key=" + apiKey;
            Map<?, ?> resp = restClient.get().uri(url).retrieve().body(Map.class);
            if (resp == null) return Optional.empty();
            Object results = resp.get("results");
            if (!(results instanceof java.util.List<?> list) || list.isEmpty()) return Optional.empty();
            Object first = list.get(0);
            if (!(first instanceof Map<?, ?> m)) return Optional.empty();
            Object geom = m.get("geometry");
            if (!(geom instanceof Map<?, ?> g)) return Optional.empty();
            Object loc = g.get("location");
            if (!(loc instanceof Map<?, ?> l)) return Optional.empty();
            double lat = ((Number) l.get("lat")).doubleValue();
            double lng = ((Number) l.get("lng")).doubleValue();
            return Optional.of(new Coordenada(lat, lng));
        } catch (Exception e) {
            log.warn("Google Geocoding falló: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public Optional<ResultadoDistancia> distancia(Coordenada origen, Coordenada destino) {
        if (!disponible()) return Optional.empty();
        try {
            String url = distanceMatrixBaseUrl
                    + "?origins=" + origen.latitud() + "," + origen.longitud()
                    + "&destinations=" + destino.latitud() + "," + destino.longitud()
                    + "&key=" + apiKey;
            Map<?, ?> resp = restClient.get().uri(url).retrieve().body(Map.class);
            if (resp == null) return Optional.empty();
            Object rows = resp.get("rows");
            if (!(rows instanceof java.util.List<?> rlist) || rlist.isEmpty()) return Optional.empty();
            Object row = rlist.get(0);
            if (!(row instanceof Map<?, ?> rm)) return Optional.empty();
            Object elems = rm.get("elements");
            if (!(elems instanceof java.util.List<?> elist) || elist.isEmpty()) return Optional.empty();
            Object el = elist.get(0);
            if (!(el instanceof Map<?, ?> em)) return Optional.empty();
            Object status = em.get("status");
            if (!"OK".equals(status)) return Optional.empty();
            Object dist = em.get("distance");
            Object dur = em.get("duration");
            double metros = dist instanceof Map<?, ?> dm ? ((Number) dm.get("value")).doubleValue() : 0.0;
            double segundos = dur instanceof Map<?, ?> du ? ((Number) du.get("value")).doubleValue() : 0.0;
            return Optional.of(new ResultadoDistancia(metros / 1000.0, (int) Math.round(segundos / 60.0), false));
        } catch (Exception e) {
            log.warn("Google Distance Matrix falló: {}", e.getMessage());
            return Optional.empty();
        }
    }

    public record ResultadoDistancia(double distanciaKm, int duracionMin, boolean estimado) {}
}