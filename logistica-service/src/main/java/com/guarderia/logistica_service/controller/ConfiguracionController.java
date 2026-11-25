package com.guarderia.logistica_service.controller;

import com.guarderia.logistica_service.dto.ConfiguracionDtos;
import com.guarderia.logistica_service.persistence.entity.Configuracion;
import com.guarderia.logistica_service.service.ConfiguracionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logistica")
public class ConfiguracionController {

    private final ConfiguracionService service;

    public ConfiguracionController(ConfiguracionService service) {
        this.service = service;
    }

    @GetMapping("/configuracion")
    @Operation(summary = "Obtener configuración de logística")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ConfiguracionDtos.ConfiguracionResponse> obtener() {
        Configuracion c = service.obtener();
        return ResponseEntity.ok(new ConfiguracionDtos.ConfiguracionResponse(
                c.getId(), c.getGuarderiaLatitud(), c.getGuarderiaLongitud(), c.getGuarderiaDireccion(),
                c.getRadioCoberturaKm(), c.getTarifaPorKm(), c.getCostoMinimoTraslado(),
                c.getCapacidadDiariaTraslados(), c.getRadioAgrupacionKm(), c.getVelocidadPromedioKmh(), c.getUpdatedAt()));
    }

    @PutMapping("/configuracion")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar configuración (ADMIN)")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<ConfiguracionDtos.ConfiguracionResponse> actualizar(@Valid @RequestBody ConfiguracionDtos.ConfiguracionRequest req) {
        Configuracion c = new Configuracion();
        c.setGuarderiaLatitud(req.guarderiaLatitud());
        c.setGuarderiaLongitud(req.guarderiaLongitud());
        c.setGuarderiaDireccion(req.guarderiaDireccion());
        c.setRadioCoberturaKm(req.radioCoberturaKm());
        c.setTarifaPorKm(req.tarifaPorKm());
        c.setCostoMinimoTraslado(req.costoMinimoTraslado());
        c.setCapacidadDiariaTraslados(req.capacidadDiariaTraslados());
        c.setRadioAgrupacionKm(req.radioAgrupacionKm());
        c.setVelocidadPromedioKmh(req.velocidadPromedioKmh());
        Configuracion guardada = service.actualizar(c);
        return ResponseEntity.ok(new ConfiguracionDtos.ConfiguracionResponse(
                guardada.getId(), guardada.getGuarderiaLatitud(), guardada.getGuarderiaLongitud(), guardada.getGuarderiaDireccion(),
                guardada.getRadioCoberturaKm(), guardada.getTarifaPorKm(), guardada.getCostoMinimoTraslado(),
                guardada.getCapacidadDiariaTraslados(), guardada.getRadioAgrupacionKm(), guardada.getVelocidadPromedioKmh(), guardada.getUpdatedAt()));
    }
}