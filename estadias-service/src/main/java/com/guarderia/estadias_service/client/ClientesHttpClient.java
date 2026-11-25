package com.guarderia.estadias_service.client;

import com.guarderia.commons.correlacion.CorrelationId;
import com.guarderia.commons.correlacion.CorrelationIdUtils;
import com.guarderia.estadias_service.client.dto.ClientesSnapshots;
import com.guarderia.estadias_service.exception.AccesoDenegadoRecursoException;
import com.guarderia.estadias_service.exception.IntegracionServicioException;
import com.guarderia.estadias_service.exception.RecursoNoEncontradoException;
import com.guarderia.estadias_service.exception.ReglaNegocioException;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;

@Component
public class ClientesHttpClient {

    private final RestClient restClient;

    public ClientesHttpClient(@org.springframework.beans.factory.annotation.Qualifier("clientesRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    public ClientesSnapshots.ClienteSnapshot obtenerClienteActual(String token) {
        ClienteRemoto remoto = ejecutarGet("/api/clientes/me", token, ClienteRemoto.class);
        return new ClientesSnapshots.ClienteSnapshot(remoto.id(), remoto.keycloakId(), remoto.nombre(), remoto.apellido(), remoto.activo());
    }

    public ClientesSnapshots.MascotaSnapshot obtenerMascotaPropia(String token, Long mascotaId) {
        MascotaRemoto remoto = ejecutarGet("/api/clientes/me/mascotas/" + mascotaId, token, MascotaRemoto.class);
        return mapearMascota(remoto);
    }

    public ClientesSnapshots.MascotaSnapshot obtenerMascotaPorCliente(String token, Long clienteId, Long mascotaId) {
        MascotaRemoto remoto = ejecutarGet("/api/clientes/" + clienteId + "/mascotas/" + mascotaId, token, MascotaRemoto.class);
        return mapearMascota(remoto);
    }

    public ClientesSnapshots.DireccionSnapshot obtenerDireccionPropia(String token, Long direccionId) {
        DireccionRemoto remoto = ejecutarGet("/api/clientes/me/direcciones/" + direccionId, token, DireccionRemoto.class);
        return mapearDireccion(remoto);
    }

    public ClientesSnapshots.DireccionSnapshot obtenerDireccionPorCliente(String token, Long clienteId, Long direccionId) {
        DireccionRemoto remoto = ejecutarGet("/api/clientes/" + clienteId + "/direcciones/" + direccionId, token, DireccionRemoto.class);
        return mapearDireccion(remoto);
    }

    private <T> T ejecutarGet(String path, String token, Class<T> tipo) {
        try {
            return restClient.get()
                    .uri(path)
                    .header("Authorization", "Bearer " + token)
                    .header(CorrelationId.HEADER, CorrelationIdUtils.actual() != null ? CorrelationIdUtils.actual() : CorrelationIdUtils.generar())
                    .retrieve()
                    .body(tipo);
        } catch (RestClientResponseException ex) {
            mapearErrorHttp(ex);
            throw new IntegracionServicioException("Error inesperado al consultar clientes-service", ex);
        } catch (Exception ex) {
            throw new IntegracionServicioException("No se pudo conectar con clientes-service", ex);
        }
    }

    private void mapearErrorHttp(RestClientResponseException ex) {
        HttpStatusCode status = ex.getStatusCode();
        String mensaje = ex.getResponseBodyAsString();
        if (status.value() == 404) {
            throw new RecursoNoEncontradoException(mensaje.isBlank() ? "Recurso no encontrado en clientes-service" : mensaje);
        }
        if (status.value() == 401 || status.value() == 403) {
            throw new AccesoDenegadoRecursoException(mensaje.isBlank() ? "Acceso denegado en clientes-service" : mensaje);
        }
        if (status.value() >= 400 && status.value() < 500) {
            throw new ReglaNegocioException(org.springframework.http.HttpStatus.valueOf(status.value()),
                    "REGLA_CLIENTES", mensaje.isBlank() ? "Solicitud invÃ¡lida a clientes-service" : mensaje);
        }
        throw new IntegracionServicioException("Error del servicio de clientes: HTTP " + status.value());
    }

    private ClientesSnapshots.MascotaSnapshot mapearMascota(MascotaRemoto remoto) {
        return new ClientesSnapshots.MascotaSnapshot(remoto.id(), remoto.clienteId(), remoto.nombre(), remoto.especie(),
                remoto.tamanio(), remoto.pesoKg(), remoto.activa());
    }

    private ClientesSnapshots.DireccionSnapshot mapearDireccion(DireccionRemoto remoto) {
        return new ClientesSnapshots.DireccionSnapshot(remoto.id(), remoto.clienteId(), remoto.calle(), remoto.numero(),
                remoto.ciudad(), remoto.latitud(), remoto.longitud());
    }

    // Records de deserializaciÃ³n (campos alineados con clientes-service)
    public record ClienteRemoto(Long id, String keycloakId, String nombre, String apellido, String email, String telefono,
                                boolean activo, java.time.LocalDateTime createdAt, java.time.LocalDateTime updatedAt) {
    }

    public record MascotaRemoto(Long id, Long clienteId, String nombre, String especie, String raza, String tamanio,
                                java.math.BigDecimal pesoKg, String observaciones, boolean activa,
                                java.time.LocalDateTime createdAt) {
    }

    public record DireccionRemoto(Long id, Long clienteId, String calle, String numero, String barrio, String piso,
                                  String departamento, String ciudad, Double latitud, Double longitud, boolean esPrincipal,
                                  java.time.LocalDateTime createdAt) {
    }
}
