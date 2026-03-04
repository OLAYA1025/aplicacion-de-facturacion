package com.facturacion.negocio.dto.factura;

public class Establecimiento {
    private String name;
    private String address;
    private String phone_number;
    private String email;
    private Integer municipality_id;

    public Establecimiento() {
    }

    public Establecimiento(String name, String address, String phone_number, String email, Integer municipality_id) {
        this.name = name;
        this.address = address;
        this.phone_number = phone_number;
        this.email = email;
        this.municipality_id = municipality_id;
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
}
