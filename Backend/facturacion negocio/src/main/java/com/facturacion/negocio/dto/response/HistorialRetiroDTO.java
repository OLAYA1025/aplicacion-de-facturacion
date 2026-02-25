package com.facturacion.negocio.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class HistorialRetiroDTO {

    private Long retiroId;
    private Long cajaId;
    private String usuarioNombre;
    private String motivo;
    private LocalDateTime fecha;
    private BigDecimal monto;

    public HistorialRetiroDTO(Long retiroId,
                              Long cajaId,
                              String usuarioNombre,
                              String motivo,
                              LocalDateTime fecha,
                              BigDecimal monto) {
        this.retiroId = retiroId;
        this.cajaId = cajaId;
        this.usuarioNombre = usuarioNombre;
        this.motivo = motivo;
        this.fecha = fecha;
        this.monto = monto;
    }

    public Long getRetiroId() {
        return retiroId;
    }

    public void setRetiroId(Long retiroId) {
        this.retiroId = retiroId;
    }

    public Long getCajaId() {
        return cajaId;
    }

    public void setCajaId(Long cajaId) {
        this.cajaId = cajaId;
    }

    public String getUsuarioNombre() {
        return usuarioNombre;
    }

    public void setUsuarioNombre(String usuarioNombre) {
        this.usuarioNombre = usuarioNombre;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }
}
