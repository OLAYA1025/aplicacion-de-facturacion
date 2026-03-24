package com.facturacion.negocio.services.dian.dto;

public class DianCustomerData {
    private String identificationNumber;
    private String identificationType;

    private String name;
    private String email;

    private Integer legalOrganizationType;


    public DianCustomerData() {
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }

    public String getIdentificationType() {
        return identificationType;
    }

    public void setIdentificationType(String identificationType) {
        this.identificationType = identificationType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getLegalOrganizationType() {
        return legalOrganizationType;
    }

    public void setLegalOrganizationType(Integer legalOrganizationType) {
        this.legalOrganizationType = legalOrganizationType;
    }

    @Override
    public String toString() {
        return "DianCustomerData{" +
                "identificationNumber='" + identificationNumber + '\'' +
                ", identificationType='" + identificationType + '\'' +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", legalOrganizationType=" + legalOrganizationType +
                '}';
    }
}
