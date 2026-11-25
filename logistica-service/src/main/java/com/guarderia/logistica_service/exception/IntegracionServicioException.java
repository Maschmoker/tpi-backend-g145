package com.guarderia.logistica_service.exception;

public class IntegracionServicioException extends RuntimeException {
    public IntegracionServicioException(String mensaje, Throwable cause) { super(mensaje, cause); }
    public IntegracionServicioException(String mensaje) { super(mensaje); }
}