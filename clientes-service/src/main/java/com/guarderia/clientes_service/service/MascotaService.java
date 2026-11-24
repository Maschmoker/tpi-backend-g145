package com.guarderia.clientes_service.service;

import com.guarderia.clientes_service.domain.TamanioMascota;
import com.guarderia.clientes_service.dto.MascotaRequest;
import com.guarderia.clientes_service.dto.MascotaResponse;
import com.guarderia.clientes_service.exception.AccesoDenegadoRecursoException;
import com.guarderia.clientes_service.exception.RecursoNoEncontradoException;
import com.guarderia.clientes_service.exception.ReglaNegocioException;
import com.guarderia.clientes_service.persistence.entity.Cliente;
import com.guarderia.clientes_service.persistence.entity.Mascota;
import com.guarderia.clientes_service.persistence.repository.ClienteRepository;
import com.guarderia.clientes_service.persistence.repository.MascotaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MascotaService {

    private final MascotaRepository mascotaRepository;
    private final ClienteRepository clienteRepository;

    public MascotaService(MascotaRepository mascotaRepository, ClienteRepository clienteRepository) {
        this.mascotaRepository = mascotaRepository;
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public List<MascotaResponse> listarPropias(Authentication auth, boolean soloActivas) {
        String kc = keycloakId(auth);
        List<Mascota> lista = soloActivas
                ? mascotaRepository.findByCliente_KeycloakIdAndActivaTrue(kc)
                : mascotaRepository.findByCliente_KeycloakId(kc);
        return lista.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MascotaResponse obtenerPropia(Authentication auth, Long mascotaId) {
        Mascota m = mascotaRepository.findByIdAndCliente_KeycloakId(mascotaId, keycloakId(auth))
                .orElseThrow(() -> new RecursoNoEncontradoException("Mascota no encontrada: " + mascotaId));
        return toResponse(m);
    }

    @Transactional
    public MascotaResponse registrarPropia(Authentication auth, MascotaRequest req) {
        Cliente cliente = obtenerClienteActual(auth);
        Mascota m = new Mascota();
        m.setCliente(cliente);
        map(m, req);
        m.setActiva(true);
        m = mascotaRepository.save(m);
        return toResponse(m);
    }

    @Transactional
    public MascotaResponse actualizarPropia(Authentication auth, Long mascotaId, MascotaRequest req) {
        Mascota m = mascotaRepository.findByIdAndCliente_KeycloakId(mascotaId, keycloakId(auth))
                .orElseThrow(() -> new RecursoNoEncontradoException("Mascota no encontrada: " + mascotaId));
        map(m, req);
        m = mascotaRepository.save(m);
        return toResponse(m);
    }

    @Transactional
    public MascotaResponse bajaLogica(Authentication auth, Long mascotaId) {
        Mascota m = mascotaRepository.findByIdAndCliente_KeycloakId(mascotaId, keycloakId(auth))
                .orElseThrow(() -> new RecursoNoEncontradoException("Mascota no encontrada: " + mascotaId));
        m.setActiva(false);
        m = mascotaRepository.save(m);
        return toResponse(m);
    }

    @Transactional(readOnly = true)
    public List<MascotaResponse> listarPorCliente(Authentication auth, Long clienteId, boolean soloActivas) {
        validarAccesoCliente(auth, clienteId);
        List<Mascota> lista = mascotaRepository.findByClienteId(clienteId);
        if (soloActivas) {
            lista = lista.stream().filter(Mascota::isActiva).toList();
        }
        return lista.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public MascotaResponse obtenerPorCliente(Authentication auth, Long clienteId, Long mascotaId) {
        validarAccesoCliente(auth, clienteId);
        Mascota m = mascotaRepository.findById(mascotaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Mascota no encontrada: " + mascotaId));
        if (!m.getCliente().getId().equals(clienteId)) {
            throw new RecursoNoEncontradoException("Mascota no encontrada: " + mascotaId);
        }
        return toResponse(m);
    }

    private void validarAccesoCliente(Authentication auth, Long clienteId) {
        if (esCliente(auth)) {
            Cliente propio = clienteRepository.findByKeycloakId(keycloakId(auth))
                    .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado"));
            if (!propio.getId().equals(clienteId)) {
                throw new AccesoDenegadoRecursoException("No puede consultar las mascotas de otro cliente");
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

    private void map(Mascota m, MascotaRequest req) {
        m.setNombre(req.nombre());
        m.setEspecie(req.especie());
        m.setRaza(req.raza());
        m.setTamanio(req.tamanio());
        m.setPesoKg(req.pesoKg());
        m.setObservaciones(req.observaciones());
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

    private MascotaResponse toResponse(Mascota m) {
        return new MascotaResponse(m.getId(), m.getCliente().getId(), m.getNombre(), m.getEspecie(),
                m.getRaza(), m.getTamanio(), m.getPesoKg(), m.getObservaciones(), m.isActiva(),
                m.getCreatedAt());
    }
}