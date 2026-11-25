package com.guarderia.estadias_service.exception;

public class RecursoNoEncontradoException extends RuntimeException {

    public static final String CODIGO = "RECURSO_NO_ENCONTRADO";

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}