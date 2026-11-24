package com.guarderia.clientes_service.service;

import com.guarderia.clientes_service.dto.DireccionPrincipalRequest;
import com.guarderia.clientes_service.dto.DireccionRequest;
import com.guarderia.clientes_service.dto.DireccionResponse;
import com.guarderia.clientes_service.exception.AccesoDenegadoRecursoException;
import com.guarderia.clientes_service.exception.RecursoNoEncontradoException;
import com.guarderia.clientes_service.exception.ReglaNegocioException;
import com.guarderia.clientes_service.persistence.entity.Cliente;
import com.guarderia.clientes_service.persistence.entity.Direccion;
import com.guarderia.clientes_service.persistence.repository.ClienteRepository;
import com.guarderia.clientes_service.persistence.repository.DireccionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DireccionService {

    private final DireccionRepository direccionRepository;
    private final ClienteRepository clienteRepository;

    public DireccionService(DireccionRepository direccionRepository, ClienteRepository clienteRepository) {
        this.direccionRepository = direccionRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public List<DireccionResponse> listarPropias(Authentication auth) {
        return direccionRepository.findByCliente_KeycloakId(keycloakId(auth)).stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DireccionResponse obtenerPropia(Authentication auth, Long direccionId) {
        Direccion d = direccionRepository.findByIdAndCliente_KeycloakId(direccionId, keycloakId(auth))
                .orElseThrow(() -> new RecursoNoEncontradoException("Dirección no encontrada: " + direccionId));
        return toResponse(d);
    }

    @Transactional
    public DireccionResponse crearPropia(Authentication auth, DireccionRequest req) {
        Cliente cliente = obtenerClienteActual(auth);
        Direccion d = new Direccion();
        d.setCliente(cliente);
        map(d, req);
        if (d.isEsPrincipal()) {
            asegurarPrincipalUnico(cliente, d);
        }
        d = direccionRepository.save(d);
        return toResponse(d);
    }

    @Transactional
    public DireccionResponse actualizarPropia(Authentication auth, Long direccionId, DireccionRequest req) {
        Direccion d = direccionRepository.findByIdAndCliente_KeycloakId(direccionId, keycloakId(auth))
                .orElseThrow(() -> new RecursoNoEncontradoException("Dirección no encontrada: " + direccionId));
        map(d, req);
        if (d.isEsPrincipal()) {
            asegurarPrincipalUnico(d.getCliente(), d);
        }
        d = direccionRepository.save(d);
        return toResponse(d);
    }

    @Transactional
    public DireccionResponse marcarPrincipal(Authentication auth, Long direccionId, boolean esPrincipal) {
        Direccion d = direccionRepository.findByIdAndCliente_KeycloakId(direccionId, keycloakId(auth))
                .orElseThrow(() -> new RecursoNoEncontradoException("Dirección no encontrada: " + direccionId));
        d.setEsPrincipal(esPrincipal);
        if (esPrincipal) {
            asegurarPrincipalUnico(d.getCliente(), d);
        }
        d = direccionRepository.save(d);
        return toResponse(d);
    }

    @Transactional
    public void eliminarPropia(Authentication auth, Long direccionId) {
        Direccion d = direccionRepository.findByIdAndCliente_KeycloakId(direccionId, keycloakId(auth))
                .orElseThrow(() -> new RecursoNoEncontradoException("Dirección no encontrada: " + direccionId));
        direccionRepository.delete(d);
    }

    @Transactional(readOnly = true)
    public List<DireccionResponse> listarPorCliente(Authentication auth, Long clienteId) {
        validarAccesoCliente(auth, clienteId);
        return direccionRepository.findByClienteId(clienteId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public DireccionResponse obtenerPorCliente(Authentication auth, Long clienteId, Long direccionId) {
        validarAccesoCliente(auth, clienteId);
        Direccion d = direccionRepository.findById(direccionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Dirección no encontrada: " + direccionId));
        if (!d.getCliente().getId().equals(clienteId)) {
            throw new RecursoNoEncontradoException("Dirección no encontrada: " + direccionId);
        }
        return toResponse(d);
    }

    private void validarAccesoCliente(Authentication auth, Long clienteId) {
        if (esCliente(auth)) {
            Cliente propio = clienteRepository.findByKeycloakId(keycloakId(auth))
                    .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));
            if (!propio.getId().equals(clienteId)) {
                throw new AccesoDenegadoRecursoException("No puede consultar las direcciones de otro cliente");
            }
        } else if (!esStaff(auth)) {
            throw new AccesoDenegadoRecursoException("No tiene permisos para esta operación");
        }
    }

    private Cliente obtenerClienteActual(Authentication auth) {
        return clienteRepository.findByKeycloakId(keycloakId(auth))
                .orElseThrow(() -> new ReglaNegocioException(HttpStatus.CONFLICT, "CLIENTE_NO_REGISTRADO",
                        "Debe registrar su perfil de cliente antes de esta operación"));
    }

    private void asegurarPrincipalUnico(Cliente cliente, Direccion candidata) {
        direccionRepository.findByClienteIdAndEsPrincipalTrue(cliente.getId()).ifPresent(actual -> {
            if (actual.getId() != null && !actual.getId().equals(candidata.getId())) {
                actual.setEsPrincipal(false);
                direccionRepository.save(actual);
            }
        });
    }

    private void map(Direccion d, DireccionRequest req) {
        d.setCalle(req.calle());
        d.setNumero(req.numero());
        d.setBarrio(req.barrio());
        d.setPiso(req.piso());
        d.setDepartamento(req.departamento());
        String ciudad = req.ciudad();
        d.setCiudad((ciudad == null || ciudad.isBlank()) ? "Córdoba" : ciudad);
        d.setLatitud(req.latitud());
        d.setLongitud(req.longitud());
        d.setEsPrincipal(req.esPrincipal());
    }

    private String keycloakId(Authentication auth) {
        return ((Jwt) auth.getPrincipal()).getSubject();
    }

    private boolean esCliente(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> "ROLE_CLIENTE".equals(a.getAuthority()));
    }

    private boolean esStaff(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a ->
                "ROLE_RECEPCIONISTA".equals(a.getAuthority()) || "ROLE_ADMIN".equals(a.getAuthority()));
    }

    private DireccionResponse toResponse(Direccion d) {
        return new DireccionResponse(d.getId(), d.getCliente().getId(), d.getCalle(), d.getNumero(),
                d.getBarrio(), d.getPiso(), d.getDepartamento(), d.getCiudad(), d.getLatitud(),
                d.getLongitud(), d.isEsPrincipal(), d.getCreatedAt());
    }
}