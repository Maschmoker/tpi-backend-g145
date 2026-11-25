package com.guarderia.estadias_service.client;

import com.guarderia.commons.correlacion.CorrelationId;
import com.guarderia.commons.correlacion.CorrelationIdUtils;
import com.guarderia.estadias_service.client.dto.LogisticaDtos;
import com.guarderia.estadias_service.exception.AccesoDenegadoRecursoException;
import com.guarderia.estadias_service.exception.IntegracionServicioException;
import com.guarderia.estadias_service.exception.RecursoNoEncontradoException;
import com.guarderia.estadias_service.exception.ReglaNegocioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;

@Component
public class LogisticaHttpClient {

    private static final Logger log = LoggerFactory.getLogger(LogisticaHttpClient.class);

    private final RestClient restClient;

    public LogisticaHttpClient(@org.springframework.beans.factory.annotation.Qualifier("logisticaRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public LogisticaDtos.CotizacionTrasladoResponse cotizarTraslado(String token, LogisticaDtos.CotizacionTrasladoRequest request) {
        return ejecutarPost("/api/logistica/traslados/cotizar", token, request, LogisticaDtos.CotizacionTrasladoResponse.class);
    }

    public LogisticaDtos.TrasladoResponse crearTraslado(String token, LogisticaDtos.CrearTrasladoRequest request) {
        return ejecutarPost("/api/logistica/traslados", token, request, LogisticaDtos.TrasladoResponse.class);
    }

    public void habilitarTraslado(String token, Long trasladoId) {
        ejecutarPatch("/api/logistica/traslados/" + trasladoId + "/habilitar", token, null);
    }

    public void habilitarTrasladoPorEstadia(String token, Long estadiaId) {
        ejecutarPatch("/api/logistica/traslados/por-estadia/" + estadiaId + "/habilitar", token, null);
    }

    public void cancelarTrasladosPorEstadia(String token, Long estadiaId) {
        ejecutarPatch("/api/logistica/traslados/cancelar-por-estadia/" + estadiaId, token, null);
    }

    private <T> T ejecutarPost(String path, String token, Object body, Class<T> tipo) {
        try {
            return restClient.post()
                    .uri(path)
                    .header("Authorization", "Bearer " + token)
                    .header(CorrelationId.HEADER, correlationId())
                    .body(body)
                    .retrieve()
                    .body(tipo);
        } catch (RestClientResponseException ex) {
            mapearErrorHttp(ex);
            throw new IntegracionServicioException("Error inesperado al consultar logistica-service", ex);
        } catch (Exception ex) {
            throw new IntegracionServicioException("No se pudo conectar con logistica-service", ex);
        }
    }

    private void ejecutarPatch(String path, String token, Object body) {
        try {
            RestClient.RequestBodySpec spec = restClient.patch()
                    .uri(path)
                    .header("Authorization", "Bearer " + token)
                    .header(CorrelationId.HEADER, correlationId());
            if (body != null) {
                spec.body(body);
            }
            spec.retrieve().toBodilessEntity();
        } catch (RestClientResponseException ex) {
            mapearErrorHttp(ex);
            throw new IntegracionServicioException("Error inesperado al consultar logistica-service", ex);
        } catch (Exception ex) {
            log.warn("No se pudo conectar con logistica-service en {}: {}", path, ex.getMessage());
            throw new IntegracionServicioException("No se pudo conectar con logistica-service", ex);
        }
    }

    private String correlationId() {
        String actual = CorrelationIdUtils.actual();
        return actual != null ? actual : CorrelationIdUtils.generar();
    }

    private void mapearErrorHttp(RestClientResponseException ex) {
        HttpStatusCode status = ex.getStatusCode();
        String mensaje = ex.getResponseBodyAsString();
        if (status.value() == 404) {
            throw new RecursoNoEncontradoException(mensaje.isBlank() ? "Recurso no encontrado en logistica-service" : mensaje);
        }
        if (status.value() == 401 || status.value() == 403) {
            throw new AccesoDenegadoRecursoException(mensaje.isBlank() ? "Acceso denegado en logistica-service" : mensaje);
        }
        if (status.value() >= 400 && status.value() < 500) {
            throw new ReglaNegocioException(org.springframework.http.HttpStatus.valueOf(status.value()),
                    "REGLA_LOGISTICA", mensaje.isBlank() ? "Solicitud inválida a logistica-service" : mensaje);
        }
        throw new IntegracionServicioException("Error del servicio de logística: HTTP " + status.value());
    }
}