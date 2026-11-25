package com.guarderia.logistica_service.service;

import com.guarderia.commons.geo.Coordenada;
import com.guarderia.commons.geo.Haversine;
import com.guarderia.logistica_service.client.GoogleMapsClient;
import com.guarderia.logistica_service.persistence.entity.Configuracion;
import com.guarderia.logistica_service.persistence.repository.ConfiguracionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class DistanciaService {

    private static final Logger log = LoggerFactory.getLogger(DistanciaService.class);

    private final GoogleMapsClient googleMapsClient;
    private final ConfiguracionRepository configuracionRepository;

    public DistanciaService(GoogleMapsClient googleMapsClient, ConfiguracionRepository configuracionRepository) {
        this.googleMapsClient = googleMapsClient;
        this.configuracionRepository = configuracionRepository;
    }

    public record ResultadoDistancia(double distanciaKm, int duracionMin, boolean estimado) {}

    public ResultadoDistancia entre(Coordenada a, Coordenada b) {
        if (googleMapsClient.disponible()) {
            var r = googleMapsClient.distancia(a, b);
            if (r.isPresent()) {
                var res = r.get();
                return new ResultadoDistancia(res.distanciaKm(), res.duracionMin(), false);
            }
            log.warn("Google Maps no disponible o falló; se usa Haversine como contingencia");
        }
        Configuracion cfg = configuracion();
        double km = Haversine.distanciaKm(a, b);
        int min = (int) Math.round(km / cfg.getVelocidadPromedioKmh() * 60.0);
        return new ResultadoDistancia(km, min, true);
    }

    private Configuracion configuracion() {
        return configuracionRepository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new IllegalStateException("Configuración no encontrada"));
    }
}