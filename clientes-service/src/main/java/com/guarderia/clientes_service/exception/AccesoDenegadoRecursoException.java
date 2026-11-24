package com.guarderia.clientes_service.exception;

/**
 * Se lanza cuando un usuario autenticado intenta operar sobre un recurso que no le pertenece
 * y no tiene rol de personal autorizado (RECEPCIONISTA/ADMIN).
 */
public class AccesoDenegadoRecursoException extends RuntimeException {

    public static final String CODIGO = "ACCESO_DENEGADO";

    public AccesoDenegadoRecursoException(String mensaje) {
        super(mensaje);
    }
}