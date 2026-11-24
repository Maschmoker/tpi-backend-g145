package com.guarderia.clientes_service.controller;

import com.guarderia.clientes_service.dto.MascotaRequest;
import com.guarderia.clientes_service.dto.MascotaResponse;
import com.guarderia.clientes_service.service.MascotaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Mascotas", description = "Gestión de mascotas de clientes")
@SecurityRequirement(name = "bearerAuth")
public class MascotaController {

    private final MascotaService mascotaService;

    public MascotaController(MascotaService mascotaService) {
        this.mascotaService = mascotaService;
    }

    @GetMapping("/me/mascotas")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Listar las mascotas del cliente autenticado")
    public ResponseEntity<List<MascotaResponse>> listarPropias(Authentication auth,
                                                             @RequestParam(name = "activas", required = false) Boolean activas) {
        boolean soloActivas = activas == null || activas;
        return ResponseEntity.ok(mascotaService.listarPropias(auth, soloActivas));
    }

    @PostMapping("/me/mascotas")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Registrar una mascota para el cliente autenticado")
    public ResponseEntity<MascotaResponse> registrarPropia(Authentication auth, @Valid @RequestBody MascotaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mascotaService.registrarPropia(auth, req));
    }

    @GetMapping("/me/mascotas/{mascotaId}")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Obtener una mascota propia")
    public ResponseEntity<MascotaResponse> obtenerPropia(Authentication auth, @PathVariable Long mascotaId) {
        return ResponseEntity.ok(mascotaService.obtenerPropia(auth, mascotaId));
    }

    @PutMapping("/me/mascotas/{mascotaId}")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Actualizar una mascota propia")
    public ResponseEntity<MascotaResponse> actualizarPropia(Authentication auth, @PathVariable Long mascotaId,
                                                          @Valid @RequestBody MascotaRequest req) {
        return ResponseEntity.ok(mascotaService.actualizarPropia(auth, mascotaId, req));
    }

    @PatchMapping("/me/mascotas/{mascotaId}/baja")
    @PreAuthorize("hasRole('CLIENTE')")
    @Operation(summary = "Baja lógica de una mascota (activa=false)")
    public ResponseEntity<MascotaResponse> baja(Authentication auth, @PathVariable Long mascotaId) {
        return ResponseEntity.ok(mascotaService.bajaLogica(auth, mascotaId));
    }

    @GetMapping("/{clienteId}/mascotas")
    @PreAuthorize("hasAnyRole('CLIENTE','RECEPCIONISTA','ADMIN')")
    @Operation(summary = "Listar mascotas de un cliente")
    public ResponseEntity<List<MascotaResponse>> listarPorCliente(Authentication auth, @PathVariable Long clienteId,
                                                                 @RequestParam(name = "activas", required = false) Boolean activas) {
        boolean soloActivas = activas == null || activas;
        return ResponseEntity.ok(mascotaService.listarPorCliente(auth, clienteId, soloActivas));
    }

    @GetMapping("/{clienteId}/mascotas/{mascotaId}")
    @PreAuthorize("hasAnyRole('CLIENTE','RECEPCIONISTA','ADMIN')")
    @Operation(summary = "Obtener una mascota de un cliente")
    public ResponseEntity<MascotaResponse> obtenerPorCliente(Authentication auth, @PathVariable Long clienteId,
                                                            @PathVariable Long mascotaId) {
        return ResponseEntity.ok(mascotaService.obtenerPorCliente(auth, clienteId, mascotaId));
    }
}