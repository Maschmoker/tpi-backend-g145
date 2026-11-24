package com.guarderia.clientes_service.exception;

/**
 * Se lanza cuando un recurso de dominio (cliente, dirección, mascota) no existe.
 */
public class RecursoNoEncontradoException extends RuntimeException {

    public static final String CODIGO = "RECURSO_NO_ENCONTRADO";

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}