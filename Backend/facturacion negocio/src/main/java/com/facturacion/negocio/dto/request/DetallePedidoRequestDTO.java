package com.facturacion.negocio.dto.request;

public class DetallePedidoRequestDTO {
    private Long productoId;
    private Integer cantidad;
    private java.util.List<Long> notaPredefinidaIds;

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public java.util.List<Long> getNotaPredefinidaIds() {
        return notaPredefinidaIds;
    }

    public void setNotaPredefinidaIds(java.util.List<Long> notaPredefinidaIds) {
        this.notaPredefinidaIds = notaPredefinidaIds;
    }
}
