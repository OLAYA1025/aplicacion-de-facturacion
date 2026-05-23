ALTER TABLE IF EXISTS producto
    ADD COLUMN IF NOT EXISTS codigo VARCHAR(100);

UPDATE producto
SET codigo = NULL
WHERE codigo IS NOT NULL
  AND btrim(codigo) = '';

UPDATE producto
SET codigo = btrim(codigo)
WHERE codigo IS NOT NULL;

ALTER TABLE IF EXISTS producto
    DROP CONSTRAINT IF EXISTS uk_producto_codigo_negocio;

DROP INDEX IF EXISTS uk_producto_codigo_negocio;

CREATE UNIQUE INDEX IF NOT EXISTS uk_producto_codigo_negocio
    ON producto (negocio_id, codigo)
    WHERE codigo IS NOT NULL;
