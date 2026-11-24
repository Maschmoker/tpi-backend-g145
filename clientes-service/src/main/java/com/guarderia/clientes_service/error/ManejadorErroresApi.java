package com.guarderia.clientes_service.error;

import com.guarderia.commons.error.ManejadorErroresBase;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Manejador de errores del servicio. Hereda de {@link ManejadorErroresBase} los
 * handlers técnicos (400/405/415) y el contrato {@code ApiError} con correlationId.
 *
 * <p>En este bloque queda intencionalmente sin handlers propios. El handler genérico
 * de 500 y los errores de dominio (404/409/403) se agregan cuando existan las capas
 * de aplicación correspondientes: un {@code @ExceptionHandler(Exception.class)}
 * prematuro convertiría en 500 los 404 de ruta inexistente
 * ({@code NoResourceFoundException}) y los {@code ResponseStatusException} de Spring.</p>
 */
@RestControllerAdvice
public class ManejadorErroresApi extends ManejadorErroresBase {
}
