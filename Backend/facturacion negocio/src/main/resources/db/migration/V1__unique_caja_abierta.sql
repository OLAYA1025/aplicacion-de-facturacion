CREATE UNIQUE INDEX unica_caja_abierta
    ON caja  (estado)
    WHERE estado = 'ABIERTA';
