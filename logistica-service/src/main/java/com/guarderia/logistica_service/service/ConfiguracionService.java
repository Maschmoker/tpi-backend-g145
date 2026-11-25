package com.guarderia.logistica_service.service;

import com.guarderia.logistica_service.exception.RecursoNoEncontradoException;
import com.guarderia.logistica_service.persistence.entity.Configuracion;
import com.guarderia.logistica_service.persistence.repository.ConfiguracionRepository;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConfiguracionService {

    private final ConfiguracionRepository repository;

    public ConfiguracionService(ConfiguracionRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Configuracion obtener() {
        return repository.findTopByOrderByIdAsc()
                .orElseThrow(() -> new RecursoNoEncontradoException("Configuración no encontrada"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Configuracion actualizar(Configuracion datos) {
        Configuracion actual = obtener();
        actual.setGuarderiaLatitud(datos.getGuarderiaLatitud());
        actual.setGuarderiaLongitud(datos.getGuarderiaLongitud());
        actual.setGuarderiaDireccion(datos.getGuarderiaDireccion());
        actual.setRadioCoberturaKm(datos.getRadioCoberturaKm());
        actual.setTarifaPorKm(datos.getTarifaPorKm());
        actual.setCostoMinimoTraslado(datos.getCostoMinimoTraslado());
        actual.setCapacidadDiariaTraslados(datos.getCapacidadDiariaTraslados());
        actual.setRadioAgrupacionKm(datos.getRadioAgrupacionKm());
        actual.setVelocidadPromedioKmh(datos.getVelocidadPromedioKmh());
        return repository.save(actual);
    }

    public double[] coordenadasGuarderia() {
        Configuracion c = obtener();
        return new double[]{c.getGuarderiaLatitud(), c.getGuarderiaLongitud()};
    }
}