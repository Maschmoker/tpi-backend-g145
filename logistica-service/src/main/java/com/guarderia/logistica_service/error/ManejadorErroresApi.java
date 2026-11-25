package com.guarderia.logistica_service.error;

import com.guarderia.commons.error.ApiError;
import com.guarderia.commons.error.ApiErrorFactory;
import com.guarderia.commons.error.ManejadorErroresBase;
import com.guarderia.logistica_service.exception.RecursoNoEncontradoException;
import com.guarderia.logistica_service.exception.ReglaNegocioException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorErroresApi extends ManejadorErroresBase {

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<ApiError> manejarNoEncontrado(RecursoNoEncontradoException ex, HttpServletRequest request) {
        return respuesta(HttpStatus.NOT_FOUND, "RECURSO_NO_ENCONTRADO", ex.getMessage(), request);
    }

    @ExceptionHandler(ReglaNegocioException.class)
    public ResponseEntity<ApiError> manejarRegla(ReglaNegocioException ex, HttpServletRequest request) {
        return respuesta(ex.getEstado(), ex.getCodigo(), ex.getMessage(), request);
    }

    @ExceptionHandler(org.springframework.security.authorization.AuthorizationDeniedException.class)
    public ResponseEntity<ApiError> manejarAutorizacionDenegada(
            org.springframework.security.authorization.AuthorizationDeniedException ex,
            HttpServletRequest request) {
        return respuesta(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO",
                "No tiene permisos para realizar esta operación", request);
    }

    @ExceptionHandler(org.springframework.security.access.AccessDeniedException.class)
    public ResponseEntity<ApiError> manejarAccessDenied(
            org.springframework.security.access.AccessDeniedException ex,
            HttpServletRequest request) {
        return respuesta(HttpStatus.FORBIDDEN, "ACCESO_DENEGADO",
                "No tiene permisos para realizar esta operación", request);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> manejarGenerico(Exception ex, HttpServletRequest request) {
        return respuesta(HttpStatus.INTERNAL_SERVER_ERROR,
                ApiErrorFactory.CODIGO_ERROR_INTERNO, "Ocurrió un error inesperado", request);
    }
}
