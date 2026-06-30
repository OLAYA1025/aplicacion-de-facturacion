package com.facturacion.negocio.repository;

import java.math.BigDecimal;

public interface IngresosPorMetodoProjection {

    BigDecimal getIngresosTransferencia();

    BigDecimal getIngresosTarjeta();
}
