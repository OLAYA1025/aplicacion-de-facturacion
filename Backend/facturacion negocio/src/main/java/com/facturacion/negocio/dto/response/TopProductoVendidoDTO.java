package com.facturacion.negocio.dto.response;

public class TopProductoVendidoDTO {

    private Long productoCodigo;
    private String productoNombre;
    private Long cantidadVendida;

    public TopProductoVendidoDTO(Long productoCodigo, String productoNombre, Long cantidadVendida) {
        this.productoCodigo = productoCodigo;
        this.productoNombre = productoNombre;
        this.cantidadVendida = cantidadVendida;
    }

    public Long getProductoCodigo() {
        return productoCodigo;
    }

    public void setProductoCodigo(Long productoCodigo) {
        this.productoCodigo = productoCodigo;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public Long getCantidadVendida() {
        return cantidadVendida;
    }

    public void setCantidadVendida(Long cantidadVendida) {
        this.cantidadVendida = cantidadVendida;
    }
}
