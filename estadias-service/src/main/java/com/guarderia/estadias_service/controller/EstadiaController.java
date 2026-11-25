package com.guarderia.estadias_service.controller;

import com.guarderia.estadias_service.domain.ModalidadEstadia;
import com.guarderia.estadias_service.dto.mapper.EstadiaMapper;
import com.guarderia.estadias_service.dto.request.CambiarEstadoRequest;
import com.guarderia.estadias_service.dto.request.CotizarEstadiaRequest;
import com.guarderia.estadias_service.dto.request.CrearEstadiaRequest;
import com.guarderia.estadias_service.dto.response.CotizacionResponse;
import com.guarderia.estadias_service.dto.response.DetalleServicioCotizado;
import com.guarderia.estadias_service.dto.response.EstadiaResponse;
import com.guarderia.estadias_service.persistence.entity.Estadia;
import com.guarderia.estadias_service.persistence.entity.ServicioCatalogo;
import com.guarderia.estadias_service.service.EstadiaService;
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
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/estadias")
@Tag(name = "Estadías")
@SecurityRequirement(name = "bearerAuth")
public class EstadiaController {

    private final EstadiaService estadiaService;

    public EstadiaController(EstadiaService estadiaService) {
        this.estadiaService = estadiaService;
    }

    @PostMapping("/cotizar")
    @PreAuthorize("hasAnyRole('CLIENTE','RECEPCIONISTA','ADMIN')")
    @Operation(summary = "Cotizar una estadía sin persistir")
    public ResponseEntity<CotizacionResponse> cotizar(@Valid @RequestBody CotizarEstadiaRequest req, Authentication auth) {
        EstadiaService.CrearEstadiaInput input = toInput(req.mascotaId(), req.modalidad(), req.requiereRetiro(), req.requiereDevolucion(),
                req.direccionTrasladoId(), req.fechaIngresoPlanificada(), req.fechaEgresoPlanificada(),
                req.serviciosCatalogoIds(), req.observaciones());
        EstadiaService.CotizacionResultado r = estadiaService.cotizar(input, auth);
        CotizacionResponse resp = new CotizacionResponse(r.dias(), r.costoServicios(), r.costoTraslado(), r.costoTotal(), List.of());
        return ResponseEntity.ok(resp);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('CLIENTE','RECEPCIONISTA','ADMIN')")
    @Operation(summary = "Crear una estadía")
    public ResponseEntity<EstadiaResponse> crear(@Valid @RequestBody CrearEstadiaRequest req, Authentication auth) {
        EstadiaService.CrearEstadiaInput input = toInput(req.mascotaId(), req.modalidad(), req.requiereRetiro(), req.requiereDevolucion(),
                req.direccionTrasladoId(), req.fechaIngresoPlanificada(), req.fechaEgresoPlanificada(),
                req.serviciosCatalogoIds(), req.observaciones());
        Estadia creada = estadiaService.crear(input, auth);
        return ResponseEntity.status(HttpStatus.CREATED).body(EstadiaMapper.toResponse(creada));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('CLIENTE','RECEPCIONISTA','ADMIN')")
    @Operation(summary = "Listar estadías")
    public ResponseEntity<List<EstadiaResponse>> listar(Authentication auth) {
        List<EstadiaResponse> resp = estadiaService.listar(auth).stream().map(EstadiaMapper::toResponse).toList();
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CLIENTE','RECEPCIONISTA','ADMIN','PERSONAL_CUIDADO','CHOFER')")
    @Operation(summary = "Obtener una estadía")
    public ResponseEntity<EstadiaResponse> obtener(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(EstadiaMapper.toResponse(estadiaService.obtener(id, auth)));
    }

    @PatchMapping("/{id}/cancelar")
    @PreAuthorize("hasAnyRole('CLIENTE','RECEPCIONISTA','ADMIN')")
    @Operation(summary = "Cancelar una estadía")
    public ResponseEntity<EstadiaResponse> cancelar(@PathVariable Long id, Authentication auth) {
        estadiaService.cancelar(id, auth);
        return ResponseEntity.ok(EstadiaMapper.toResponse(estadiaService.obtener(id, auth)));
    }

    @PatchMapping("/{id}/recibir")
    @PreAuthorize("hasAnyRole('RECEPCIONISTA','ADMIN')")
    @Operation(summary = "Registrar recepción")
    public ResponseEntity<EstadiaResponse> recibir(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(EstadiaMapper.toResponse(estadiaService.recibir(id, auth)));
    }

    @PatchMapping("/{id}/iniciar-cuidado")
    @PreAuthorize("hasAnyRole('RECEPCIONISTA','ADMIN','PERSONAL_CUIDADO')")
    @Operation(summary = "Iniciar cuidado")
    public ResponseEntity<EstadiaResponse> iniciarCuidado(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(EstadiaMapper.toResponse(estadiaService.iniciarCuidado(id, auth)));
    }

    @PatchMapping("/{id}/entregar")
    @PreAuthorize("hasAnyRole('RECEPCIONISTA','ADMIN')")
    @Operation(summary = "Registrar entrega")
    public ResponseEntity<EstadiaResponse> entregar(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(EstadiaMapper.toResponse(estadiaService.entregar(id, auth)));
    }

    @PatchMapping("/{id}/transito-ida")
    @PreAuthorize("hasAnyRole('CHOFER','ADMIN')")
    @Operation(summary = "Marcar tránsito ida")
    public ResponseEntity<EstadiaResponse> transitoIda(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(EstadiaMapper.toResponse(estadiaService.transitoIda(id, auth)));
    }

    @PatchMapping("/{id}/recibida")
    @PreAuthorize("hasAnyRole('CHOFER','ADMIN')")
    @Operation(summary = "Marcar recibida por chofer")
    public ResponseEntity<EstadiaResponse> recibida(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(EstadiaMapper.toResponse(estadiaService.marcarRecibidaPorChofer(id, auth)));
    }

    @PatchMapping("/{id}/transito-vuelta")
    @PreAuthorize("hasAnyRole('CHOFER','ADMIN')")
    @Operation(summary = "Marcar tránsito vuelta")
    public ResponseEntity<EstadiaResponse> transitoVuelta(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(EstadiaMapper.toResponse(estadiaService.transitoVuelta(id, auth)));
    }

    @PatchMapping("/{id}/entregada")
    @PreAuthorize("hasAnyRole('CHOFER','ADMIN')")
    @Operation(summary = "Marcar entregada por chofer")
    public ResponseEntity<EstadiaResponse> entregada(@PathVariable Long id, Authentication auth) {
        return ResponseEntity.ok(EstadiaMapper.toResponse(estadiaService.marcarEntregadaPorChofer(id, auth)));
    }

    private EstadiaService.CrearEstadiaInput toInput(Long mascotaId, ModalidadEstadia modalidad, boolean requiereRetiro,
                                                    boolean requiereDevolucion, Long direccionTrasladoId,
                                                    java.time.LocalDate fechaIngreso, java.time.LocalDate fechaEgreso,
                                                    List<Long> serviciosIds, String observaciones) {
        return new EstadiaService.CrearEstadiaInput(mascotaId, modalidad, requiereRetiro, requiereDevolucion,
                direccionTrasladoId, fechaIngreso, fechaEgreso, serviciosIds, observaciones);
    }

}