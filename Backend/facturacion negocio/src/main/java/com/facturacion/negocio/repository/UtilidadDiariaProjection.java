package com.facturacion.negocio.repository;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface UtilidadDiariaProjection {

    LocalDate getDia();

    BigDecimal getTotal();
}