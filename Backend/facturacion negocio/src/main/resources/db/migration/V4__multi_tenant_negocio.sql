CREATE TABLE IF NOT EXISTS negocio (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(150) NOT NULL,
    nit VARCHAR(30),
    direccion VARCHAR(255),
    telefono VARCHAR(30),
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO negocio (nombre, nit, direccion, telefono, activo)
SELECT 'Negocio Default', NULL, NULL, NULL, TRUE
WHERE NOT EXISTS (SELECT 1 FROM negocio);

ALTER TABLE IF EXISTS usuario ADD COLUMN IF NOT EXISTS negocio_id BIGINT;
ALTER TABLE IF EXISTS categoria ADD COLUMN IF NOT EXISTS negocio_id BIGINT;
ALTER TABLE IF EXISTS producto ADD COLUMN IF NOT EXISTS negocio_id BIGINT;
ALTER TABLE IF EXISTS clientes ADD COLUMN IF NOT EXISTS negocio_id BIGINT;
ALTER TABLE IF EXISTS pedido ADD COLUMN IF NOT EXISTS negocio_id BIGINT;
ALTER TABLE IF EXISTS detalle_pedido ADD COLUMN IF NOT EXISTS negocio_id BIGINT;
ALTER TABLE IF EXISTS detalle_pedido_nota ADD COLUMN IF NOT EXISTS negocio_id BIGINT;
ALTER TABLE IF EXISTS grupo_nota_categoria ADD COLUMN IF NOT EXISTS negocio_id BIGINT;
ALTER TABLE IF EXISTS nota_predefinida ADD COLUMN IF NOT EXISTS negocio_id BIGINT;
ALTER TABLE IF EXISTS caja ADD COLUMN IF NOT EXISTS negocio_id BIGINT;
ALTER TABLE IF EXISTS movimiento_caja ADD COLUMN IF NOT EXISTS negocio_id BIGINT;
ALTER TABLE IF EXISTS proveedor ADD COLUMN IF NOT EXISTS negocio_id BIGINT;
ALTER TABLE IF EXISTS compra ADD COLUMN IF NOT EXISTS negocio_id BIGINT;
ALTER TABLE IF EXISTS compra_detalle ADD COLUMN IF NOT EXISTS negocio_id BIGINT;
ALTER TABLE IF EXISTS movimiento_inventario ADD COLUMN IF NOT EXISTS negocio_id BIGINT;
ALTER TABLE IF EXISTS documentos_fiscales ADD COLUMN IF NOT EXISTS negocio_id BIGINT;
ALTER TABLE IF EXISTS numbering_ranges ADD COLUMN IF NOT EXISTS negocio_id BIGINT;

UPDATE usuario SET negocio_id = (SELECT id FROM negocio ORDER BY id LIMIT 1) WHERE negocio_id IS NULL;
UPDATE categoria SET negocio_id = (SELECT id FROM negocio ORDER BY id LIMIT 1) WHERE negocio_id IS NULL;
UPDATE producto SET negocio_id = (SELECT id FROM negocio ORDER BY id LIMIT 1) WHERE negocio_id IS NULL;
UPDATE clientes SET negocio_id = (SELECT id FROM negocio ORDER BY id LIMIT 1) WHERE negocio_id IS NULL;
UPDATE pedido SET negocio_id = (SELECT id FROM negocio ORDER BY id LIMIT 1) WHERE negocio_id IS NULL;
UPDATE detalle_pedido SET negocio_id = (SELECT id FROM negocio ORDER BY id LIMIT 1) WHERE negocio_id IS NULL;
UPDATE detalle_pedido_nota SET negocio_id = (SELECT id FROM negocio ORDER BY id LIMIT 1) WHERE negocio_id IS NULL;
UPDATE grupo_nota_categoria SET negocio_id = (SELECT id FROM negocio ORDER BY id LIMIT 1) WHERE negocio_id IS NULL;
UPDATE nota_predefinida SET negocio_id = (SELECT id FROM negocio ORDER BY id LIMIT 1) WHERE negocio_id IS NULL;
UPDATE caja SET negocio_id = (SELECT id FROM negocio ORDER BY id LIMIT 1) WHERE negocio_id IS NULL;
UPDATE movimiento_caja SET negocio_id = (SELECT id FROM negocio ORDER BY id LIMIT 1) WHERE negocio_id IS NULL;
UPDATE proveedor SET negocio_id = (SELECT id FROM negocio ORDER BY id LIMIT 1) WHERE negocio_id IS NULL;
UPDATE compra SET negocio_id = (SELECT id FROM negocio ORDER BY id LIMIT 1) WHERE negocio_id IS NULL;
UPDATE compra_detalle SET negocio_id = (SELECT id FROM negocio ORDER BY id LIMIT 1) WHERE negocio_id IS NULL;
UPDATE movimiento_inventario SET negocio_id = (SELECT id FROM negocio ORDER BY id LIMIT 1) WHERE negocio_id IS NULL;
UPDATE documentos_fiscales SET negocio_id = (SELECT id FROM negocio ORDER BY id LIMIT 1) WHERE negocio_id IS NULL;
UPDATE numbering_ranges SET negocio_id = (SELECT id FROM negocio ORDER BY id LIMIT 1) WHERE negocio_id IS NULL;

ALTER TABLE IF EXISTS usuario ALTER COLUMN negocio_id SET NOT NULL;
ALTER TABLE IF EXISTS categoria ALTER COLUMN negocio_id SET NOT NULL;
ALTER TABLE IF EXISTS producto ALTER COLUMN negocio_id SET NOT NULL;
ALTER TABLE IF EXISTS clientes ALTER COLUMN negocio_id SET NOT NULL;
ALTER TABLE IF EXISTS pedido ALTER COLUMN negocio_id SET NOT NULL;
ALTER TABLE IF EXISTS detalle_pedido ALTER COLUMN negocio_id SET NOT NULL;
ALTER TABLE IF EXISTS detalle_pedido_nota ALTER COLUMN negocio_id SET NOT NULL;
ALTER TABLE IF EXISTS grupo_nota_categoria ALTER COLUMN negocio_id SET NOT NULL;
ALTER TABLE IF EXISTS nota_predefinida ALTER COLUMN negocio_id SET NOT NULL;
ALTER TABLE IF EXISTS caja ALTER COLUMN negocio_id SET NOT NULL;
ALTER TABLE IF EXISTS movimiento_caja ALTER COLUMN negocio_id SET NOT NULL;
ALTER TABLE IF EXISTS proveedor ALTER COLUMN negocio_id SET NOT NULL;
ALTER TABLE IF EXISTS compra ALTER COLUMN negocio_id SET NOT NULL;
ALTER TABLE IF EXISTS compra_detalle ALTER COLUMN negocio_id SET NOT NULL;
ALTER TABLE IF EXISTS movimiento_inventario ALTER COLUMN negocio_id SET NOT NULL;
ALTER TABLE IF EXISTS documentos_fiscales ALTER COLUMN negocio_id SET NOT NULL;
ALTER TABLE IF EXISTS numbering_ranges ALTER COLUMN negocio_id SET NOT NULL;

ALTER TABLE IF EXISTS usuario DROP CONSTRAINT IF EXISTS fk_usuario_negocio;
ALTER TABLE IF EXISTS categoria DROP CONSTRAINT IF EXISTS fk_categoria_negocio;
ALTER TABLE IF EXISTS producto DROP CONSTRAINT IF EXISTS fk_producto_negocio;
ALTER TABLE IF EXISTS clientes DROP CONSTRAINT IF EXISTS fk_cliente_negocio;
ALTER TABLE IF EXISTS pedido DROP CONSTRAINT IF EXISTS fk_pedido_negocio;
ALTER TABLE IF EXISTS detalle_pedido DROP CONSTRAINT IF EXISTS fk_detalle_pedido_negocio;
ALTER TABLE IF EXISTS detalle_pedido_nota DROP CONSTRAINT IF EXISTS fk_detalle_pedido_nota_negocio;
ALTER TABLE IF EXISTS grupo_nota_categoria DROP CONSTRAINT IF EXISTS fk_grupo_nota_categoria_negocio;
ALTER TABLE IF EXISTS nota_predefinida DROP CONSTRAINT IF EXISTS fk_nota_predefinida_negocio;
ALTER TABLE IF EXISTS caja DROP CONSTRAINT IF EXISTS fk_caja_negocio;
ALTER TABLE IF EXISTS movimiento_caja DROP CONSTRAINT IF EXISTS fk_movimiento_caja_negocio;
ALTER TABLE IF EXISTS proveedor DROP CONSTRAINT IF EXISTS fk_proveedor_negocio;
ALTER TABLE IF EXISTS compra DROP CONSTRAINT IF EXISTS fk_compra_negocio;
ALTER TABLE IF EXISTS compra_detalle DROP CONSTRAINT IF EXISTS fk_compra_detalle_negocio;
ALTER TABLE IF EXISTS movimiento_inventario DROP CONSTRAINT IF EXISTS fk_movimiento_inventario_negocio;
ALTER TABLE IF EXISTS documentos_fiscales DROP CONSTRAINT IF EXISTS fk_documentos_fiscales_negocio;
ALTER TABLE IF EXISTS numbering_ranges DROP CONSTRAINT IF EXISTS fk_numbering_ranges_negocio;

ALTER TABLE IF EXISTS usuario
    ADD CONSTRAINT fk_usuario_negocio FOREIGN KEY (negocio_id) REFERENCES negocio (id);
ALTER TABLE IF EXISTS categoria
    ADD CONSTRAINT fk_categoria_negocio FOREIGN KEY (negocio_id) REFERENCES negocio (id);
ALTER TABLE IF EXISTS producto
    ADD CONSTRAINT fk_producto_negocio FOREIGN KEY (negocio_id) REFERENCES negocio (id);
ALTER TABLE IF EXISTS clientes
    ADD CONSTRAINT fk_cliente_negocio FOREIGN KEY (negocio_id) REFERENCES negocio (id);
ALTER TABLE IF EXISTS pedido
    ADD CONSTRAINT fk_pedido_negocio FOREIGN KEY (negocio_id) REFERENCES negocio (id);
ALTER TABLE IF EXISTS detalle_pedido
    ADD CONSTRAINT fk_detalle_pedido_negocio FOREIGN KEY (negocio_id) REFERENCES negocio (id);
ALTER TABLE IF EXISTS detalle_pedido_nota
    ADD CONSTRAINT fk_detalle_pedido_nota_negocio FOREIGN KEY (negocio_id) REFERENCES negocio (id);
ALTER TABLE IF EXISTS grupo_nota_categoria
    ADD CONSTRAINT fk_grupo_nota_categoria_negocio FOREIGN KEY (negocio_id) REFERENCES negocio (id);
ALTER TABLE IF EXISTS nota_predefinida
    ADD CONSTRAINT fk_nota_predefinida_negocio FOREIGN KEY (negocio_id) REFERENCES negocio (id);
ALTER TABLE IF EXISTS caja
    ADD CONSTRAINT fk_caja_negocio FOREIGN KEY (negocio_id) REFERENCES negocio (id);
ALTER TABLE IF EXISTS movimiento_caja
    ADD CONSTRAINT fk_movimiento_caja_negocio FOREIGN KEY (negocio_id) REFERENCES negocio (id);
ALTER TABLE IF EXISTS proveedor
    ADD CONSTRAINT fk_proveedor_negocio FOREIGN KEY (negocio_id) REFERENCES negocio (id);
ALTER TABLE IF EXISTS compra
    ADD CONSTRAINT fk_compra_negocio FOREIGN KEY (negocio_id) REFERENCES negocio (id);
ALTER TABLE IF EXISTS compra_detalle
    ADD CONSTRAINT fk_compra_detalle_negocio FOREIGN KEY (negocio_id) REFERENCES negocio (id);
ALTER TABLE IF EXISTS movimiento_inventario
    ADD CONSTRAINT fk_movimiento_inventario_negocio FOREIGN KEY (negocio_id) REFERENCES negocio (id);
ALTER TABLE IF EXISTS documentos_fiscales
    ADD CONSTRAINT fk_documentos_fiscales_negocio FOREIGN KEY (negocio_id) REFERENCES negocio (id);
ALTER TABLE IF EXISTS numbering_ranges
    ADD CONSTRAINT fk_numbering_ranges_negocio FOREIGN KEY (negocio_id) REFERENCES negocio (id);

CREATE INDEX IF NOT EXISTS idx_usuario_negocio_id ON usuario (negocio_id);
CREATE INDEX IF NOT EXISTS idx_categoria_negocio_id ON categoria (negocio_id);
CREATE INDEX IF NOT EXISTS idx_producto_negocio_id ON producto (negocio_id);
CREATE INDEX IF NOT EXISTS idx_cliente_negocio_id ON clientes (negocio_id);
CREATE INDEX IF NOT EXISTS idx_pedido_negocio_id ON pedido (negocio_id);
CREATE INDEX IF NOT EXISTS idx_detalle_pedido_negocio_id ON detalle_pedido (negocio_id);
CREATE INDEX IF NOT EXISTS idx_detalle_pedido_nota_negocio_id ON detalle_pedido_nota (negocio_id);
CREATE INDEX IF NOT EXISTS idx_grupo_nota_categoria_negocio_id ON grupo_nota_categoria (negocio_id);
CREATE INDEX IF NOT EXISTS idx_nota_predefinida_negocio_id ON nota_predefinida (negocio_id);
CREATE INDEX IF NOT EXISTS idx_caja_negocio_id ON caja (negocio_id);
CREATE INDEX IF NOT EXISTS idx_movimiento_caja_negocio_id ON movimiento_caja (negocio_id);
CREATE INDEX IF NOT EXISTS idx_proveedor_negocio_id ON proveedor (negocio_id);
CREATE INDEX IF NOT EXISTS idx_compra_negocio_id ON compra (negocio_id);
CREATE INDEX IF NOT EXISTS idx_compra_detalle_negocio_id ON compra_detalle (negocio_id);
CREATE INDEX IF NOT EXISTS idx_movimiento_inventario_negocio_id ON movimiento_inventario (negocio_id);
CREATE INDEX IF NOT EXISTS idx_documentos_fiscales_negocio_id ON documentos_fiscales (negocio_id);
CREATE INDEX IF NOT EXISTS idx_numbering_ranges_negocio_id ON numbering_ranges (negocio_id);

CREATE OR REPLACE FUNCTION drop_unique_constraints_exact(p_table_name TEXT, p_columns TEXT[])
RETURNS VOID
LANGUAGE plpgsql
AS $$
DECLARE
    r RECORD;
BEGIN
    FOR r IN
        SELECT con.conname
        FROM pg_constraint con
        JOIN pg_class rel ON rel.oid = con.conrelid
        JOIN pg_namespace nsp ON nsp.oid = rel.relnamespace
        WHERE con.contype = 'u'
          AND rel.relname = p_table_name
          AND nsp.nspname = current_schema()
          AND (
                SELECT array_agg(att.attname ORDER BY ord.ordinality)
                FROM unnest(con.conkey) WITH ORDINALITY AS ord(attnum, ordinality)
                JOIN pg_attribute att ON att.attrelid = rel.oid AND att.attnum = ord.attnum
          ) = p_columns
    LOOP
        EXECUTE format('ALTER TABLE %I DROP CONSTRAINT %I', p_table_name, r.conname);
    END LOOP;
END;
$$;

SELECT drop_unique_constraints_exact('usuario', ARRAY['username']);
SELECT drop_unique_constraints_exact('categoria', ARRAY['nombre']);
SELECT drop_unique_constraints_exact('producto', ARRAY['nombre']);
SELECT drop_unique_constraints_exact('clientes', ARRAY['tipo_identificacion', 'numero_identificacion']);
SELECT drop_unique_constraints_exact('proveedor', ARRAY['correo']);
SELECT drop_unique_constraints_exact('compra', ARRAY['proveedor_id', 'numero_factura']);
SELECT drop_unique_constraints_exact('compra_detalle', ARRAY['compra_id', 'producto_id']);
SELECT drop_unique_constraints_exact('grupo_nota_categoria', ARRAY['categoria_id', 'nombre']);
SELECT drop_unique_constraints_exact('nota_predefinida', ARRAY['grupo_id', 'descripcion']);
SELECT drop_unique_constraints_exact('detalle_pedido_nota', ARRAY['detalle_pedido_id', 'nota_predefinida_id']);
SELECT drop_unique_constraints_exact('documentos_fiscales', ARRAY['reference_code']);

DROP FUNCTION IF EXISTS drop_unique_constraints_exact(TEXT, TEXT[]);

ALTER TABLE IF EXISTS usuario DROP CONSTRAINT IF EXISTS uk_usuario_negocio_username;
ALTER TABLE IF EXISTS categoria DROP CONSTRAINT IF EXISTS uk_categoria_negocio_nombre;
ALTER TABLE IF EXISTS producto DROP CONSTRAINT IF EXISTS uk_producto_negocio_nombre;
ALTER TABLE IF EXISTS clientes DROP CONSTRAINT IF EXISTS uk_cliente_negocio_tipo_numero;
ALTER TABLE IF EXISTS proveedor DROP CONSTRAINT IF EXISTS uk_proveedor_negocio_correo;
ALTER TABLE IF EXISTS compra DROP CONSTRAINT IF EXISTS uk_compra_negocio_proveedor_factura;
ALTER TABLE IF EXISTS compra_detalle DROP CONSTRAINT IF EXISTS uk_compra_detalle_negocio_compra_producto;
ALTER TABLE IF EXISTS grupo_nota_categoria DROP CONSTRAINT IF EXISTS uk_grupo_nota_categoria_nombre;
ALTER TABLE IF EXISTS nota_predefinida DROP CONSTRAINT IF EXISTS uk_nota_predefinida_descripcion;
ALTER TABLE IF EXISTS detalle_pedido_nota DROP CONSTRAINT IF EXISTS uk_detalle_pedido_nota_unica;
ALTER TABLE IF EXISTS documentos_fiscales DROP CONSTRAINT IF EXISTS uk_docfiscal_ref;

ALTER TABLE IF EXISTS usuario
    ADD CONSTRAINT uk_usuario_negocio_username UNIQUE (negocio_id, username);
ALTER TABLE IF EXISTS categoria
    ADD CONSTRAINT uk_categoria_negocio_nombre UNIQUE (negocio_id, nombre);
ALTER TABLE IF EXISTS producto
    ADD CONSTRAINT uk_producto_negocio_nombre UNIQUE (negocio_id, nombre);
ALTER TABLE IF EXISTS clientes
    ADD CONSTRAINT uk_cliente_negocio_tipo_numero UNIQUE (negocio_id, tipo_identificacion, numero_identificacion);
ALTER TABLE IF EXISTS proveedor
    ADD CONSTRAINT uk_proveedor_negocio_correo UNIQUE (negocio_id, correo);
ALTER TABLE IF EXISTS compra
    ADD CONSTRAINT uk_compra_negocio_proveedor_factura UNIQUE (negocio_id, proveedor_id, numero_factura);
ALTER TABLE IF EXISTS compra_detalle
    ADD CONSTRAINT uk_compra_detalle_negocio_compra_producto UNIQUE (negocio_id, compra_id, producto_id);
ALTER TABLE IF EXISTS grupo_nota_categoria
    ADD CONSTRAINT uk_grupo_nota_categoria_nombre UNIQUE (negocio_id, categoria_id, nombre);
ALTER TABLE IF EXISTS nota_predefinida
    ADD CONSTRAINT uk_nota_predefinida_descripcion UNIQUE (negocio_id, grupo_id, descripcion);
ALTER TABLE IF EXISTS detalle_pedido_nota
    ADD CONSTRAINT uk_detalle_pedido_nota_unica UNIQUE (negocio_id, detalle_pedido_id, nota_predefinida_id);
ALTER TABLE IF EXISTS documentos_fiscales
    ADD CONSTRAINT uk_docfiscal_ref UNIQUE (negocio_id, reference_code);
