DROP INDEX IF EXISTS unica_caja_abierta;

CREATE UNIQUE INDEX IF NOT EXISTS unica_caja_abierta_por_negocio
    ON caja (negocio_id)
    WHERE estado = 'ABIERTA';
