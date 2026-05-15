CREATE TABLE IF NOT EXISTS grupo_nota_categoria (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    obligatorio BOOLEAN NOT NULL,
    tipo_seleccion VARCHAR(16) NOT NULL,
    categoria_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS nota_predefinida (
    id BIGSERIAL PRIMARY KEY,
    descripcion VARCHAR(180) NOT NULL,
    grupo_id BIGINT NOT NULL
);

CREATE TABLE IF NOT EXISTS detalle_pedido_nota (
    id BIGSERIAL PRIMARY KEY,
    detalle_pedido_id BIGINT NOT NULL,
    nota_predefinida_id BIGINT NOT NULL
);

CREATE UNIQUE INDEX IF NOT EXISTS uk_grupo_nota_categoria_nombre
    ON grupo_nota_categoria (categoria_id, nombre);

CREATE UNIQUE INDEX IF NOT EXISTS uk_nota_predefinida_descripcion
    ON nota_predefinida (grupo_id, descripcion);

CREATE UNIQUE INDEX IF NOT EXISTS uk_detalle_pedido_nota_unica
    ON detalle_pedido_nota (detalle_pedido_id, nota_predefinida_id);

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_grupo_nota_categoria_categoria'
    ) THEN
        ALTER TABLE grupo_nota_categoria
            ADD CONSTRAINT fk_grupo_nota_categoria_categoria
                FOREIGN KEY (categoria_id) REFERENCES categoria (id) ON DELETE CASCADE;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_nota_predefinida_grupo'
    ) THEN
        ALTER TABLE nota_predefinida
            ADD CONSTRAINT fk_nota_predefinida_grupo
                FOREIGN KEY (grupo_id) REFERENCES grupo_nota_categoria (id) ON DELETE CASCADE;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_detalle_pedido_nota_detalle'
    ) THEN
        ALTER TABLE detalle_pedido_nota
            ADD CONSTRAINT fk_detalle_pedido_nota_detalle
                FOREIGN KEY (detalle_pedido_id) REFERENCES detalle_pedido (id) ON DELETE CASCADE;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1
        FROM pg_constraint
        WHERE conname = 'fk_detalle_pedido_nota_nota'
    ) THEN
        ALTER TABLE detalle_pedido_nota
            ADD CONSTRAINT fk_detalle_pedido_nota_nota
                FOREIGN KEY (nota_predefinida_id) REFERENCES nota_predefinida (id) ON DELETE RESTRICT;
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_grupo_nota_categoria_categoria_id
    ON grupo_nota_categoria (categoria_id);

CREATE INDEX IF NOT EXISTS idx_nota_predefinida_grupo_id
    ON nota_predefinida (grupo_id);

CREATE INDEX IF NOT EXISTS idx_detalle_pedido_nota_detalle_id
    ON detalle_pedido_nota (detalle_pedido_id);

CREATE INDEX IF NOT EXISTS idx_detalle_pedido_nota_nota_id
    ON detalle_pedido_nota (nota_predefinida_id);
