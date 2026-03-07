package com.facturacion.negocio.services.tirillaFctura;

import com.facturacion.negocio.domain.DetallePedido;
import com.facturacion.negocio.domain.Producto;
import com.facturacion.negocio.enumerated.TipoImpuesto;

import java.math.BigDecimal;

public class FacturaItem {
    private Integer cantidad;
    private String codigo;
    private String nombre;
    private BigDecimal valorUnitario;
    private BigDecimal impuesto;
    private TipoImpuesto tipoImpuesto;

    public FacturaItem(){}

    public static FacturaItem from(DetallePedido detalle){
        FacturaItem facturaItem = new FacturaItem();
        Producto producto = detalle.getProducto();
        facturaItem.setCodigo(producto.getCodigo().toString());
        facturaItem.setNombre(producto.getNombre());
        facturaItem.setCantidad(detalle.getCantidad());
        facturaItem.setValorUnitario(detalle.getPrecioUnitario());
        facturaItem.setImpuesto(producto.getImpuestoPorcentaje());
        facturaItem.setTipoImpuesto(producto.getTipoImpuesto());
        return facturaItem;
    }

    public BigDecimal calcularSubtotal(){
        BigDecimal subtotal = BigDecimal.ZERO;
        return this.valorUnitario.multiply(BigDecimal.valueOf(this.cantidad.doubleValue()));
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public BigDecimal getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(BigDecimal impuesto) {
        this.impuesto = impuesto;
    }

    public TipoImpuesto getTipoImpuesto() {
        return tipoImpuesto;
    }

    public void setTipoImpuesto(TipoImpuesto tipoImpuesto) {
        this.tipoImpuesto = tipoImpuesto;
    }
}