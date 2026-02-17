package com.facturacion.negocio.dto.response;

import com.facturacion.negocio.domain.DetallePedido;
import com.facturacion.negocio.domain.Pedido;
import com.facturacion.negocio.enumerated.EstadoPedido;
import com.facturacion.negocio.enumerated.MetodoPago;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class PedidoResponseDTO {
    private Long id;
    private String nombre;
    private LocalDateTime fecha;
    private EstadoPedido estado;
    private MetodoPago metodoPago;
    private Long usuarioId;
    private String usuarioUsername;
    private BigDecimal total;
    private List<DetallePedidoResponseDTO> detalles;

    public static PedidoResponseDTO from(Pedido pedido) {
        PedidoResponseDTO dto = new PedidoResponseDTO();
        dto.setId(pedido.getId());
        dto.setNombre(pedido.getNombre());
        dto.setFecha(pedido.getFecha());
        dto.setEstado(pedido.getEstado());
        dto.setMetodoPago(pedido.getMetodoPago());
        if (pedido.getUsuario() != null) {
            dto.setUsuarioId(pedido.getUsuario().getId());
            dto.setUsuarioUsername(pedido.getUsuario().getUsername());
        }

        return dto;
    }

    public static PedidoResponseDTO from(Pedido pedido, List<DetallePedido> detalles) {
        PedidoResponseDTO dto = from(pedido);
        List<DetallePedidoResponseDTO> detalleDtos = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        if (detalles != null) {
            for (DetallePedido detalle : detalles) {
                detalleDtos.add(DetallePedidoResponseDTO.from(detalle));
                total = total.add(detalle.calcularSubtotal());
            }
            total = total.setScale(2, RoundingMode.HALF_UP);
        }

        dto.setDetalles(detalleDtos);
        dto.setTotal(total);
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public String getUsuarioUsername() {
        return usuarioUsername;
    }

    public void setUsuarioUsername(String usuarioUsername) {
        this.usuarioUsername = usuarioUsername;
    }



    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<DetallePedidoResponseDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedidoResponseDTO> detalles) {
        this.detalles = detalles;
    }
}
