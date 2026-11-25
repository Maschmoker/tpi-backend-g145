package com.guarderia.logistica_service.controller;

import com.guarderia.logistica_service.dto.TrasladoDtos;
import com.guarderia.logistica_service.service.TrasladoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logistica")
public class TrasladoController {

    private final TrasladoService service;

    public TrasladoController(TrasladoService service) {
        this.service = service;
    }

    @PostMapping("/traslados/cotizar")
    @PreAuthorize("hasAnyRole('CLIENTE','RECEPCIONISTA','ADMIN')")
    @Operation(summary = "Cotizar traslado")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<TrasladoDtos.CotizacionTrasladoResponse> cotizar(@Valid @RequestBody TrasladoDtos.CotizacionTrasladoRequest req) {
        var res = service.cotizar(new TrasladoService.CotizacionRequest(
                req.estadiaId(), req.direccionId(), req.origenLat(), req.origenLng(),
                req.destinoLat(), req.destinoLng(), req.fecha(), req.idaYVuelta()));
        return ResponseEntity.ok(new TrasladoDtos.CotizacionTrasladoResponse(res.costoTraslado(), res.distanciaKm(), res.duracionMin(), res.estimado()));
    }

    @PostMapping("/traslados")
    @PreAuthorize("hasAnyRole('CLIENTE','RECEPCIONISTA','ADMIN')")
    @Operation(summary = "Crear traslado")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<TrasladoDtos.TrasladoResponse> crear(@Valid @RequestBody TrasladoDtos.CrearTrasladoRequest req) {
        var res = service.crear(new TrasladoService.CrearRequest(
                req.estadiaId(), req.clienteId(), req.tipo(), req.direccionId(),
                req.latitud(), req.longitud(), req.fechaProgramada(), req.direccionTexto()));
        return ResponseEntity.ok(new TrasladoDtos.TrasladoResponse(res.id(), res.estado()));
    }

    @PatchMapping("/traslados/{id}/habilitar")
    @PreAuthorize("hasAnyRole('RECEPCIONISTA','ADMIN')")
    @Operation(summary = "Habilitar traslado por id")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> habilitarPorId(@PathVariable Long id) {
        service.habilitarPorId(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/traslados/por-estadia/{estadiaId}/habilitar")
    @PreAuthorize("hasAnyRole('CLIENTE','RECEPCIONISTA','ADMIN')")
    @Operation(summary = "Habilitar traslados por estadía")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> habilitarPorEstadia(@PathVariable Long estadiaId) {
        service.habilitarPorEstadia(estadiaId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/traslados/cancelar-por-estadia/{estadiaId}")
    @PreAuthorize("hasAnyRole('CLIENTE','RECEPCIONISTA','ADMIN')")
    @Operation(summary = "Cancelar traslados por estadía")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<Void> cancelarPorEstadia(@PathVariable Long estadiaId) {
        service.cancelarPorEstadia(estadiaId);
        return ResponseEntity.noContent().build();
    }
}