package com.facturacion.negocio.services.dian.dto;

public class DianSupplierData {
    private String nit;

    private String dv;

    private String name;

    private String address;

    private String phone;

    private String email;

    private String municipalityCode;

    private Integer legalOrganizationType = 1;

    private String municipalityName = "MEDELLIN";


    public DianSupplierData() {
    }


    public Integer getLegalOrganizationType() {
        return legalOrganizationType;
    }

    public void setLegalOrganizationType(Integer legalOrganizationType) {
        this.legalOrganizationType = legalOrganizationType;
    }

    public String getMunicipalityName() {
        return municipalityName;
    }

    public void setMunicipalityName(String municipalityName) {
        this.municipalityName = municipalityName;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getDv() {
        return dv;
    }

    public void setDv(String dv) {
        this.dv = dv;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMunicipalityCode() {
        return municipalityCode;
    }

    public void setMunicipalityCode(String municipalityCode) {
        this.municipalityCode = municipalityCode;
    }

    @Override
    public String toString() {
        return "DianSupplierData{" +
                "nit='" + nit + '\'' +
                ", dv='" + dv + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", municipalityCode='" + municipalityCode + '\'' +
                ", legalOrganizationType=" + legalOrganizationType +
                ", municipalityName='" + municipalityName + '\'' +
                '}';
    }
}
