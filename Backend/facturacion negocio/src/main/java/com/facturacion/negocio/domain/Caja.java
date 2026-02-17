package com.facturacion.negocio.domain;

import com.facturacion.negocio.enumerated.EstadoCaja;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
public class Caja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDateTime fechaApertura;

    private LocalDateTime fechaCierre;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal montoInicial;

    @Column(precision = 10, scale = 2)
    private BigDecimal montoFinal;

    @ManyToOne(optional = false)
    private Usuario usuarioApertura;

    @Column(precision = 10, scale = 2)
    private BigDecimal Utilidad;

    @Enumerated(EnumType.STRING)
    private EstadoCaja estado;

    @ManyToOne
    private Usuario usuarioCierre;

    public Caja() {
    }


    public Caja(BigDecimal montoInicial, Usuario usuarioApertura) {
        this.montoInicial = montoInicial;
        this.estado = EstadoCaja.ABIERTA;
        this.usuarioApertura = usuarioApertura;
        this.fechaApertura = LocalDateTime.now();
    }
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Usuario getUsuarioCierre() {
        return usuarioCierre;
    }

    public Usuario getUsuarioApertura() {
        return usuarioApertura;
    }

    public BigDecimal getMontoFinal() {
        return montoFinal;
    }

    public void setMontoFinal(BigDecimal montoFinal) {
        this.montoFinal = montoFinal;
    }

    public BigDecimal getMontoInicial() {
        return montoInicial;
    }

    public void setMontoInicial(BigDecimal montoInicial) {
        this.montoInicial = montoInicial;
    }

    public LocalDateTime getFechaCierre() {
        return fechaCierre;
    }

    public void setFechaCierre(LocalDateTime fechaCierre) {
        this.fechaCierre = fechaCierre;
    }

    public LocalDateTime getFechaApertura() {
        return fechaApertura;
    }

    public void setFechaApertura(LocalDateTime fechaApertura) {
        this.fechaApertura = fechaApertura;
    }

    public Boolean isAbierta() {
        return this.estado == EstadoCaja.ABIERTA;
    }

    public void cerrarCaja(Usuario usuarioCierre, BigDecimal utilidad, BigDecimal montoFinal) {
        this.usuarioCierre = usuarioCierre;
        this.Utilidad = utilidad;
        this.estado = EstadoCaja.CERRADA;
        this.montoFinal = montoFinal;
        fechaCierre = LocalDateTime.now();
    }

    public EstadoCaja getEstado() {
        return estado;
    }

    public void setEstado(EstadoCaja estado) {
        this.estado = estado;
    }

    public BigDecimal getUtilidad() {
        return Utilidad;
    }
}

