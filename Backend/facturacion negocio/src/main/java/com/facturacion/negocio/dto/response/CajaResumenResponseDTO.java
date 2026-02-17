package com.facturacion.negocio.dto.response;

import java.math.BigDecimal;

public class CajaResumenResponseDTO {
    private BigDecimal montoInicial;
    private BigDecimal ingresos;
    private BigDecimal egresos;
    private BigDecimal retiros;
    private BigDecimal saldo;

    public CajaResumenResponseDTO(BigDecimal montoInicial, BigDecimal ingresos, BigDecimal egresos, BigDecimal retiros) {
        this.montoInicial = montoInicial;
        this.ingresos = ingresos;
        this.egresos = egresos;
        this.retiros = retiros;
        this.saldo = ingresos.add(montoInicial).subtract(egresos).subtract(retiros);
    }

    public BigDecimal getMontoInicial() {
        return montoInicial;
    }

    public void setMontoInicial(BigDecimal montoInicial) {
        this.montoInicial = montoInicial;
    }

    public BigDecimal getIngresos() {
        return ingresos;
    }

    public void setIngresos(BigDecimal ingresos) {
        this.ingresos = ingresos;
    }

    public BigDecimal getEgresos() {
        return egresos;
    }

    public void setEgresos(BigDecimal egresos) {
        this.egresos = egresos;
    }

    public BigDecimal getRetiros() {
        return retiros;
    }

    public void setRetiros(BigDecimal retiros) {
        this.retiros = retiros;
    }

    public BigDecimal getSaldo() {
        return saldo;
    }

    public void setSaldo(BigDecimal saldo) {
        this.saldo = saldo;
    }
}
