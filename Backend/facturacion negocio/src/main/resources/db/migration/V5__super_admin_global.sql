ALTER TABLE IF EXISTS usuario
    ALTER COLUMN negocio_id DROP NOT NULL;

ALTER TABLE IF EXISTS usuario
    DROP CONSTRAINT IF EXISTS ck_usuario_negocio_por_rol;

ALTER TABLE IF EXISTS usuario
    ADD CONSTRAINT ck_usuario_negocio_por_rol
        CHECK (
            (rol = 'SUPER_ADMIN' AND negocio_id IS NULL)
                OR (rol <> 'SUPER_ADMIN' AND negocio_id IS NOT NULL)
            );

CREATE UNIQUE INDEX IF NOT EXISTS uk_usuario_super_admin_unico
    ON usuario (rol)
    WHERE rol = 'SUPER_ADMIN';
