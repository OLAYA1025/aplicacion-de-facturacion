package com.facturacion.negocio.domain;

import com.facturacion.negocio.enumerated.EstadoCompra;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"proveedor_id", "numero_factura"})
        }
)
public class Compra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @Column(name = "numero_factura", nullable = false, length = 80)
    private String numeroFactura;

    @Column(nullable = false)
    private LocalDateTime fechaCreacion;

    private LocalDateTime fechaConfirmacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCompra estado;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompraDetalle> detalles = new ArrayList<>();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(length = 255)
    private String observacion;

    protected Compra() {
    }

    public Compra(Proveedor proveedor, String numeroFactura, String observacion) {
        this.proveedor = Objects.requireNonNull(proveedor, "proveedor requerido");
        this.numeroFactura = Objects.requireNonNull(numeroFactura, "numeroFactura requerido");
        this.fechaCreacion = LocalDateTime.now();
        this.estado = EstadoCompra.BORRADOR;
        this.subtotal = BigDecimal.ZERO;
        this.total = BigDecimal.ZERO;
        this.observacion = observacion;
    }

    public Long getId() {
        return id;
    }

    public Proveedor getProveedor() {
        return proveedor;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaConfirmacion() {
        return fechaConfirmacion;
    }

    public EstadoCompra getEstado() {
        return estado;
    }

    public List<CompraDetalle> getDetalles() {
        return Collections.unmodifiableList(detalles);
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getObservacion() {
        return observacion;
    }

    public boolean isBorrador() {
        return this.estado == EstadoCompra.BORRADOR;
    }

    public boolean isConfirmada() {
        return this.estado == EstadoCompra.CONFIRMADA;
    }

    public boolean isAnulada() {
        return this.estado == EstadoCompra.ANULADA;
    }

    public void agregarDetalle(CompraDetalle detalle) {
        if (!isBorrador()) {
            throw new IllegalStateException("Solo se puede editar una compra en BORRADOR");
        }
        Objects.requireNonNull(detalle, "detalle requerido");
        detalle.asignarCompra(this);
        this.detalles.add(detalle);
        recalcularTotales();
    }

    public void confirmar() {
        if (!isBorrador()) {
            throw new IllegalStateException("La compra debe estar en BORRADOR para confirmar");
        }
        this.estado = EstadoCompra.CONFIRMADA;
        this.fechaConfirmacion = LocalDateTime.now();
    }

    public void anular() {
        if (isAnulada()) {
            throw new IllegalStateException("La compra ya esta anulada");
        }
        this.estado = EstadoCompra.ANULADA;
    }

    private void recalcularTotales() {
        BigDecimal nuevoSubtotal = BigDecimal.ZERO;
        for (CompraDetalle detalle : this.detalles) {
            nuevoSubtotal = nuevoSubtotal.add(detalle.calcularSubtotal());
        }
        this.subtotal = nuevoSubtotal;
        this.total = nuevoSubtotal;
    }
}
