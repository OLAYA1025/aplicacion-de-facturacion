package com.facturacion.negocio.repository;

import java.math.BigDecimal;

public interface PedidoTotalProjection {

    Long getPedidoId();

    BigDecimal getTotal();
}
