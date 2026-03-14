package com.facturacion.negocio.domain;

import com.facturacion.negocio.enumerated.TipoMovimiento;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
public class MovimientoCaja {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private BigDecimal monto;

    @Enumerated(EnumType.STRING)
    private TipoMovimiento tipo;

    private String motivo;
    private Instant fecha;

    @ManyToOne(optional = false)
    private Usuario usuario;

    @ManyToOne(optional = false)
    private Caja caja;

    protected MovimientoCaja() {
    }

    public MovimientoCaja(BigDecimal monto, TipoMovimiento tipo, String motivo, Usuario usuario, Caja caja) {
        if (!usuario.isActivo()){
            throw new IllegalArgumentException("El usuario tiene que estar activo");
        }

        this.monto = monto;
        this.tipo = tipo;
        this.motivo = motivo;
        this.fecha = Instant.now();
        this.usuario = usuario;
        this.caja = caja;
    }

    public Long getId() {
        return id;
    }


    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimiento tipo) {
        this.tipo = tipo;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public Caja getCaja() {
        return caja;
    }

    public void setCaja(Caja caja) {
        this.caja = caja;
    }
}
