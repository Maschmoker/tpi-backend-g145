-- V3__datos_iniciales_configuracion.sql
-- Fila inicial de configuración para la guardería en Córdoba Capital.

INSERT INTO configuracion (
    guarderia_latitud,
    guarderia_longitud,
    guarderia_direccion,
    radio_cobertura_km,
    tarifa_por_km,
    costo_minimo_traslado,
    capacidad_diaria_traslados,
    radio_agrupacion_km,
    velocidad_promedio_kmh
) VALUES (
    -31.4257,
    -64.1818,
    'Obispo Salguero 500, Córdoba Capital, Córdoba, Argentina',
    25.0,
    800.00,
    2500.00,
    15,
    3.0,
    30.0
);
