package com.facturacion.negocio.domain;

import com.facturacion.negocio.enumerated.EstadoPedido;
import com.facturacion.negocio.enumerated.MetodoPago;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

@Entity
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @NotBlank
    private String nombre;

    private Instant fecha;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPedido estado;

    @Enumerated(EnumType.STRING)
    @Column
    private MetodoPago metodoPago;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DetallePedido> detallePedidos = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "caja_id", nullable = false)
    private Caja caja;

    @ManyToOne
    private Usuario usuario;

    @OneToOne(mappedBy = "pedido", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private DocumentoFiscal documentoFiscal;

    protected Pedido() {}

    public Pedido(String nombre, List<DetallePedido> detallePedidos, Caja caja) {
        this.nombre = resolverNombre(nombre, null);
        this.estado = EstadoPedido.PENDIENTE;
        this.caja = caja;
        this.fecha = Instant.now();
        if (detallePedidos != null) {
            for (DetallePedido detalle : detallePedidos) {
                agregarDetalle(detalle);
            }
        }
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }

    public List<DetallePedido> getDetallePedidos() {
        return Collections.unmodifiableList(detallePedidos);
    }

    public Instant getFecha() {
        return fecha;
    }

    public EstadoPedido getEstado() { return estado; }
    public MetodoPago getMetodoPago() { return metodoPago; }
    public Usuario getUsuario() { return usuario; }

    public BigDecimal calcularTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (DetallePedido detalle : detallePedidos) {
            total = total.add(detalle.calcularSubtotal());
        }
        return total.setScale(2, RoundingMode.HALF_UP);
    }

    private void validarEditable() {
        if (this.estado != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se puede modificar un pedido en estado PENDIENTE");
        }
    }

    public void agregarDetalle(DetallePedido detalle) {
        Objects.requireNonNull(detalle, "detalle null");
        validarEditable();
        detalle.asignarPedido(this);
        this.detallePedidos.add(detalle);
    }

    public void removerDetalle(DetallePedido detalle) {
        Objects.requireNonNull(detalle, "detalle null");
        validarEditable();
        if (this.detallePedidos.remove(detalle)) {
            detalle.asignarPedido(null);
        }
    }

    public void actualizarPedido(String nombre, List<DetallePedido> detallePedidos) {
        validarEditable();
        this.nombre = resolverNombre(nombre, this.id);
        this.detallePedidos.clear();
        if (detallePedidos != null) {
            for (DetallePedido detalle : detallePedidos) {
                agregarDetalle(detalle);
            }
        }
    }

    public void actualizarNombre(String nombre) {
        validarEditable();
        this.nombre = resolverNombre(nombre, this.id);
    }

    public void pagar(Usuario usuario, MetodoPago metodoPago) {
        if (this.estado != EstadoPedido.PENDIENTE) {
            throw new IllegalStateException("Solo se puede pagar un pedido pendiente");
        }
        this.metodoPago = Objects.requireNonNull(metodoPago, "metodoPago requerido");
        this.estado = EstadoPedido.PAGO;
        this.usuario = usuario;
        this.fecha = Instant.now();
    }

    public void cancelar(Usuario usuario) {
        if (this.estado == EstadoPedido.PAGO) {
            throw new IllegalStateException("No se puede cancelar un pedido pagado");
        }
        this.estado = EstadoPedido.CANCELADO;
        this.usuario = usuario;
        this.fecha = Instant.now();
    }

    public Boolean isPendiente() {
        return this.estado == EstadoPedido.PENDIENTE;
    }

    public Caja getCaja() {
        return caja;
    }

    public void setCaja(Caja caja) {
        this.caja = caja;
    }

    private String resolverNombre(String nombre, Long pedidoId) {
        if (nombre == null || nombre.trim().isEmpty()) {
            if (pedidoId == null) {
                return "Pedido";
            }
            return "Pedido #" + pedidoId;
        }
        return nombre.trim();
    }

    public DocumentoFiscal getDocumentoFiscal() { return documentoFiscal; }

    public void setDocumentoFiscal(DocumentoFiscal documentoFiscal) {
        this.documentoFiscal = documentoFiscal;
        if (documentoFiscal != null) {
            documentoFiscal.setPedido(this);
        }
    }
}
