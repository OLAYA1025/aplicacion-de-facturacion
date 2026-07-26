package com.facturacion.negocio.services.tirillaFactura;

import java.math.BigDecimal;

import com.facturacion.negocio.domain.DetallePedido;
import com.facturacion.negocio.domain.Producto;
import com.facturacion.negocio.enumerated.TipoImpuesto;

public class FacturaItem {
    private Integer cantidad;
    private String id;
    private String nombre;
    private BigDecimal valorUnitario;
    private BigDecimal impuesto;
    private TipoImpuesto tipoImpuesto;

    public FacturaItem(){}

    public static FacturaItem from(DetallePedido detalle){
        return from(
                detalle.getProducto(),
                detalle.getCantidad(),
                detalle.getPrecioUnitario(),
                detalle.getTipoImpuesto(),
                detalle.getImpuestoPorcentaje()
        );
    }

    public static FacturaItem from(Producto producto, Integer cantidad, BigDecimal precioUnitario){
        if (producto == null) {
            throw new IllegalArgumentException("producto invalido");
        }
        return from(producto, cantidad, precioUnitario, producto.getTipoImpuesto(), producto.getImpuestoPorcentaje());
    }

    public static FacturaItem from(Producto producto,
                                   Integer cantidad,
                                   BigDecimal precioUnitario,
                                   TipoImpuesto tipoImpuesto,
                                   BigDecimal impuestoPorcentaje){
        if (producto == null) {
            throw new IllegalArgumentException("producto invalido");
        }
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("cantidad invalida");
        }
        FacturaItem facturaItem = new FacturaItem();
        TipoImpuesto tipoNormalizado = tipoImpuesto != null ? tipoImpuesto : TipoImpuesto.IVA;
        BigDecimal impuestoNormalizado = impuestoPorcentaje != null ? impuestoPorcentaje : BigDecimal.ZERO;
        facturaItem.setId(producto.getId().toString());
        facturaItem.setNombre(producto.getNombre());
        facturaItem.setCantidad(cantidad);
        facturaItem.setValorUnitario(precioUnitario);
        facturaItem.setImpuesto(impuestoNormalizado);
        facturaItem.setTipoImpuesto(tipoNormalizado);
        return facturaItem;
    }

    public BigDecimal calcularSubtotal(){
        return this.valorUnitario.multiply(BigDecimal.valueOf(this.cantidad.doubleValue()));
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
