package com.guarderia.estadias_service.controller;

import com.guarderia.estadias_service.domain.TipoServicio;
import com.guarderia.estadias_service.dto.request.ServicioCatalogoRequest;
import com.guarderia.estadias_service.dto.response.ServicioCatalogoResponse;
import com.guarderia.estadias_service.persistence.entity.ServicioCatalogo;
import com.guarderia.estadias_service.service.CatalogoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/estadias/catalogo")
@Tag(name = "Catálogo de servicios")
@SecurityRequirement(name = "bearerAuth")
public class CatalogoController {

    private final CatalogoService catalogoService;

    public CatalogoController(CatalogoService catalogoService) {
        this.catalogoService = catalogoService;
    }

    @GetMapping
    @Operation(summary = "Listar servicios activos del catálogo")
    public ResponseEntity<List<ServicioCatalogoResponse>> listar(
            @RequestParam(required = false) String especie,
            @RequestParam(required = false) TipoServicio tipo) {
        List<ServicioCatalogoResponse> resp = catalogoService.listarActivos(especie, tipo).stream()
                .map(com.guarderia.estadias_service.dto.mapper.ServicioCatalogoMapper::toResponse)
                .toList();
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener un servicio del catálogo")
    public ResponseEntity<ServicioCatalogoResponse> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(com.guarderia.estadias_service.dto.mapper.ServicioCatalogoMapper.toResponse(catalogoService.obtenerPorId(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear un servicio del catálogo")
    public ResponseEntity<ServicioCatalogoResponse> crear(@Valid @RequestBody ServicioCatalogoRequest req) {
        ServicioCatalogo creado = catalogoService.crear(com.guarderia.estadias_service.dto.mapper.ServicioCatalogoMapper.toEntity(req));
        return ResponseEntity.status(HttpStatus.CREATED).body(com.guarderia.estadias_service.dto.mapper.ServicioCatalogoMapper.toResponse(creado));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar un servicio del catálogo")
    public ResponseEntity<ServicioCatalogoResponse> actualizar(@PathVariable Long id, @Valid @RequestBody ServicioCatalogoRequest req) {
        ServicioCatalogo actualizado = catalogoService.actualizar(id, com.guarderia.estadias_service.dto.mapper.ServicioCatalogoMapper.toEntity(req));
        return ResponseEntity.ok(com.guarderia.estadias_service.dto.mapper.ServicioCatalogoMapper.toResponse(actualizado));
    }

    @PatchMapping("/{id}/activo")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar/desactivar un servicio del catálogo")
    public ResponseEntity<ServicioCatalogoResponse> cambiarActivo(@PathVariable Long id, @RequestParam boolean activo) {
        ServicioCatalogo actualizado = catalogoService.cambiarActivo(id, activo);
        return ResponseEntity.ok(com.guarderia.estadias_service.dto.mapper.ServicioCatalogoMapper.toResponse(actualizado));
    }
}