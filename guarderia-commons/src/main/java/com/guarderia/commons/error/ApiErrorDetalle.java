package com.guarderia.commons.error;

/**
 * Detalle de un error de validación o de entrada.
 *
 * @param campo   nombre del campo o parámetro involucrado; puede ser {@code null} para
 *                errores globales de la solicitud
 * @param mensaje descripción legible del problema
 */
public record ApiErrorDetalle(String campo, String mensaje) {
}
