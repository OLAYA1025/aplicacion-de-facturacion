package com.facturacion.negocio.dto.request;

import com.facturacion.negocio.enumerated.TipoGestionProducto;

import java.math.BigDecimal;

public class CrearProductoRequestDTO {

    private String nombre;
    private BigDecimal precio;
    private String descripcion;
    private TipoGestionProducto tipoGestion;
    private Integer stockInicial;
    private Integer stockMinimo;
    private Long categoriaId;


    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public TipoGestionProducto getTipoGestion() {
        return tipoGestion;
    }

    public void setTipoGestion(TipoGestionProducto tipoGestion) {
        this.tipoGestion = tipoGestion;
    }

    public Integer getStockInicial() {
        return stockInicial;
    }

    public void setStockInicial(Integer stockInicial) {
        this.stockInicial = stockInicial;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public void setStockMinimo(Integer stockMinimo) {
        this.stockMinimo = stockMinimo;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }
}
