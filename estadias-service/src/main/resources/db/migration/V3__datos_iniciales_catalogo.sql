-- V3__datos_iniciales_catalogo.sql
-- Servicios base del catálogo para probar cotizaciones.

INSERT INTO servicios_catalogo
    (nombre, descripcion, tipo_servicio, precio_pequeno, precio_mediano, precio_grande, especies_aplicables, activo)
VALUES
    ('Alojamiento diario',
     'Estadía diaria con alimentación y cuidado básico.',
     'ALOJAMIENTO', 1500.00, 2000.00, 2500.00, 'PERRO,GATO,CONEJO,ROEDOR', TRUE),

    ('Baño y cepillado',
     'Baño completo y cepillado según tamaño.',
     'HIGIENE', 800.00, 1000.00, 1200.00, 'PERRO,GATO', TRUE),

    ('Paseo recreativo',
     'Paseo diario de 30 minutos.',
     'PASEO', 500.00, 700.00, 900.00, 'PERRO', TRUE),

    ('Control veterinario básico',
     'Revisión general y control de salud durante la estadía.',
     'VETERINARIA', 1200.00, 1500.00, 1800.00, 'PERRO,GATO,CONEJO,AVE,REPTIL,ROEDOR', TRUE);
