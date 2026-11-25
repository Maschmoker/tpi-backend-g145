package com.guarderia.estadias_service.exception;

public class IntegracionServicioException extends RuntimeException {
    public IntegracionServicioException(String message) {
        super(message);
    }

    public IntegracionServicioException(String message, Throwable cause) {
        super(message, cause);
    }
}