package com.guarderia.clientes_service.controller;

import com.guarderia.clientes_service.dto.DireccionPrincipalRequest;
import com.guarderia.clientes_service.dto.DireccionRequest;
import com.guarderia.clientes_service.dto.DireccionResponse;
import com.guarderia.clientes_service.service.DireccionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Direcciones", description = "Gestión de direcciones de clientes")
@SecurityRequirement(name = "bearerAuth")
public class DireccionController {

    private final DireccionService direccionService;

    public DireccionController(DireccionService direccionService) {
        this.direccionService = direccionService;
    }

    @GetMapping("/me/direcciones")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Listar las direcciones del cliente autenticado")
    public ResponseEntity<List<DireccionResponse>> listarPropias(Authentication auth) {
        return ResponseEntity.ok(direccionService.listarPropias(auth));
    }

    @PostMapping("/me/direcciones")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Crear una dirección para el cliente autenticado")
    public ResponseEntity<DireccionResponse> crearPropia(Authentication auth,
                                                        @Valid @RequestBody DireccionRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(direccionService.crearPropia(auth, req));
    }

    @GetMapping("/me/direcciones/{direccionId}")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Obtener una dirección propia")
    public ResponseEntity<DireccionResponse> obtenerPropia(Authentication auth, @PathVariable Long direccionId) {
        return ResponseEntity.ok(direccionService.obtenerPropia(auth, direccionId));
    }

    @PutMapping("/me/direcciones/{direccionId}")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Actualizar una dirección propia")
    public ResponseEntity<DireccionResponse> actualizarPropia(Authentication auth, @PathVariable Long direccionId,
                                                            @Valid @RequestBody DireccionRequest req) {
        return ResponseEntity.ok(direccionService.actualizarPropia(auth, direccionId, req));
    }

    @PatchMapping("/me/direcciones/{direccionId}/principal")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Marcar/desmarcar una dirección como principal")
    public ResponseEntity<DireccionResponse> marcarPrincipal(Authentication auth, @PathVariable Long direccionId,
                                                           @Valid @RequestBody DireccionPrincipalRequest req) {
        return ResponseEntity.ok(direccionService.marcarPrincipal(auth, direccionId, Boolean.TRUE.equals(req.esPrincipal())));
    }

    @DeleteMapping("/me/direcciones/{direccionId}")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Eliminar una dirección propia")
    public ResponseEntity<Void> eliminarPropia(Authentication auth, @PathVariable Long direccionId) {
        direccionService.eliminarPropia(auth, direccionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{clienteId}/direcciones")
    @PreAuthorize("hasAnyRole('CLIENTE','RECEPCIONISTA','ADMIN')")
    @Operation(summary = "Listar direcciones de un cliente")
    public ResponseEntity<List<DireccionResponse>> listarPorCliente(Authentication auth, @PathVariable Long clienteId) {
        return ResponseEntity.ok(direccionService.listarPorCliente(auth, clienteId));
    }

    @GetMapping("/{clienteId}/direcciones/{direccionId}")
    @PreAuthorize("hasAnyRole('CLIENTE','RECEPCIONISTA','ADMIN')")
    @Operation(summary = "Obtener una dirección de un cliente")
    public ResponseEntity<DireccionResponse> obtenerPorCliente(Authentication auth, @PathVariable Long clienteId,
                                                             @PathVariable Long direccionId) {
        return ResponseEntity.ok(direccionService.obtenerPorCliente(auth, clienteId, direccionId));
    }
}