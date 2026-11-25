-- V2__crear_esquema_logistica.sql
-- Esquema de dominio de logistica-service (Bloque 5B).
-- Referencias a estadias/clientes/direcciones son LÓGICAS (sin FK entre bases).

-- ---------------------------------------------------------------------
-- Configuración de la guardería (fila única operativa)
-- ---------------------------------------------------------------------
CREATE TABLE configuracion (
    id                          BIGSERIAL PRIMARY KEY,
    guarderia_latitud           DOUBLE PRECISION NOT NULL,
    guarderia_longitud         DOUBLE PRECISION NOT NULL,
    guarderia_direccion        VARCHAR(255)     NOT NULL,
    radio_cobertura_km         DOUBLE PRECISION NOT NULL,
    tarifa_por_km              NUMERIC(10,2)    NOT NULL,
    costo_minimo_traslado      NUMERIC(10,2)    NOT NULL,
    capacidad_diaria_traslados  INT              NOT NULL,
    radio_agrupacion_km        DOUBLE PRECISION NOT NULL,
    velocidad_promedio_kmh     DOUBLE PRECISION NOT NULL,
    updated_at                 TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_configuracion_capacidad CHECK (capacidad_diaria_traslados >= 0),
    CONSTRAINT chk_configuracion_tarifa CHECK (tarifa_por_km >= 0),
    CONSTRAINT chk_configuracion_costo_min CHECK (costo_minimo_traslado >= 0),
    CONSTRAINT chk_configuracion_radio_cob CHECK (radio_cobertura_km >= 0),
    CONSTRAINT chk_configuracion_radio_agr CHECK (radio_agrupacion_km >= 0),
    CONSTRAINT chk_configuracion_velocidad CHECK (velocidad_promedio_kmh > 0)
);

-- ---------------------------------------------------------------------
-- Traslados
-- ---------------------------------------------------------------------
CREATE TABLE traslados (
    id                     BIGSERIAL PRIMARY KEY,
    estadia_id             BIGINT          NOT NULL,
    cliente_id             BIGINT          NOT NULL,
    direccion_id           BIGINT          NOT NULL,
    tipo                   VARCHAR(20)     NOT NULL,
    direccion_texto        VARCHAR(500),
    origen_latitud         DOUBLE PRECISION NOT NULL,
    origen_longitud        DOUBLE PRECISION NOT NULL,
    destino_latitud        DOUBLE PRECISION NOT NULL,
    destino_longitud       DOUBLE PRECISION NOT NULL,
    fecha_programada       DATE            NOT NULL,
    estado                 VARCHAR(20)     NOT NULL DEFAULT 'PENDIENTE',
    distancia_km           DOUBLE PRECISION,
    duracion_estimada_min  INT,
    costo_calculado        NUMERIC(10,2),
    estimado               BOOLEAN         NOT NULL DEFAULT FALSE,
    chofer_keycloak_id     VARCHAR(64),
    fecha_inicio_real      TIMESTAMP,
    fecha_fin_real         TIMESTAMP,
    created_at             TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_traslados_tipo CHECK (tipo IN ('RETIRO','DEVOLUCION')),
    CONSTRAINT chk_traslados_estado CHECK (estado IN ('PENDIENTE','PROGRAMADO','EN_CURSO','COMPLETADO','CANCELADO')),
    CONSTRAINT uq_traslados_estadia_tipo UNIQUE (estadia_id, tipo)
);

CREATE INDEX idx_traslados_fecha        ON traslados (fecha_programada);
CREATE INDEX idx_traslados_estado      ON traslados (estado);
CREATE INDEX idx_traslados_tipo        ON traslados (tipo);
CREATE INDEX idx_traslados_estadia     ON traslados (estadia_id);
CREATE INDEX idx_traslados_cliente     ON traslados (cliente_id);

-- ---------------------------------------------------------------------
-- Rutas diarias
-- ---------------------------------------------------------------------
CREATE TABLE rutas_diarias (
    id                   BIGSERIAL PRIMARY KEY,
    fecha                DATE            NOT NULL,
    tipo                 VARCHAR(20)     NOT NULL,
    chofer_keycloak_id   VARCHAR(64),
    estado               VARCHAR(20)     NOT NULL DEFAULT 'PLANIFICADA',
    distancia_total_km   DOUBLE PRECISION,
    duracion_total_min   INT,
    estimada             BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at           TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_rutas_tipo CHECK (tipo IN ('RETIRO','DEVOLUCION')),
    CONSTRAINT chk_rutas_estado CHECK (estado IN ('PLANIFICADA','EN_CURSO','FINALIZADA'))
);

CREATE INDEX idx_rutas_fecha   ON rutas_diarias (fecha);
CREATE INDEX idx_rutas_tipo    ON rutas_diarias (tipo);
CREATE INDEX idx_rutas_chofer  ON rutas_diarias (chofer_keycloak_id);
CREATE INDEX idx_rutas_estado  ON rutas_diarias (estado);

-- ---------------------------------------------------------------------
-- Paradas de ruta
-- ---------------------------------------------------------------------
CREATE TABLE paradas_ruta (
    id                          BIGSERIAL PRIMARY KEY,
    ruta_id                     BIGINT          NOT NULL REFERENCES rutas_diarias (id) ON DELETE CASCADE,
    traslado_id                 BIGINT          NOT NULL REFERENCES traslados (id) ON DELETE RESTRICT,
    orden                       INT             NOT NULL,
    distancia_desde_anterior_km DOUBLE PRECISION,
    duracion_desde_anterior_min INT,
    CONSTRAINT uq_paradas_traslado UNIQUE (traslado_id)
);

CREATE INDEX idx_paradas_ruta_ruta      ON paradas_ruta (ruta_id);
CREATE INDEX idx_paradas_ruta_orden     ON paradas_ruta (ruta_id, orden);
