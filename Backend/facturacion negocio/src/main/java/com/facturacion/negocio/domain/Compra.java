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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_compra_negocio_proveedor_factura",
                        columnNames = {"negocio_id", "proveedor_id", "numero_factura"})
        }
)
public class Compra extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @Column(name = "numero_factura", nullable = false, length = 80)
    private String numeroFactura;

    @Column(nullable = false)
    private Instant fechaCreacion;

    private Instant fechaConfirmacion;

    private Instant fechaAnulacion;

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

    @Column(name = "anulado_por", length = 80)
    private String anuladoPor;

    protected Compra() {
    }

    public Compra(Proveedor proveedor, String numeroFactura, String observacion) {
        this.proveedor = Objects.requireNonNull(proveedor, "proveedor requerido");
        heredarNegocio(proveedor);
        this.numeroFactura = Objects.requireNonNull(numeroFactura, "numeroFactura requerido");
        this.fechaCreacion = Instant.now();
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

    public Instant getFechaCreacion() {
        return fechaCreacion;
    }

    public Instant getFechaConfirmacion() {
        return fechaConfirmacion;
    }

    public Instant getFechaAnulacion() {
        return fechaAnulacion;
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

    public String getAnuladoPor() {
        return anuladoPor;
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
        this.fechaConfirmacion = Instant.now();
    }

    public void anular(String anuladoPor) {
        if (isAnulada()) {
            throw new IllegalStateException("La compra ya esta anulada");
        }
        String usuarioAnulacion = anuladoPor == null ? "" : anuladoPor.trim();
        if (usuarioAnulacion.isEmpty()) {
            throw new IllegalArgumentException("anuladoPor invalido");
        }
        this.estado = EstadoCompra.ANULADA;
        this.anuladoPor = usuarioAnulacion;
        this.fechaAnulacion = Instant.now();
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
