package com.facturacion.negocio.dto.response;

import java.math.BigDecimal;

public class CajaResumenResponseDTO {
    private BigDecimal montoInicial;
    private BigDecimal ingresos;
    private BigDecimal ingresosEfectivo;
    private BigDecimal ingresosTransferencia;
    private BigDecimal ingresosTarjeta;
    private BigDecimal egresos;
    private BigDecimal retiros;
    private BigDecimal saldo;

    public CajaResumenResponseDTO(BigDecimal montoInicial,
                                  BigDecimal ingresos,
                                  BigDecimal ingresosEfectivo,
                                  BigDecimal ingresosTransferencia,
                                  BigDecimal ingresosTarjeta,
                                  BigDecimal egresos,
                                  BigDecimal retiros) {
        this.montoInicial = valorSeguro(montoInicial);
        this.ingresos = valorSeguro(ingresos);
        this.ingresosEfectivo = valorSeguro(ingresosEfectivo);
        this.ingresosTransferencia = valorSeguro(ingresosTransferencia);
        this.ingresosTarjeta = valorSeguro(ingresosTarjeta);
        this.egresos = valorSeguro(egresos);
        this.retiros = valorSeguro(retiros);
        this.saldo = this.ingresos.add(this.montoInicial).subtract(this.egresos).subtract(this.retiros);
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

    public BigDecimal getIngresosEfectivo() {
        return ingresosEfectivo;
    }

    public void setIngresosEfectivo(BigDecimal ingresosEfectivo) {
        this.ingresosEfectivo = ingresosEfectivo;
    }

    public BigDecimal getIngresosTransferencia() {
        return ingresosTransferencia;
    }

    public void setIngresosTransferencia(BigDecimal ingresosTransferencia) {
        this.ingresosTransferencia = ingresosTransferencia;
    }

    public BigDecimal getIngresosTarjeta() {
        return ingresosTarjeta;
    }

    public void setIngresosTarjeta(BigDecimal ingresosTarjeta) {
        this.ingresosTarjeta = ingresosTarjeta;
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

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }
}
