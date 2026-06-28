ALTER TABLE IF EXISTS negocio
    ADD COLUMN IF NOT EXISTS electronica_habilitada BOOLEAN NOT NULL DEFAULT FALSE;

CREATE TABLE IF NOT EXISTS configuracion_facturacion_electronica (
    id BIGSERIAL PRIMARY KEY,
    negocio_id BIGINT NOT NULL,
    proveedor VARCHAR(30) NOT NULL,
    base_url VARCHAR(255) NOT NULL,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(1024) NOT NULL,
    client_id VARCHAR(255) NOT NULL,
    client_secret VARCHAR(1024) NOT NULL,
    prefijo_code VARCHAR(80),
    CONSTRAINT fk_config_fe_negocio FOREIGN KEY (negocio_id) REFERENCES negocio (id),
    CONSTRAINT uk_config_fe_negocio UNIQUE (negocio_id)
);

CREATE INDEX IF NOT EXISTS idx_config_fe_negocio_id
    ON configuracion_facturacion_electronica (negocio_id);

ALTER TABLE IF EXISTS numbering_ranges
    ADD COLUMN IF NOT EXISTS configuracion_fe_id BIGINT;

ALTER TABLE IF EXISTS numbering_ranges
    DROP CONSTRAINT IF EXISTS fk_numbering_ranges_config_fe;

ALTER TABLE IF EXISTS numbering_ranges
    ADD CONSTRAINT fk_numbering_ranges_config_fe
        FOREIGN KEY (configuracion_fe_id)
        REFERENCES configuracion_facturacion_electronica (id);

CREATE INDEX IF NOT EXISTS idx_numbering_ranges_configuracion_fe_id
    ON numbering_ranges (configuracion_fe_id);
