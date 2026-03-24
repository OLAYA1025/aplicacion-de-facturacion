package com.facturacion.negocio.dto.factura;

public class Establecimiento {
    private String name;
    private String nit;
    private String dv;
    private String address;
    private String phone_number;
    private String email;
    //Factus
    private Integer municipality_id;
    // DIAN (código DANE)
    private String municipality_code_dian;

    public Establecimiento() {
    }

    public Establecimiento(String name, String nit, String address, String phone_number, String email, Integer municipality_id) {
        this.name = name;
        this.nit = nit;
        this.address = address;
        this.phone_number = phone_number;
        this.email = email;
        this.municipality_id = municipality_id;
    }

    public Establecimiento(String name, String nit, String dv, String address, String phone_number, String email, Integer municipality_id,  String municipality_code_dian) {
        this.name = name;
        this.nit = nit;
        this.dv = dv;
        this.address = address;
        this.phone_number = phone_number;
        this.email = email;
        this.municipality_id = municipality_id;
        this.municipality_code_dian = municipality_code_dian;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public Integer getMunicipality_id() {
        return municipality_id;
    }

    public void setMunicipality_id(Integer municipality_id) {
        this.municipality_id = municipality_id;
    }

    public String getMunicipality_code_dian() {
        return municipality_code_dian;
    }

    public void setMunicipality_code_dian(String municipality_code_dian) {
        this.municipality_code_dian = municipality_code_dian;
    }

    @Override
    public String toString() {
        return "Establecimiento{" +
                "name='" + name + '\'' +
                ", nit='" + nit + '\'' +
                ", dv='" + dv + '\'' +
                ", address='" + address + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", email='" + email + '\'' +
                ", municipality_id=" + municipality_id +
                ", municipality_code_dian='" + municipality_code_dian + '\'' +
                '}';
    }
}
