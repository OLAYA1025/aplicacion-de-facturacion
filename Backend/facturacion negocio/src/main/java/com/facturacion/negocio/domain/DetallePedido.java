package com.facturacion.negocio.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.facturacion.negocio.enumerated.TipoImpuesto;

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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
public class DetallePedido extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="pedido_id", nullable = false)
    private Pedido pedido;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name="producto_id", nullable = false)
    private Producto producto;

    @Column(nullable = false)
    @NotNull
    @Min(1)
    private Integer cantidad;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull
    private BigDecimal precioUnitario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_impuesto", nullable = false, length = 10)
    private TipoImpuesto tipoImpuesto;

    @Column(name = "impuesto_porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal impuestoPorcentaje;

    @OneToMany(mappedBy = "detallePedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DetallePedidoNota> detallePedidoNotas = new ArrayList<>();

    protected DetallePedido() {}

    public DetallePedido(@NotNull Producto producto, @Min(1) Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor a cero");
        }
        this.producto = Objects.requireNonNull(producto, "producto no puede ser null");
        heredarNegocio(producto);
        this.cantidad = cantidad;
        this.precioUnitario = producto.getPrecio().setScale(2, RoundingMode.HALF_UP);
        this.tipoImpuesto = producto.getTipoImpuesto() != null ? producto.getTipoImpuesto() : TipoImpuesto.IVA;
        this.impuestoPorcentaje = producto.getImpuestoPorcentaje() != null
            ? producto.getImpuestoPorcentaje().setScale(2, RoundingMode.HALF_UP)
            : BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
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

    public TipoImpuesto getTipoImpuesto() {
        return tipoImpuesto;
    }

    public BigDecimal getImpuestoPorcentaje() {
        return impuestoPorcentaje;
    }

    public List<DetallePedidoNota> getDetallePedidoNotas() {
        return Collections.unmodifiableList(detallePedidoNotas);
    }

    public void cambiarPrecioUnitario(BigDecimal precioUnitario) {
        if (precioUnitario == null) throw new IllegalArgumentException("precioUnitario null");
        this.precioUnitario = precioUnitario.setScale(2, RoundingMode.HALF_UP);
    }

    public void asignarPedido(Pedido pedido) {
        this.pedido = pedido;
        heredarNegocio(pedido);
    }

    public void reemplazarNotas(List<NotaPredefinida> notasPredefinidas) {
        this.detallePedidoNotas.clear();
        if (notasPredefinidas == null || notasPredefinidas.isEmpty()) {
            return;
        }

        Set<Long> notasIds = new HashSet<>();
        for (NotaPredefinida notaPredefinida : notasPredefinidas) {
            if (notaPredefinida == null || notaPredefinida.getId() == null) {
                throw new IllegalArgumentException("nota predefinida invalida");
            }
            if (!notasIds.add(notaPredefinida.getId())) {
                throw new IllegalArgumentException("nota predefinida duplicada");
            }
            this.detallePedidoNotas.add(new DetallePedidoNota(this, notaPredefinida));
        }
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
