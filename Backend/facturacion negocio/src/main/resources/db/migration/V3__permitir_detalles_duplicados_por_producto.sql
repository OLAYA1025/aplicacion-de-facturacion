DO $$
DECLARE
    registro RECORD;
BEGIN
    FOR registro IN
        SELECT c.conname
        FROM pg_constraint c
        JOIN pg_class t ON t.oid = c.conrelid
        JOIN pg_namespace n ON n.oid = t.relnamespace
        WHERE n.nspname = current_schema()
          AND t.relname = 'detalle_pedido'
          AND c.contype = 'u'
          AND (
            SELECT array_agg(a.attname::text ORDER BY a.attname::text)
            FROM unnest(c.conkey) AS cols(attnum)
            JOIN pg_attribute a
              ON a.attrelid = t.oid
             AND a.attnum = cols.attnum
          ) = ARRAY['pedido_id', 'producto_id']::text[]
    LOOP
        EXECUTE format('ALTER TABLE detalle_pedido DROP CONSTRAINT IF EXISTS %I', registro.conname);
    END LOOP;
END $$;

DO $$
DECLARE
    registro RECORD;
BEGIN
    FOR registro IN
        SELECT i.relname AS index_name
        FROM pg_class t
        JOIN pg_namespace n ON n.oid = t.relnamespace
        JOIN pg_index idx ON idx.indrelid = t.oid
        JOIN pg_class i ON i.oid = idx.indexrelid
        WHERE n.nspname = current_schema()
          AND t.relname = 'detalle_pedido'
          AND idx.indisunique
          AND idx.indnatts = 2
          AND (
            SELECT array_agg(a.attname::text ORDER BY a.attname::text)
            FROM unnest(idx.indkey) AS cols(attnum)
            JOIN pg_attribute a
              ON a.attrelid = t.oid
             AND a.attnum = cols.attnum
          ) = ARRAY['pedido_id', 'producto_id']::text[]
    LOOP
        EXECUTE format('DROP INDEX IF EXISTS %I', registro.index_name);
    END LOOP;
END $$;
