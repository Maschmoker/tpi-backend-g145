package com.guarderia.logistica_service.controller;

import com.guarderia.logistica_service.dto.RutaDtos;
import com.guarderia.logistica_service.persistence.entity.RutaDiaria;
import com.guarderia.logistica_service.service.RutaPlanificacionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/logistica")
public class RutaController {

    private final RutaPlanificacionService service;

    public RutaController(RutaPlanificacionService service) {
        this.service = service;
    }

    @PostMapping("/rutas/generar")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPCIONISTA')")
    @Operation(summary = "Generar ruta diaria")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<RutaDtos.RutaResponse> generar(@Valid @RequestBody RutaDtos.GenerarRutaRequest req) {
        RutaDiaria r = service.generar(new RutaPlanificacionService.GenerarRequest(req.fecha(), req.tipo()), null);
        return ResponseEntity.ok(toResponse(r));
    }

    @GetMapping("/rutas")
    @Operation(summary = "Listar rutas")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<List<RutaDtos.RutaResponse>> listar(@RequestParam(required = false) LocalDate fecha,
                                                             @RequestParam(required = false) String tipo) {
        return ResponseEntity.ok(service.listar(fecha, tipo).stream().map(this::toResponse).collect(Collectors.toList()));
    }

    @GetMapping("/rutas/{id}")
    @Operation(summary = "Obtener ruta")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<RutaDtos.RutaResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(service.obtener(id)));
    }

    @PutMapping("/rutas/{id}/iniciar")
    @PreAuthorize("hasAnyRole('CHOFER','ADMIN')")
    @Operation(summary = "Iniciar ruta")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<RutaDtos.RutaResponse> iniciar(@PathVariable Long id, Authentication auth) {
        String token = ((Jwt) auth.getPrincipal()).getTokenValue();
        String chofer = ((Jwt) auth.getPrincipal()).getSubject();
        RutaDiaria r = service.iniciar(id, token, chofer);
        return ResponseEntity.ok(toResponse(r));
    }

    @PutMapping("/rutas/{id}/paradas/{orden}/completar")
    @PreAuthorize("hasAnyRole('CHOFER','ADMIN')")
    @Operation(summary = "Completar parada")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<RutaDtos.RutaResponse> completar(@PathVariable Long id, @PathVariable int orden, Authentication auth) {
        String token = ((Jwt) auth.getPrincipal()).getTokenValue();
        RutaDiaria r = service.completarParada(id, orden, token);
        return ResponseEntity.ok(toResponse(r));
    }

    private RutaDtos.RutaResponse toResponse(RutaDiaria r) {
        List<RutaDtos.ParadaResponse> paradas = r.getParadas().stream()
                .map(p -> new RutaDtos.ParadaResponse(p.getId(), p.getOrden(), p.getTraslado().getId(),
                        p.getDistanciaDesdeAnteriorKm(), p.getDuracionDesdeAnteriorMin()))
                .collect(Collectors.toList());
        return new RutaDtos.RutaResponse(r.getId(), r.getFecha(), r.getTipo().name(), r.getEstado().name(),
                r.getDistanciaTotalKm(), r.getDuracionTotalMin(), r.isEstimada(), r.getChoferKeycloakId(), paradas);
    }
}