package com.facturacion.negocio.dto.request;

import com.facturacion.negocio.enumerated.TipoGestionProducto;
import com.facturacion.negocio.enumerated.TipoImpuesto;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.math.BigDecimal;

public class ProductoUpdateRequestDTO {

    private String nombre;
    private String codigo;
    private BigDecimal precio;
    private BigDecimal costo;
    private String descripcion;
    private TipoGestionProducto tipoProducto;
    private TipoGestionProducto tipoGestion;
    @JsonAlias({"tipo_impuesto"})
    private TipoImpuesto tipoImpuesto;
    @JsonAlias({"impuesto_porcentaje", "ivaPorcentaje", "iva_porcentaje"})
    private BigDecimal impuestoPorcentaje;
    private Integer stockMinimo;
    private Long categoriaId;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public void setPrecio(BigDecimal precio) {
        this.precio = precio;
    }

    public BigDecimal getCosto() {
        return costo;
    }

    public void setCosto(BigDecimal costo) {
        this.costo = costo;
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

    public TipoGestionProducto getTipoProducto() {
        return tipoProducto;
    }

    public void setTipoProducto(TipoGestionProducto tipoProducto) {
        this.tipoProducto = tipoProducto;
    }

    public TipoGestionProducto resolverTipoProducto() {
        return tipoProducto != null ? tipoProducto : tipoGestion;
    }

    public TipoImpuesto getTipoImpuesto() {
        return tipoImpuesto;
    }

    public void setTipoImpuesto(TipoImpuesto tipoImpuesto) {
        this.tipoImpuesto = tipoImpuesto;
    }

    public BigDecimal getImpuestoPorcentaje() {
        return impuestoPorcentaje;
    }

    public void setImpuestoPorcentaje(BigDecimal impuestoPorcentaje) {
        this.impuestoPorcentaje = impuestoPorcentaje;
    }

    @Deprecated
    public BigDecimal getIvaPorcentaje() {
        return impuestoPorcentaje;
    }

    @Deprecated
    public void setIvaPorcentaje(BigDecimal ivaPorcentaje) {
        this.impuestoPorcentaje = ivaPorcentaje;
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
