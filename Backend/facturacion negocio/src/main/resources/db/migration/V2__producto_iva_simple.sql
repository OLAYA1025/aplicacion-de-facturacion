ALTER TABLE producto
    ADD COLUMN IF NOT EXISTS iva_porcentaje NUMERIC(5,2);

ALTER TABLE producto
    ADD COLUMN IF NOT EXISTS is_excluded BOOLEAN;

UPDATE producto
SET iva_porcentaje = CASE
    WHEN tipo_gestion = 'PREPARACION' THEN 19
    WHEN tipo_gestion = 'INVENTARIABLE' THEN COALESCE(iva_porcentaje, 19)
    ELSE COALESCE(iva_porcentaje, 19)
END;

UPDATE producto
SET is_excluded = (COALESCE(iva_porcentaje, 19) = 0);

ALTER TABLE producto
    ALTER COLUMN iva_porcentaje SET DEFAULT 19,
    ALTER COLUMN iva_porcentaje SET NOT NULL;

ALTER TABLE producto
    ALTER COLUMN is_excluded SET DEFAULT FALSE,
    ALTER COLUMN is_excluded SET NOT NULL;
