package com.guarderia.estadias_service.service;

import com.guarderia.estadias_service.domain.EstadoEstadia;
import com.guarderia.estadias_service.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EstadiaEstadoServiceTest {

    private final EstadiaEstadoService service = new EstadiaEstadoService();

    @Test
    void transicionValida() {
        assertDoesNotThrow(() -> service.validarTransicion(EstadoEstadia.SOLICITADA, EstadoEstadia.RECIBIDA));
    }

    @Test
    void transicionInvalida() {
        assertThrows(ReglaNegocioException.class, () -> service.validarTransicion(EstadoEstadia.ENTREGADA, EstadoEstadia.SOLICITADA));
    }
}