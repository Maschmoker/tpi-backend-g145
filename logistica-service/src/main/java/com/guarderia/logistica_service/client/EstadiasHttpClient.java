package com.guarderia.logistica_service.client;

import com.guarderia.commons.correlacion.CorrelationId;
import com.guarderia.commons.correlacion.CorrelationIdUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class EstadiasHttpClient {

    private static final Logger log = LoggerFactory.getLogger(EstadiasHttpClient.class);

    private final RestClient restClient;

    public EstadiasHttpClient(@org.springframework.beans.factory.annotation.Qualifier("estadiasRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public void marcarTransitoIda(String token, Long estadiaId) {
        ejecutarPatch("/api/estadias/" + estadiaId + "/transito-ida", token);
    }

    public void marcarRecibida(String token, Long estadiaId) {
        ejecutarPatch("/api/estadias/" + estadiaId + "/recibida", token);
    }

    public void marcarTransitoVuelta(String token, Long estadiaId) {
        ejecutarPatch("/api/estadias/" + estadiaId + "/transito-vuelta", token);
    }

    public void marcarEntregada(String token, Long estadiaId) {
        ejecutarPatch("/api/estadias/" + estadiaId + "/entregada", token);
    }

    private void ejecutarPatch(String path, String token) {
        try {
            restClient.patch().uri(path)
                    .header("Authorization", "Bearer " + token)
                    .header(CorrelationId.HEADER, correlationId())
                    .retrieve().toBodilessEntity();
        } catch (RestClientResponseException ex) {
            log.warn("Error al notificar estadias-service ({}): {}", ex.getStatusCode().value(), ex.getResponseBodyAsString());
        } catch (Exception ex) {
            log.warn("No se pudo conectar con estadias-service: {}", ex.getMessage());
        }
    }

    private String correlationId() {
        String actual = CorrelationIdUtils.actual();
        return actual != null ? actual : CorrelationIdUtils.generar();
    }
}