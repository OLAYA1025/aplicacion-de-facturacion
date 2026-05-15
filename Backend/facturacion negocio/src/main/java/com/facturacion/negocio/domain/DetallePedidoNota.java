package com.facturacion.negocio.domain;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(
        name = "detalle_pedido_nota",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_detalle_pedido_nota_unica",
                        columnNames = {"detalle_pedido_id", "nota_predefinida_id"}
                )
        }
)
public class DetallePedidoNota {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "detalle_pedido_id", nullable = false)
    private DetallePedido detallePedido;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "nota_predefinida_id", nullable = false)
    private NotaPredefinida notaPredefinida;

    protected DetallePedidoNota() {
    }

    public DetallePedidoNota(DetallePedido detallePedido, NotaPredefinida notaPredefinida) {
        this.detallePedido = Objects.requireNonNull(detallePedido, "detallePedido requerido");
        this.notaPredefinida = Objects.requireNonNull(notaPredefinida, "notaPredefinida requerida");
    }

    public Long getId() {
        return id;
    }

    public DetallePedido getDetallePedido() {
        return detallePedido;
    }

    public NotaPredefinida getNotaPredefinida() {
        return notaPredefinida;
    }

    public void asignarDetallePedido(DetallePedido detallePedido) {
        this.detallePedido = detallePedido;
    }
}
