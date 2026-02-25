package com.facturacion.negocio.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class HistorialCajaCerradaDTO {

    private Long cajaId;
    private String usuarioAperturaNombre;
    private String usuarioCierreNombre;
    private LocalDateTime horaApertura;
    private LocalDateTime horaCierre;
    private LocalDate fecha;
    private BigDecimal utilidad;

    public HistorialCajaCerradaDTO(Long cajaId,
                                   String usuarioAperturaNombre,
                                   String usuarioCierreNombre,
                                   LocalDateTime horaApertura,
                                   LocalDateTime horaCierre,
                                   LocalDate fecha,
                                   BigDecimal utilidad) {
        this.cajaId = cajaId;
        this.usuarioAperturaNombre = usuarioAperturaNombre;
        this.usuarioCierreNombre = usuarioCierreNombre;
        this.horaApertura = horaApertura;
        this.horaCierre = horaCierre;
        this.fecha = fecha;
        this.utilidad = utilidad;
    }

    public Long getCajaId() {
        return cajaId;
    }

    public void setCajaId(Long cajaId) {
        this.cajaId = cajaId;
    }

    public String getUsuarioAperturaNombre() {
        return usuarioAperturaNombre;
    }

    public void setUsuarioAperturaNombre(String usuarioAperturaNombre) {
        this.usuarioAperturaNombre = usuarioAperturaNombre;
    }

    public String getUsuarioCierreNombre() {
        return usuarioCierreNombre;
    }

    public void setUsuarioCierreNombre(String usuarioCierreNombre) {
        this.usuarioCierreNombre = usuarioCierreNombre;
    }

    public LocalDateTime getHoraApertura() {
        return horaApertura;
    }

    public void setHoraApertura(LocalDateTime horaApertura) {
        this.horaApertura = horaApertura;
    }

    public LocalDateTime getHoraCierre() {
        return horaCierre;
    }

    public void setHoraCierre(LocalDateTime horaCierre) {
        this.horaCierre = horaCierre;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public BigDecimal getUtilidad() {
        return utilidad;
    }

    public void setUtilidad(BigDecimal utilidad) {
        this.utilidad = utilidad;
    }
}
