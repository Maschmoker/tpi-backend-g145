package com.guarderia.estadias_service.controller;

import com.guarderia.estadias_service.dto.mapper.EstadiaMapper;
import com.guarderia.estadias_service.dto.response.EstadiaServicioResponse;
import com.guarderia.estadias_service.persistence.entity.EstadiaServicio;
import com.guarderia.estadias_service.service.EstadiaCuidadoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/estadias")
@Tag(name = "Servicios de cuidado")
@SecurityRequirement(name = "bearerAuth")
public class EstadiaServicioController {

    private final EstadiaCuidadoService cuidadoService;

    public EstadiaServicioController(EstadiaCuidadoService cuidadoService) {
        this.cuidadoService = cuidadoService;
    }

    @GetMapping("/{id}/servicios")
    @PreAuthorize("hasAnyRole('CLIENTE','RECEPCIONISTA','ADMIN','PERSONAL_CUIDADO','CHOFER')")
    @Operation(summary = "Listar servicios de una estadía")
    public ResponseEntity<List<EstadiaServicioResponse>> listar(@PathVariable Long id, Authentication auth) {
        List<EstadiaServicioResponse> resp = cuidadoService.listar(id, auth).stream()
                .map(EstadiaMapper::toServicioResponse)
                .toList();
        return ResponseEntity.ok(resp);
    }

    @PatchMapping("/{id}/servicios/{itemId}/iniciar")
    @PreAuthorize("hasAnyRole('PERSONAL_CUIDADO','RECEPCIONISTA','ADMIN')")
    @Operation(summary = "Iniciar un servicio de cuidado")
    public ResponseEntity<EstadiaServicioResponse> iniciar(@PathVariable Long id, @PathVariable Long itemId, Authentication auth) {
        EstadiaServicio item = cuidadoService.iniciar(id, itemId, auth);
        return ResponseEntity.ok(EstadiaMapper.toServicioResponse(item));
    }

    @PatchMapping("/{id}/servicios/{itemId}/realizar")
    @PreAuthorize("hasAnyRole('PERSONAL_CUIDADO','RECEPCIONISTA','ADMIN')")
    @Operation(summary = "Marcar un servicio de cuidado como realizado")
    public ResponseEntity<EstadiaServicioResponse> realizar(@PathVariable Long id, @PathVariable Long itemId, Authentication auth) {
        EstadiaServicio item = cuidadoService.realizar(id, itemId, auth);
        return ResponseEntity.ok(EstadiaMapper.toServicioResponse(item));
    }
}