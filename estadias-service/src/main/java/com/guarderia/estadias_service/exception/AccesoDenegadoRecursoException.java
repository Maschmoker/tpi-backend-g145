package com.guarderia.estadias_service.exception;

public class AccesoDenegadoRecursoException extends RuntimeException {

    public static final String CODIGO = "ACCESO_DENEGADO";

    public AccesoDenegadoRecursoException(String mensaje) {
        super(mensaje);
    }
}