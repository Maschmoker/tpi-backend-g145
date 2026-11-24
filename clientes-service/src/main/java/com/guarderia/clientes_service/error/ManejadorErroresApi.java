package com.guarderia.clientes_service.error;

import com.guarderia.clientes_service.exception.AccesoDenegadoRecursoException;
import com.guarderia.clientes_service.exception.RecursoNoEncontradoException;
import com.guarderia.clientes_service.exception.ReglaNegocioException;
import com.guarderia.commons.error.ApiError;
import com.guarderia.commons.error.ApiErrorFactory;
import com.guarderia.commons.error.ManejadorErroresBase;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorErroresApi extends ManejadorErroresBase {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> manejarNoEncontrado(RecursoNoEncontradoException ex,
                                                       HttpServletRequest request) {
        return respuesta(org.springframework.http.HttpStatus.NOT_FOUND,
                RecursoNoEncontradoException.CODIGO, ex.getMessage(), request);
    }

    @ExceptionHandler(AccesoDenegadoRecursoException.class)
    public ResponseEntity<ApiError> manejarAccesoDenegado(AccesoDenegadoRecursoException ex,
                                                          HttpServletRequest request) {
        return respuesta(org.springframework.http.HttpStatus.FORBIDDEN,
                AccesoDenegadoRecursoException.CODIGO, ex.getMessage(), request);
    }

    @ExceptionHandler(org.springframework.security.authorization.AuthorizationDeniedException.class)
    public ResponseEntity<ApiError> manejarAutorizacionDenegada(
            org.springframework.security.authorization.AuthorizationDeniedException ex,
            HttpServletRequest request) {
        return respuesta(org.springframework.http.HttpStatus.FORBIDDEN,
                AccesoDenegadoRecursoException.CODIGO, "No tiene permisos para realizar esta operación", request);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiError> manejarAccessDenied(org.springframework.security.access.AccessDeniedException ex,
                                                       HttpServletRequest request) {
        return respuesta(org.springframework.http.HttpStatus.FORBIDDEN,
                AccesoDenegadoRecursoException.CODIGO, "No tiene permisos para realizar esta operación", request);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiError> manejarReglaNegocio(ReglaNegocioException ex,
                                                        HttpServletRequest request) {
        return respuesta(ex.getEstado(), ex.getCodigo(), ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> manejarGenerico(Exception ex, HttpServletRequest request) {
        return respuesta(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                ApiErrorFactory.CODIGO_ERROR_INTERNO, "Ocurrió un error inesperado", request);
    }
}