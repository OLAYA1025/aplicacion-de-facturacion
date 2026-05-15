package com.facturacion.negocio.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DetallePedidoSchemaRepairService {

    private static final Logger log = LoggerFactory.getLogger(DetallePedidoSchemaRepairService.class);

    private final JdbcTemplate jdbcTemplate;

    public DetallePedidoSchemaRepairService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void repararRestriccionUnicaDetallePedido() {
        List<String> constraints = jdbcTemplate.query(
                """
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
                  ) = ARRAY['pedido_id', 'producto_codigo']::text[]
                """,
                (rs, rowNum) -> rs.getString("conname")
        );

        for (String constraintName : constraints) {
            String escapedName = escapeIdentifier(constraintName);
            jdbcTemplate.execute("ALTER TABLE detalle_pedido DROP CONSTRAINT IF EXISTS \"" + escapedName + "\"");
            log.info("Restriccion unica eliminada en detalle_pedido: {}", constraintName);
        }

        List<String> uniqueIndexes = jdbcTemplate.query(
                """
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
                  ) = ARRAY['pedido_id', 'producto_codigo']::text[]
                """,
                (rs, rowNum) -> rs.getString("index_name")
        );

        for (String indexName : uniqueIndexes) {
            String escapedName = escapeIdentifier(indexName);
            jdbcTemplate.execute("DROP INDEX IF EXISTS \"" + escapedName + "\"");
            log.info("Indice unico eliminado en detalle_pedido: {}", indexName);
        }
    }

    private String escapeIdentifier(String identifier) {
        return identifier.replace("\"", "\"\"");
    }
}
