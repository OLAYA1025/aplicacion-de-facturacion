package com.facturacion.negocio.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_compra_detalle_negocio_compra_producto",
                        columnNames = {"negocio_id", "compra_id", "producto_id"})
        }
)
public class CompraDetalle extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "compra_id", nullable = false)
    private Compra compra;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    private Integer cantidad;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoUnitario;

    protected CompraDetalle() {
    }

    public CompraDetalle(Producto producto, Integer cantidad, BigDecimal costoUnitario) {
        this.producto = Objects.requireNonNull(producto, "producto requerido");
        heredarNegocio(producto);
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("cantidad invalida");
        }
        if (costoUnitario == null || costoUnitario.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("costoUnitario invalido");
        }
        this.cantidad = cantidad;
        this.costoUnitario = costoUnitario.setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() {
        return id;
    }

    public Producto getProducto() {
        return producto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public BigDecimal calcularSubtotal() {
        return this.costoUnitario.multiply(BigDecimal.valueOf(this.cantidad)).setScale(2, RoundingMode.HALF_UP);
    }

    public void asignarCompra(Compra compra) {
        this.compra = compra;
        heredarNegocio(compra);
    }
}
