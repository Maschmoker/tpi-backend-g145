package com.guarderia.clientes_service.controller;

import com.guarderia.clientes_service.dto.ClientePerfilRequest;
import com.guarderia.clientes_service.dto.ClienteResponse;
import com.guarderia.clientes_service.service.ClienteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "Gestión de perfiles de clientes")
@SecurityRequirement(name = "bearerAuth")
public class ClienteController {

    private final ClienteService clienteService;

    public ClienteController(ClienteService clienteService) {
        this.clienteService = clienteService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Obtener el perfil del cliente autenticado")
    public ResponseEntity<ClienteResponse> obtenerMe(Authentication auth) {
        return ResponseEntity.ok(clienteService.obtenerPerfilActual(auth));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Crear o actualizar el perfil del cliente autenticado (upsert)")
    public ResponseEntity<ClienteResponse> upsertMe(Authentication auth,
                                                   @Valid @RequestBody ClientePerfilRequest req) {
        return ResponseEntity.ok(clienteService.crearOActualizarPerfilActual(auth, req));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RECEPCIONISTA','ADMIN')")
    @Operation(summary = "Listar todos los clientes (solo recepcionista/admin)")
    public ResponseEntity<List<ClienteResponse>> listar(Authentication auth) {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE','RECEPCIONISTA','ADMIN')")
    @Operation(summary = "Obtener un cliente por id")
    public ResponseEntity<ClienteResponse> obtenerPorId(Authentication auth, @PathVariable Long id) {
        return ResponseEntity.ok(clienteService.obtenerPorId(auth, id));
    }
}