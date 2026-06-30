package com.facturacion.negocio.repository;

import java.math.BigDecimal;

import com.facturacion.negocio.enumerated.TipoMovimiento;

public interface TotalesMovimientoPorTipoProjection {

    TipoMovimiento getTipo();

    BigDecimal getTotal();
}