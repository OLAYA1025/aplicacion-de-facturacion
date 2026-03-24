package com.facturacion.negocio.services.dian.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public class DianInvoiceData {
    private String invoiceNumber;
    private String prefix;
    private Long consecutive;

    private String currencyCode = "COP";
    private String invoiceTypeCode = "01";

    private Instant issuedAt;

    private DianSupplierData supplier;
    private DianCustomerData customer;

    private List<DianInvoiceLineData> lines;

    private BigDecimal subtotal;// sin impuesto
    private BigDecimal ivaTotal;
    private BigDecimal incTotal;
    private BigDecimal taxTotal; // suma impuestos
    private BigDecimal total; // subtotal + impuestos
    private String cufe;


    public DianInvoiceData() {
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(String currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getInvoiceTypeCode() {
        return invoiceTypeCode;
    }

    public void setInvoiceTypeCode(String invoiceTypeCode) {
        this.invoiceTypeCode = invoiceTypeCode;
    }

    public Long getConsecutive() {
        return consecutive;
    }

    public void setConsecutive(Long consecutive) {
        this.consecutive = consecutive;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Instant getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(Instant issuedAt) {
        this.issuedAt = issuedAt;
    }

    public DianSupplierData getSupplier() {
        return supplier;
    }

    public void setSupplier(DianSupplierData supplier) {
        this.supplier = supplier;
    }

    public DianCustomerData getCustomer() {
        return customer;
    }

    public void setCustomer(DianCustomerData customer) {
        this.customer = customer;
    }

    public List<DianInvoiceLineData> getLines() {
        return lines;
    }

    public void setLines(List<DianInvoiceLineData> lines) {
        this.lines = lines;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public BigDecimal getTaxTotal() {
        return taxTotal;
    }

    public void setTaxTotal(BigDecimal taxTotal) {
        this.taxTotal = taxTotal;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public BigDecimal getIvaTotal() {
        return ivaTotal;
    }

    public void setIvaTotal(BigDecimal ivaTotal) {
        this.ivaTotal = ivaTotal;
    }

    public BigDecimal getIncTotal() {
        return incTotal;
    }

    public void setIncTotal(BigDecimal incTotal) {
        this.incTotal = incTotal;
    }

    public String getCufe() {
        return cufe;
    }

    public void setCufe(String cufe) {
        this.cufe = cufe;
    }


    @Override
    public String toString() {
        return "DianInvoiceData{" +
                "invoiceNumber='" + invoiceNumber + '\'' +
                ", prefix='" + prefix + '\'' +
                ", consecutive=" + consecutive +
                ", currencyCode='" + currencyCode + '\'' +
                ", invoiceTypeCode='" + invoiceTypeCode + '\'' +
                ", issuedAt=" + issuedAt +
                ", supplier=" + supplier.toString() +
                ", customer=" + customer.toString() +
                ", lines=" + lines.toString() +
                ", subtotal=" + subtotal +
                ", ivaTotal=" + ivaTotal +
                ", incTotal=" + incTotal +
                ", taxTotal=" + taxTotal +
                ", total=" + total +
                ", cufe='" + cufe + '\'' +
                '}';
    }
}