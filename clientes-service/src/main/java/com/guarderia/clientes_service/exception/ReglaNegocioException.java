package com.guarderia.clientes_service.exception;

import org.springframework.http.HttpStatus;

/**
 * Se lanza cuando se viola una regla de negocio del servicio. El {@link HttpStatus} asociado
 * permite mapear a 400 (BAD_REQUEST) o 409 (CONFLICT) según el caso.
 */
public class ReglaNegocioException extends RuntimeException {

    private final HttpStatus estado;
    private final String codigo;

    public ReglaNegocioException(HttpStatus estado, String codigo, String mensaje) {
        super(mensaje);
        this.estado = estado;
        this.codigo = codigo;
    }

    public HttpStatus getEstado() {
        return estado;
    }

    public String getCodigo() {
        return codigo;
    }
}