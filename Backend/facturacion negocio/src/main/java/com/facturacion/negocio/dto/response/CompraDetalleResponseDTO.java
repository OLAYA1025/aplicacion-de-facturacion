package com.facturacion.negocio.dto.response;

import com.facturacion.negocio.domain.CompraDetalle;

import java.math.BigDecimal;

public class CompraDetalleResponseDTO {

    private Long productoId;
    private String nombreProducto;
    private Integer cantidad;
    private BigDecimal costoUnitario;
    private BigDecimal subtotal;

    public static CompraDetalleResponseDTO from(CompraDetalle detalle) {
        CompraDetalleResponseDTO dto = new CompraDetalleResponseDTO();
        dto.productoId = detalle.getProducto().getId();
        dto.nombreProducto = detalle.getProducto().getNombre();
        dto.cantidad = detalle.getCantidad();
        dto.costoUnitario = detalle.getCostoUnitario();
        dto.subtotal = detalle.calcularSubtotal();
        return dto;
    }

    public Long getProductoId() {
        return productoId;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public BigDecimal getCostoUnitario() {
        return costoUnitario;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }
}
