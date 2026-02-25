package com.facturacion.negocio.dto.response;

import com.facturacion.negocio.domain.Compra;
import com.facturacion.negocio.enumerated.EstadoCompra;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class CompraResumenResponseDTO {

    private Long compraId;
    private Long proveedorId;
    private String proveedorNombre;
    private String numeroFactura;
    private EstadoCompra estado;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaConfirmacion;
    private LocalDateTime fechaAnulacion;
    private BigDecimal subtotal;
    private BigDecimal total;
    private String observacion;
    private String anuladoPor;

    public static CompraResumenResponseDTO from(Compra compra) {
        CompraResumenResponseDTO dto = new CompraResumenResponseDTO();
        dto.compraId = compra.getId();
        dto.proveedorId = compra.getProveedor().getId();
        dto.proveedorNombre = compra.getProveedor().getNombre();
        dto.numeroFactura = compra.getNumeroFactura();
        dto.estado = compra.getEstado();
        dto.fechaCreacion = compra.getFechaCreacion();
        dto.fechaConfirmacion = compra.getFechaConfirmacion();
        dto.fechaAnulacion = compra.getFechaAnulacion();
        dto.subtotal = compra.getSubtotal();
        dto.total = compra.getTotal();
        dto.observacion = compra.getObservacion();
        dto.anuladoPor = compra.getAnuladoPor();
        return dto;
    }

    public Long getCompraId() {
        return compraId;
    }

    public Long getProveedorId() {
        return proveedorId;
    }

    public String getProveedorNombre() {
        return proveedorNombre;
    }

    public String getNumeroFactura() {
        return numeroFactura;
    }

    public EstadoCompra getEstado() {
        return estado;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public LocalDateTime getFechaConfirmacion() {
        return fechaConfirmacion;
    }

    public LocalDateTime getFechaAnulacion() {
        return fechaAnulacion;
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
}
