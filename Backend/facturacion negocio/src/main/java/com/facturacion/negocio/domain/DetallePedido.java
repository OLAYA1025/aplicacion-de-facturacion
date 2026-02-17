package com.facturacion.negocio.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"pedido_id", "producto_codigo"})
        }
)
public class DetallePedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="producto_codigo", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    @NotNull
    @Min(1)
    private Integer cantidad;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull
    private BigDecimal precioUnitario;

    protected DetallePedido() {}

    public DetallePedido(@NotNull Producto producto, @Min(1) Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        this.producto = Objects.requireNonNull(producto, "producto no puede ser null");
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecio().setScale(2, RoundingMode.HALF_UP);
    }

    public Long getId() { return id; }
    public Producto getProducto() { return producto; }
    public Integer getCantidad() { return cantidad; }

    public Long getPedidoId() {
        return pedido != null ? pedido.getId() : null;
    }

    public void setCantidad(Integer cantidad) {
        if (cantidad == null || cantidad <= 0) throw new IllegalArgumentException("cantidad > 0");
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() { return precioUnitario; }

    public void cambiarPrecioUnitario(BigDecimal precioUnitario) {
        if (precioUnitario == null) throw new IllegalArgumentException("precioUnitario null");
        this.precioUnitario = precioUnitario.setScale(2, RoundingMode.HALF_UP);
    }

    public void asignarPedido(Pedido pedido) {
        this.pedido = pedido;
    }

    public BigDecimal calcularSubtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad))
                .setScale(2, RoundingMode.HALF_UP);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DetallePedido)) return false;
        DetallePedido that = (DetallePedido) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
