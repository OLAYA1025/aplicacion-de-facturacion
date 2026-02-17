package com.facturacion.negocio.dto.response;

import com.facturacion.negocio.domain.Caja;
import com.facturacion.negocio.domain.Usuario;
import com.facturacion.negocio.enumerated.EstadoCaja;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CajaResponseDTO {
    private Long id;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private BigDecimal montoInicial;
    private BigDecimal montoFinal;
    private EstadoCaja estado;
    private Long usuarioAperturaId;
    private Long usuarioCierreId;
    private BigDecimal utilidad;

    public static CajaResponseDTO from(Caja caja) {
        CajaResponseDTO dto = new CajaResponseDTO();
        dto.setId(caja.getId());
        dto.setFechaApertura(caja.getFechaApertura());
        dto.setFechaCierre(caja.getFechaCierre());
        dto.setMontoInicial(caja.getMontoInicial());
        dto.setMontoFinal(caja.getMontoFinal());
        dto.setEstado(caja.getEstado());
        dto.setUtilidad(caja.getUtilidad());

        Usuario usuarioApertura = caja.getUsuarioApertura();
        if (usuarioApertura != null) {
            dto.setUsuarioAperturaId(usuarioApertura.getId());
        }

        Usuario usuarioCierre = caja.getUsuarioCierre();
        if (usuarioCierre != null) {
            dto.setUsuarioCierreId(usuarioCierre.getId());
        }

        return dto;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getUtilidad() {
        return utilidad;
    }

    public void setUtilidad(BigDecimal utilidad) {
        this.utilidad = utilidad;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDateTime fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public BigDecimal getMontoInicial() {
        return montoInicial;
    }

    public void setMontoInicial(BigDecimal montoInicial) {
        this.montoInicial = montoInicial;
    }

    public BigDecimal getMontoFinal() {
        return montoFinal;
    }

    public void setMontoFinal(BigDecimal montoFinal) {
        this.montoFinal = montoFinal;
    }

    public EstadoCaja getEstado() {
        return estado;
    }

    public void setEstado(EstadoCaja estado) {
        this.estado = estado;
    }

    public Long getUsuarioAperturaId() {
        return usuarioAperturaId;
    }

    public void setUsuarioAperturaId(Long usuarioAperturaId) {
        this.usuarioAperturaId = usuarioAperturaId;
    }

    public Long getUsuarioCierreId() {
        return usuarioCierreId;
    }

    public void setUsuarioCierreId(Long usuarioCierreId) {
        this.usuarioCierreId = usuarioCierreId;
    }
}
