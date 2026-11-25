package com.guarderia.estadias_service.service;

import com.guarderia.estadias_service.domain.EstadoEstadia;
import com.guarderia.estadias_service.exception.ReglaNegocioException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

@Component
public class EstadiaEstadoService {

    private static final Map<EstadoEstadia, Set<EstadoEstadia>> TRANSICIONES = Map.of(
            EstadoEstadia.SOLICITADA, EnumSet.of(EstadoEstadia.RETIRO_PROGRAMADO, EstadoEstadia.RECIBIDA, EstadoEstadia.CANCELADA),
            EstadoEstadia.RETIRO_PROGRAMADO, EnumSet.of(EstadoEstadia.EN_TRANSITO_IDA, EstadoEstadia.CANCELADA),
            EstadoEstadia.EN_TRANSITO_IDA, EnumSet.of(EstadoEstadia.RECIBIDA),
            EstadoEstadia.RECIBIDA, EnumSet.of(EstadoEstadia.EN_CUIDADO),
            EstadoEstadia.EN_CUIDADO, EnumSet.of(EstadoEstadia.LISTA_PARA_RETIRO),
            EstadoEstadia.LISTA_PARA_RETIRO, EnumSet.of(EstadoEstadia.EN_TRANSITO_VUELTA, EstadoEstadia.ENTREGADA),
            EstadoEstadia.EN_TRANSITO_VUELTA, EnumSet.of(EstadoEstadia.ENTREGADA),
            EstadoEstadia.CANCELADA, EnumSet.noneOf(EstadoEstadia.class),
            EstadoEstadia.ENTREGADA, EnumSet.noneOf(EstadoEstadia.class)
    );

    public void validarTransicion(EstadoEstadia actual, EstadoEstadia nuevo) {
        Set<EstadoEstadia> permitidas = TRANSICIONES.getOrDefault(actual, Set.of());
        if (!permitidas.contains(nuevo)) {
            throw new ReglaNegocioException(HttpStatus.CONFLICT, "TRANSICION_INVALIDA",
                    "Transición de estado inválida: " + actual + " -> " + nuevo);
        }
    }
}