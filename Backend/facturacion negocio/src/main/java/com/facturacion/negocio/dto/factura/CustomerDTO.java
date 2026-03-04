package com.facturacion.negocio.dto.factura;

import com.facturacion.negocio.domain.Cliente;
import com.facturacion.negocio.enumerated.TipoDocumento;
import com.fasterxml.jackson.annotation.JsonProperty;

public class CustomerDTO {
    private Integer identification_document_id;
    @JsonProperty("identification")
    private String numeroIdentificacion;
    private String names;
    private String email;
    private Integer legal_organization_id = 2; //1 persona juridica, 2 para persona natural;
    @JsonProperty("company")
    private String razonSocial;

    private Integer tribute_id = 21; // 21 No aplica, 18 IVA

    public static CustomerDTO from(Cliente cliente){
        CustomerDTO customerDTO = new CustomerDTO();
        customerDTO.setIdentification_document_id(cliente.getTipoIdentificacion().getFactusId());
        customerDTO.setNumeroIdentificacion(cliente.getNumeroIdentificacion());


        customerDTO.setEmail(cliente.getEmail());
        if(cliente.getTipoIdentificacion() == TipoDocumento.NIT){
            customerDTO.setLegal_organization_id(1);
            customerDTO.setRazonSocial(cliente.getNombre());
            customerDTO.setTribute_id(18); //se pone como responsable de iva
        }

        if(cliente.getTipoIdentificacion() != TipoDocumento.NIT)customerDTO.setNames(cliente.getNombre());

        return customerDTO;

    };

    public Integer getIdentification_document_id() {
        return identification_document_id;
    }

    public void setIdentification_document_id(Integer identification_document_id) {
        this.identification_document_id = identification_document_id;
    }

    public String getNumeroIdentificacion() {
        return numeroIdentificacion;
    }

    public void setNumeroIdentificacion(String numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNames() {
        return names;
    }

    public void setNames(String names) {
        this.names = names;
    }

    public Integer getLegal_organization_id() {
        return legal_organization_id;
    }

    public void setLegal_organization_id(Integer legal_organization_id) {
        this.legal_organization_id = legal_organization_id;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public Integer getTribute_id() {
        return tribute_id;
    }

    public void setTribute_id(Integer tribute_id) {
        this.tribute_id = tribute_id;
    }
}
