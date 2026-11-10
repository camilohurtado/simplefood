-- SimpleFood (SIGR) - Esquema relacional de referencia (PostgreSQL)
-- La linea base usa almacenamiento en memoria; este esquema define la
-- estructura objetivo para la capa de persistencia.

CREATE TABLE usuario (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    correo      VARCHAR(150) NOT NULL UNIQUE,
    hash_clave  VARCHAR(64)  NOT NULL,
    rol         VARCHAR(20)  NOT NULL CHECK (rol IN ('CLIENTE','MESERO','ADMINISTRADOR'))
);

CREATE TABLE categoria (
    id      BIGSERIAL PRIMARY KEY,
    nombre  VARCHAR(80) NOT NULL
);

CREATE TABLE plato (
    id            BIGSERIAL PRIMARY KEY,
    nombre        VARCHAR(100)  NOT NULL,
    descripcion   VARCHAR(255),
    precio        NUMERIC(12,2) NOT NULL CHECK (precio > 0),
    categoria_id  BIGINT        NOT NULL REFERENCES categoria(id),
    disponible    BOOLEAN       NOT NULL DEFAULT TRUE
);

CREATE TABLE pedido (
    id              BIGSERIAL PRIMARY KEY,
    mesa            INT         NOT NULL CHECK (mesa > 0),
    mesero_id       BIGINT      NOT NULL REFERENCES usuario(id),
    estado          VARCHAR(20) NOT NULL,
    creado_en       TIMESTAMP   NOT NULL,
    actualizado_en  TIMESTAMP   NOT NULL
);

CREATE TABLE item_pedido (
    pedido_id        BIGINT        NOT NULL REFERENCES pedido(id),
    plato_id         BIGINT        NOT NULL REFERENCES plato(id),
    nombre_plato     VARCHAR(100)  NOT NULL,
    cantidad         INT           NOT NULL CHECK (cantidad > 0),
    precio_unitario  NUMERIC(12,2) NOT NULL,
    PRIMARY KEY (pedido_id, plato_id)
);

CREATE TABLE reserva (
    id          BIGSERIAL PRIMARY KEY,
    cliente_id  BIGINT      NOT NULL REFERENCES usuario(id),
    fecha_hora  TIMESTAMP   NOT NULL,
    personas    INT         NOT NULL CHECK (personas BETWEEN 1 AND 12),
    mesa        INT         NOT NULL,
    estado      VARCHAR(20) NOT NULL
);

CREATE TABLE cierre_caja (
    fecha             DATE PRIMARY KEY,
    pedidos           INT           NOT NULL,
    total_ventas      NUMERIC(14,2) NOT NULL,
    efectivo_contado  NUMERIC(14,2) NOT NULL,
    diferencia        NUMERIC(14,2) NOT NULL,
    responsable_id    BIGINT        NOT NULL REFERENCES usuario(id),
    cerrado_en        TIMESTAMP     NOT NULL
);

CREATE INDEX idx_pedido_estado ON pedido(estado);
CREATE INDEX idx_reserva_fecha ON reserva(fecha_hora);
