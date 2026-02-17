package com.facturacion.negocio.domain;

import com.facturacion.negocio.enumerated.TipoMovimientoInventario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_codigo", nullable = false)
    private Producto producto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoMovimientoInventario tipo;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, length = 30)
    private String referenciaTipo;

    @Column(nullable = false)
    private Long referenciaId;

    @Column(nullable = false)
    private LocalDateTime fecha;

    @Column(length = 255)
    private String motivo;

    protected MovimientoInventario() {
    }

    public MovimientoInventario(Producto producto,
                                TipoMovimientoInventario tipo,
                                Integer cantidad,
                                String referenciaTipo,
                                Long referenciaId,
                                String motivo) {
        this.producto = Objects.requireNonNull(producto, "producto requerido");
        this.tipo = Objects.requireNonNull(tipo, "tipo requerido");
        this.referenciaTipo = Objects.requireNonNull(referenciaTipo, "referenciaTipo requerido");
        this.referenciaId = Objects.requireNonNull(referenciaId, "referenciaId requerido");
        if (cantidad == null || cantidad == 0) {
            throw new IllegalArgumentException("cantidad invalida");
        }
        this.cantidad = cantidad;
        this.motivo = motivo;
        this.fecha = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Producto getProducto() {
        return producto;
    }

    public TipoMovimientoInventario getTipo() {
        return tipo;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public String getReferenciaTipo() {
        return referenciaTipo;
    }

    public Long getReferenciaId() {
        return referenciaId;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public String getMotivo() {
        return motivo;
    }
}
