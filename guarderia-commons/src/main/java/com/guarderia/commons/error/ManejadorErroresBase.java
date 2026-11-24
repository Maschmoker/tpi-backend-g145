package com.guarderia.commons.error;

import com.guarderia.commons.correlacion.CorrelationId;
import com.guarderia.commons.correlacion.CorrelationIdUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.ArrayList;
import java.util.List;

/**
 * Base para el manejo homogéneo de errores técnicos de entrada HTTP en servicios MVC.
 *
 * <p>Se inspira en el patrón de {@code ResponseEntityExceptionHandler}: es una clase
 * <strong>abstracta, sin {@code @RestControllerAdvice}</strong>, de modo que no se registra
 * por escaneo de paquetes. Cada microservicio la extiende y anota su subclase:</p>
 *
 * <pre>{@code
 * @RestControllerAdvice
 * public class ManejadorErroresApi extends ManejadorErroresBase {
 *     // handlers de dominio: 404, 409, 403 y 500 propios del servicio
 * }
 * }</pre>
 *
 * <p>Solo cubre errores técnicos (400/405/415). Los errores de dominio y el {@code 500}
 * quedan bajo responsabilidad de cada servicio, que puede reutilizar
 * {@link #respuesta(HttpStatus, String, String, List, HttpServletRequest)} y
 * {@link ApiErrorFactory#interno(String, String)}.</p>
 */
public abstract class ManejadorErroresBase {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ApiError> manejarCuerpoNoValido(MethodArgumentNotValidException ex,
                                                              HttpServletRequest request) {
        List<ApiErrorDetalle> detalles = new ArrayList<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            detalles.add(new ApiErrorDetalle(error.getField(), mensaje(error.getDefaultMessage())));
        }
        for (ObjectError error : ex.getBindingResult().getGlobalErrors()) {
            detalles.add(new ApiErrorDetalle(error.getObjectName(), mensaje(error.getDefaultMessage())));
        }
        return respuesta(HttpStatus.BAD_REQUEST, ApiErrorFactory.CODIGO_VALIDACION,
                "La solicitud contiene datos inválidos", detalles, request);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    protected ResponseEntity<ApiError> manejarParametrosNoValidos(HandlerMethodValidationException ex,
                                                                   HttpServletRequest request) {
        List<ApiErrorDetalle> detalles = new ArrayList<>();
        for (ParameterValidationResult resultado : ex.getParameterValidationResults()) {
            String campo = resultado.getMethodParameter().getParameterName();
            for (MessageSourceResolvable error : resultado.getResolvableErrors()) {
                detalles.add(new ApiErrorDetalle(campo, mensaje(error.getDefaultMessage())));
            }
        }
        for (MessageSourceResolvable error : ex.getCrossParameterValidationResults()) {
            detalles.add(new ApiErrorDetalle(null, mensaje(error.getDefaultMessage())));
        }
        return respuesta(HttpStatus.BAD_REQUEST, ApiErrorFactory.CODIGO_VALIDACION,
                "La solicitud contiene datos inválidos", detalles, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ApiError> manejarRestricciones(ConstraintViolationException ex,
                                                             HttpServletRequest request) {
        List<ApiErrorDetalle> detalles = new ArrayList<>();
        for (ConstraintViolation<?> violacion : ex.getConstraintViolations()) {
            detalles.add(new ApiErrorDetalle(ultimoNodo(violacion), mensaje(violacion.getMessage())));
        }
        return respuesta(HttpStatus.BAD_REQUEST, ApiErrorFactory.CODIGO_VALIDACION,
                "La solicitud contiene datos inválidos", detalles, request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ApiError> manejarTipoIncorrecto(MethodArgumentTypeMismatchException ex,
                                                              HttpServletRequest request) {
        List<ApiErrorDetalle> detalles =
                List.of(new ApiErrorDetalle(ex.getName(), "tipo de dato inválido"));
        return respuesta(HttpStatus.BAD_REQUEST, ApiErrorFactory.CODIGO_VALIDACION,
                "La solicitud contiene datos inválidos", detalles, request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    protected ResponseEntity<ApiError> manejarCuerpoIlegible(HttpMessageNotReadableException ex,
                                                              HttpServletRequest request) {
        return respuesta(HttpStatus.BAD_REQUEST, ApiErrorFactory.CODIGO_CUERPO_ILEGIBLE,
                "El cuerpo de la solicitud no es válido o no puede leerse", List.of(), request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    protected ResponseEntity<ApiError> manejarParametroFaltante(MissingServletRequestParameterException ex,
                                                                 HttpServletRequest request) {
        List<ApiErrorDetalle> detalles =
                List.of(new ApiErrorDetalle(ex.getParameterName(), "es obligatorio"));
        return respuesta(HttpStatus.BAD_REQUEST, ApiErrorFactory.CODIGO_PARAMETRO_FALTANTE,
                "Falta un parámetro obligatorio", detalles, request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    protected ResponseEntity<ApiError> manejarMetodoNoSoportado(HttpRequestMethodNotSupportedException ex,
                                                                 HttpServletRequest request) {
        return respuesta(HttpStatus.METHOD_NOT_ALLOWED, ApiErrorFactory.CODIGO_METODO_NO_SOPORTADO,
                "El método HTTP no está soportado para esta ruta", List.of(), request);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    protected ResponseEntity<ApiError> manejarMediaTypeNoSoportado(HttpMediaTypeNotSupportedException ex,
                                                                    HttpServletRequest request) {
        return respuesta(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ApiErrorFactory.CODIGO_MEDIA_TYPE_NO_SOPORTADO,
                "El tipo de contenido de la solicitud no está soportado", List.of(), request);
    }

    /**
     * Construye la respuesta estándar incluyendo el {@code correlationId} en el cuerpo y en
     * el encabezado {@value CorrelationId#HEADER}.
     */
    protected ResponseEntity<ApiError> respuesta(HttpStatus estado, String codigo, String mensaje,
                                                 List<ApiErrorDetalle> detalles,
                                                 HttpServletRequest request) {
        String correlationId = CorrelationIdUtils.actual();
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = CorrelationIdUtils.normalizar(request.getHeader(CorrelationId.HEADER));
        }

        ApiError error = ApiErrorFactory.crear(estado, codigo, mensaje, request.getRequestURI(),
                correlationId, detalles);

        return ResponseEntity.status(estado)
                .header(CorrelationId.HEADER, correlationId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(error);
    }

    protected ResponseEntity<ApiError> respuesta(HttpStatus estado, String codigo, String mensaje,
                                                 HttpServletRequest request) {
        return respuesta(estado, codigo, mensaje, List.of(), request);
    }

    private static String ultimoNodo(ConstraintViolation<?> violacion) {
        String ultimo = null;
        for (Path.Node nodo : violacion.getPropertyPath()) {
            ultimo = nodo.getName();
        }
        return ultimo;
    }

    private static String mensaje(String valor) {
        return (valor == null || valor.isBlank()) ? "Valor inválido" : valor;
    }
}
