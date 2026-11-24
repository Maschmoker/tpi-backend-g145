-- V2__crear_esquema_clientes.sql
-- Esquema de dominio de clientes-service (Bloque 3B).

CREATE TABLE clientes (
    id            BIGSERIAL PRIMARY KEY,
    keycloak_id   VARCHAR(64)  NOT NULL UNIQUE,
    nombre        VARCHAR(100) NOT NULL,
    apellido      VARCHAR(100) NOT NULL,
    email         VARCHAR(255),
    telefono      VARCHAR(30),
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_clientes_activo ON clientes (activo);

CREATE TABLE direcciones (
    id            BIGSERIAL PRIMARY KEY,
    cliente_id    BIGINT       NOT NULL REFERENCES clientes (id),
    calle         VARCHAR(200) NOT NULL,
    numero        VARCHAR(20),
    barrio        VARCHAR(100),
    piso          VARCHAR(20),
    departamento  VARCHAR(20),
    ciudad        VARCHAR(100) NOT NULL DEFAULT 'Córdoba',
    latitud       DOUBLE PRECISION,
    longitud      DOUBLE PRECISION,
    es_principal  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_direcciones_cliente_id   ON direcciones (cliente_id);
CREATE INDEX idx_direcciones_es_principal ON direcciones (cliente_id, es_principal);

CREATE TABLE mascotas (
    id            BIGSERIAL PRIMARY KEY,
    cliente_id    BIGINT       NOT NULL REFERENCES clientes (id),
    nombre        VARCHAR(100) NOT NULL,
    especie       VARCHAR(30)  NOT NULL,
    raza          VARCHAR(100),
    tamanio       VARCHAR(20)  NOT NULL,
    peso_kg       NUMERIC(5,2),
    observaciones  VARCHAR(1000),
    activa        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_mascotas_especie  CHECK (especie IN ('PERRO','GATO','CONEJO','AVE','REPTIL','ROEDOR','OTRO')),
    CONSTRAINT chk_mascotas_tamanio CHECK (tamanio IN ('PEQUENO','MEDIANO','GRANDE'))
);

CREATE INDEX idx_mascotas_cliente_id ON mascotas (cliente_id);
CREATE INDEX idx_mascotas_activa     ON mascotas (cliente_id, activa);