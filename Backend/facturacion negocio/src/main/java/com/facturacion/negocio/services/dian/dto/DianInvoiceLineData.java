package com.facturacion.negocio.services.dian.dto;

import java.math.BigDecimal;

public class DianInvoiceLineData {
    private String code;

    private String description;

    private Integer quantity;

    private BigDecimal unitPrice; // precio sin IVA

    private String taxCode; // "01" o "04"

    private BigDecimal taxAmount;

    private BigDecimal taxPercent;

    private BigDecimal lineExtensionAmount;

    public DianInvoiceLineData() {
    }


    public String getTaxCode() {
        return taxCode;
    }

    public void setTaxCode(String taxCode) {
        this.taxCode = taxCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getTaxPercent() {
        return taxPercent;
    }

    public void setTaxPercent(BigDecimal taxPercent) {
        this.taxPercent = taxPercent;
    }

    public BigDecimal getLineExtensionAmount() {
        return lineExtensionAmount;
    }

    public void setLineExtensionAmount(BigDecimal lineExtensionAmount) {
        this.lineExtensionAmount = lineExtensionAmount;
    }

    @Override
    public String toString() {
        return "DianInvoiceLineData{" +
                "code='" + code + '\'' +
                ", description='" + description + '\'' +
                ", quantity=" + quantity +
                ", unitPrice=" + unitPrice +
                ", taxCode='" + taxCode + '\'' +
                ", taxAmount=" + taxAmount +
                ", taxPercent=" + taxPercent +
                ", lineExtensionAmount=" + lineExtensionAmount +
                '}';
    }
}
