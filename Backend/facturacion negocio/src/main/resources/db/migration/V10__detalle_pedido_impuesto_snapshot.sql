ALTER TABLE IF EXISTS detalle_pedido
    ADD COLUMN IF NOT EXISTS tipo_impuesto VARCHAR(10),
    ADD COLUMN IF NOT EXISTS impuesto_porcentaje NUMERIC(5,2);

UPDATE detalle_pedido dp
SET tipo_impuesto = COALESCE(p.tipo_impuesto, 'IVA'),
    impuesto_porcentaje = COALESCE(p.impuesto_porcentaje, 0)
FROM producto p
WHERE dp.producto_id = p.id
  AND (dp.tipo_impuesto IS NULL OR dp.impuesto_porcentaje IS NULL);

ALTER TABLE IF EXISTS detalle_pedido
    ALTER COLUMN tipo_impuesto SET NOT NULL,
    ALTER COLUMN impuesto_porcentaje SET NOT NULL;