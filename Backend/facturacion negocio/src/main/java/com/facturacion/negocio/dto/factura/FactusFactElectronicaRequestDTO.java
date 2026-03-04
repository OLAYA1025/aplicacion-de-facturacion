package com.facturacion.negocio.dto.factura;

import com.facturacion.negocio.domain.DetallePedido;
import com.facturacion.negocio.domain.DocumentoFiscal;
import com.facturacion.negocio.domain.Pedido;
import com.facturacion.negocio.enumerated.TipoDocumentoFiscal;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;


public class FactusFactElectronicaRequestDTO {

    private String document = "01"; // 01 factura electronica
    private Integer numbering_range_id = 8;
    private String reference_code; // Código único
    private String payment_method_code; // 10 EFECTIVO
    private Boolean send_email = false;

    @JsonProperty("establishment")
    private Establecimiento establecimiento;

    @JsonProperty("customer")
    private CustomerDTO customer;

    @JsonProperty("items")
    private List<ItemDTO> items;

    public String getDocument() { return document; }
    public void setDocument(String document) { this.document = document; }

    public Integer getNumbering_range_id() { return numbering_range_id; }
    public void setNumbering_range_id(Integer numbering_range_id) { this.numbering_range_id = numbering_range_id; }

    public String getReference_code() { return reference_code; }
    public void setReference_code(String reference_code) { this.reference_code = reference_code; }

    public String getPayment_method_code() { return payment_method_code; }
    public void setPayment_method_code(String payment_method_code) { this.payment_method_code = payment_method_code; }

    public Boolean getSend_email() { return send_email; }
    public void setSend_email(Boolean send_email) { this.send_email = send_email; }

    public Establecimiento getEstablecimiento() { return establecimiento; }
    public void setEstablecimiento(Establecimiento establecimiento) { this.establecimiento = establecimiento; }

    public CustomerDTO getCustomer() { return customer; }
    public void setCustomer(CustomerDTO customer) { this.customer = customer; }

    public List<ItemDTO> getItems() { return items; }
    public void setItems(List<ItemDTO> items) { this.items = items; }
}