package com.facturacion.negocio.dto.factura;

import com.facturacion.negocio.domain.DetallePedido;
import com.facturacion.negocio.domain.Producto;
import com.facturacion.negocio.enumerated.TipoImpuesto;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ItemDTO {
    private String code_reference;
    private String name;
    private Integer quantity;
    private BigDecimal discount_rate = BigDecimal.ZERO;
    private BigDecimal price;
    private String tax_rate;
    private Integer unit_measure_id = 70; //id de unidades
    private Integer standard_code_id = 1; //ID que corresponde al código de estándar que se adopto para los productos o servicios
    @JsonProperty("is_excluded")
    private Integer isExcluded; //Si el producto está excluido de IVA (0: no, 1: sí).
    private Integer tribute_id = 1; //1 para IVA (0%, 5%, 19%) y 4 para INC (8% impuesto nacional al consumo. Para preparaciones)

    public static ItemDTO from(DetallePedido detalle) {
        return from(detalle.getProducto(), detalle.getCantidad(), detalle.getPrecioUnitario());
    }

    public static ItemDTO from(Producto producto, Integer cantidad, BigDecimal precioUnitario) {
        if (producto == null) {
            throw new IllegalArgumentException("producto invalido");
        }
        if (cantidad == null || cantidad <= 0) {
            throw new IllegalArgumentException("cantidad invalida");
        }
        ItemDTO itemDTO = new ItemDTO();
        itemDTO.setCode_reference(producto.getId().toString());
        itemDTO.setName(producto.getNombre());
        itemDTO.setQuantity(cantidad);
        itemDTO.setPrice(precioUnitario);
        itemDTO.setIsExcluded(Boolean.TRUE.equals(producto.getIsExcluded()) ? 1 : 0);
        TipoImpuesto tipoImpuesto = producto.getTipoImpuesto() != null ? producto.getTipoImpuesto() : TipoImpuesto.IVA;
        itemDTO.setTribute_id(tipoImpuesto.getTributeId());
        BigDecimal porcentaje = producto.getImpuestoPorcentaje() != null
                ? producto.getImpuestoPorcentaje()
                : BigDecimal.ZERO;
        String taxString = porcentaje.setScale(2, RoundingMode.HALF_UP).toPlainString();
        itemDTO.setTax_rate(taxString);

        return itemDTO;
    }


    public String getCode_reference() {
        return code_reference;
    }

    public void setCode_reference(String code_reference) {
        this.code_reference = code_reference;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getTax_rate() {
        return tax_rate;
    }

    public void setTax_rate(String tax_rate) {
        this.tax_rate = tax_rate;
    }

    public Integer getUnit_measure_id() {
        return unit_measure_id;
    }

    public void setUnit_measure_id(Integer unit_measure_id) {
        this.unit_measure_id = unit_measure_id;
    }

    public Integer getStandard_code_id() {
        return standard_code_id;
    }

    public void setStandard_code_id(Integer standard_code_id) {
        this.standard_code_id = standard_code_id;
    }

    public Integer getIsExcluded() {
        return isExcluded;
    }

    public void setIsExcluded(Integer isExcluded) {
        this.isExcluded = isExcluded;
    }

    public Integer getTribute_id() {
        return tribute_id;
    }

    public void setTribute_id(Integer tribute_id) {
        this.tribute_id = tribute_id;
    }

    public BigDecimal getDiscount_rate() {
        return discount_rate;
    }

    public void setDiscount_rate(BigDecimal discount_rate) {
        this.discount_rate = discount_rate;
    }
}
