package com.guarderia.clientes_service.service;

import com.guarderia.clientes_service.dto.ClientePerfilRequest;
import com.guarderia.clientes_service.dto.ClienteResponse;
import com.guarderia.clientes_service.exception.AccesoDenegadoRecursoException;
import com.guarderia.clientes_service.exception.RecursoNoEncontradoException;
import com.guarderia.clientes_service.persistence.entity.Cliente;
import com.guarderia.clientes_service.persistence.repository.ClienteRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;

    public ClienteService(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtenerPerfilActual(Authentication auth) {
        Cliente cliente = clienteRepository.findByKeycloakId(keycloakId(auth))
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe perfil de cliente para el usuario autenticado"));
        return toResponse(cliente);
    }

    @Transactional
    public ClienteResponse crearOActualizarPerfilActual(Authentication auth, ClientePerfilRequest req) {
        String kc = keycloakId(auth);
        Cliente cliente = clienteRepository.findByKeycloakId(kc).orElseGet(() -> {
            Cliente nuevo = new Cliente();
            nuevo.setKeycloakId(kc);
            nuevo.setActivo(true);
            return nuevo;
        });
        cliente.setNombre(req.nombre());
        cliente.setApellido(req.apellido());
        cliente.setEmail(req.email());
        cliente.setTelefono(req.telefono());
        cliente = clienteRepository.save(cliente);
        return toResponse(cliente);
    }

    @Transactional(readOnly = true)
    public ClienteResponse obtenerPorId(Authentication auth, Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Cliente no encontrado: " + id));
        if (esCliente(auth) && !cliente.getKeycloakId().equals(keycloakId(auth))) {
            throw new AccesoDenegadoRecursoException("No puede consultar el perfil de otro cliente");
        }
        return toResponse(cliente);
    }

    @Transactional(readOnly = true)
    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAll().stream().map(this::toResponse).toList();
    }

    private String keycloakId(Authentication auth) {
        return ((Jwt) auth.getPrincipal()).getSubject();
    }

    private boolean esCliente(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> "ROLE_CLIENTE".equals(a.getAuthority()));
    }

    private ClienteResponse toResponse(Cliente c) {
        return new ClienteResponse(c.getId(), c.getKeycloakId(), c.getNombre(), c.getApellido(),
                c.getEmail(), c.getTelefono(), c.isActivo(), c.getCreatedAt(), c.getUpdatedAt());
    }
}