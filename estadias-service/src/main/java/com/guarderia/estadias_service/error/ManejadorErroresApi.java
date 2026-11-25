package com.guarderia.estadias_service.error;

import com.guarderia.estadias_service.exception.AccesoDenegadoRecursoException;
import com.guarderia.estadias_service.exception.RecursoNoEncontradoException;
import com.guarderia.estadias_service.exception.IntegracionServicioException;
import com.guarderia.estadias_service.exception.ReglaNegocioException;
import com.guarderia.commons.error.ApiError;
import com.guarderia.commons.error.ApiErrorFactory;
import com.guarderia.commons.error.ManejadorErroresBase;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorErroresApi extends ManejadorErroresBase {

    private static final Logger log = LoggerFactory.getLogger(ManejadorErroresApi.class);

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

    @ExceptionHandler(IntegracionServicioException.class)
    public ResponseEntity<ApiError> manejarIntegracion(IntegracionServicioException ex,
                                                       HttpServletRequest request) {
        return respuesta(org.springframework.http.HttpStatus.BAD_GATEWAY,
                "INTEGRACION_SERVICIO", ex.getMessage(), request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> manejarGenerico(Exception ex, HttpServletRequest request) {
        log.error("Error no controlado en {}", request.getRequestURI(), ex);
        return respuesta(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                ApiErrorFactory.CODIGO_ERROR_INTERNO, "Ocurrió un error inesperado", request);
    }
}