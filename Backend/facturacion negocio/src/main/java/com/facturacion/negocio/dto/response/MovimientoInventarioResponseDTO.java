package com.facturacion.negocio.dto.response;

import com.facturacion.negocio.domain.MovimientoInventario;
import com.facturacion.negocio.enumerated.TipoMovimientoInventario;

import java.time.LocalDateTime;

public class MovimientoInventarioResponseDTO {

    private Long movimientoId;
    private Long productoId;
    private String productoNombre;
    private TipoMovimientoInventario tipo;
    private Integer cantidad;
    private String referenciaTipo;
    private Long referenciaId;
    private String motivo;
    private LocalDateTime fecha;

    public static MovimientoInventarioResponseDTO from(MovimientoInventario movimiento) {
        MovimientoInventarioResponseDTO dto = new MovimientoInventarioResponseDTO();
        dto.movimientoId = movimiento.getId();
        dto.productoId = movimiento.getProducto().getCodigo();
        dto.productoNombre = movimiento.getProducto().getNombre();
        dto.tipo = movimiento.getTipo();
        dto.cantidad = movimiento.getCantidad();
        dto.referenciaTipo = movimiento.getReferenciaTipo();
        dto.referenciaId = movimiento.getReferenciaId();
        dto.motivo = movimiento.getMotivo();
        dto.fecha = movimiento.getFecha();
        return dto;
    }

    public Long getMovimientoId() {
        return movimientoId;
    }

    public Long getProductoId() {
        return productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
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

    public String getMotivo() {
        return motivo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }
}
