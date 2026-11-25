-- V2__crear_esquema_estadias.sql
-- Esquema de dominio de estadias-service (Bloque 4B).
-- Referencias a clientes/mascotas/direcciones son LÓGICAS (sin FK entre bases).

-- ---------------------------------------------------------------------
-- Catálogo de servicios
-- ---------------------------------------------------------------------
CREATE TABLE servicios_catalogo (
    id                  BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(100) NOT NULL,
    descripcion         VARCHAR(500),
    tipo_servicio       VARCHAR(30)  NOT NULL,
    precio_pequeno      NUMERIC(10,2) NOT NULL,
    precio_mediano      NUMERIC(10,2) NOT NULL,
    precio_grande       NUMERIC(10,2) NOT NULL,
    especies_aplicables  VARCHAR(255) NOT NULL,
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_servicios_tipo CHECK (tipo_servicio IN ('ALOJAMIENTO','HIGIENE','PASEO','VETERINARIA')),
    CONSTRAINT chk_servicios_precios CHECK (precio_pequeno >= 0 AND precio_mediano >= 0 AND precio_grande >= 0)
);

CREATE INDEX idx_servicios_activo ON servicios_catalogo (activo);
CREATE INDEX idx_servicios_tipo   ON servicios_catalogo (tipo_servicio);

-- ---------------------------------------------------------------------
-- Estadías
-- ---------------------------------------------------------------------
CREATE TABLE estadias (
    id                          BIGSERIAL PRIMARY KEY,
    cliente_id                  BIGINT       NOT NULL,
    cliente_keycloak_id         VARCHAR(64)  NOT NULL,
    mascota_id                  BIGINT       NOT NULL,
    mascota_nombre_snapshot     VARCHAR(100) NOT NULL,
    mascota_especie_snapshot    VARCHAR(30)  NOT NULL,
    mascota_tamanio_snapshot    VARCHAR(20)  NOT NULL,
    modalidad                   VARCHAR(20)  NOT NULL,
    requiere_retiro             BOOLEAN      NOT NULL DEFAULT FALSE,
    requiere_devolucion         BOOLEAN      NOT NULL DEFAULT FALSE,
    direccion_traslado_id       BIGINT,
    estado                      VARCHAR(30)  NOT NULL,
    fecha_ingreso_planificada   DATE         NOT NULL,
    fecha_egreso_planificada    DATE         NOT NULL,
    fecha_recepcion_real        TIMESTAMP,
    fecha_entrega_real          TIMESTAMP,
    costo_servicios             NUMERIC(12,2) NOT NULL DEFAULT 0,
    costo_traslado              NUMERIC(12,2) NOT NULL DEFAULT 0,
    costo_total                 NUMERIC(12,2) NOT NULL DEFAULT 0,
    observaciones               VARCHAR(1000),
    created_at                  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_estadias_modalidad CHECK (modalidad IN ('PRESENCIAL','CON_TRASLADO')),
    CONSTRAINT chk_estadias_estado CHECK (estado IN (
        'SOLICITADA','RETIRO_PROGRAMADO','EN_TRANSITO_IDA','RECIBIDA',
        'EN_CUIDADO','LISTA_PARA_RETIRO','EN_TRANSITO_VUELTA','ENTREGADA','CANCELADA'
    )),
    CONSTRAINT chk_estadias_especie CHECK (mascota_especie_snapshot IN (
        'PERRO','GATO','CONEJO','AVE','REPTIL','ROEDOR','OTRO'
    )),
    CONSTRAINT chk_estadias_tamanio CHECK (mascota_tamanio_snapshot IN ('PEQUENO','MEDIANO','GRANDE')),
    CONSTRAINT chk_estadias_fechas CHECK (fecha_egreso_planificada >= fecha_ingreso_planificada)
);

CREATE INDEX idx_estadias_cliente_id     ON estadias (cliente_id);
CREATE INDEX idx_estadias_cliente_kc    ON estadias (cliente_keycloak_id);
CREATE INDEX idx_estadias_mascota_id    ON estadias (mascota_id);
CREATE INDEX idx_estadias_estado        ON estadias (estado);
CREATE INDEX idx_estadias_fecha_ingreso ON estadias (fecha_ingreso_planificada);

-- ---------------------------------------------------------------------
-- Servicios de cuidado asociados a una estadía
-- ---------------------------------------------------------------------
CREATE TABLE estadia_servicios (
    id                     BIGSERIAL PRIMARY KEY,
    estadia_id             BIGINT       NOT NULL REFERENCES estadias (id) ON DELETE CASCADE,
    servicio_catalogo_id   BIGINT       NOT NULL REFERENCES servicios_catalogo (id),
    precio_aplicado        NUMERIC(10,2) NOT NULL,
    estado                 VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    responsable_keycloak_id VARCHAR(64),
    fecha_inicio           TIMESTAMP,
    fecha_finalizacion     TIMESTAMP,
    created_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_estadia_servicios_estado CHECK (estado IN ('PENDIENTE','EN_CURSO','REALIZADO'))
);

CREATE INDEX idx_estadia_servicios_estadia ON estadia_servicios (estadia_id);
CREATE INDEX idx_estadia_servicios_estado  ON estadia_servicios (estado);

-- ---------------------------------------------------------------------
-- Historial de cambios de estado de la estadía
-- ---------------------------------------------------------------------
CREATE TABLE historial_estados (
    id                    BIGSERIAL PRIMARY KEY,
    estadia_id            BIGINT       NOT NULL REFERENCES estadias (id) ON DELETE CASCADE,
    estado_anterior       VARCHAR(30),
    estado_nuevo          VARCHAR(30)  NOT NULL,
    usuario_keycloak_id   VARCHAR(64)  NOT NULL,
    observacion           VARCHAR(500),
    fecha_cambio          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_historial_estado_anterior CHECK (estado_anterior IS NULL OR estado_anterior IN (
        'SOLICITADA','RETIRO_PROGRAMADO','EN_TRANSITO_IDA','RECIBIDA',
        'EN_CUIDADO','LISTA_PARA_RETIRO','EN_TRANSITO_VUELTA','ENTREGADA','CANCELADA'
    )),
    CONSTRAINT chk_historial_estado_nuevo CHECK (estado_nuevo IN (
        'SOLICITADA','RETIRO_PROGRAMADO','EN_TRANSITO_IDA','RECIBIDA',
        'EN_CUIDADO','LISTA_PARA_RETIRO','EN_TRANSITO_VUELTA','ENTREGADA','CANCELADA'
    ))
);

CREATE INDEX idx_historial_estadia ON historial_estados (estadia_id);
CREATE INDEX idx_historial_fecha   ON historial_estados (fecha_cambio);
