package com.facturacion.negocio.config;

import com.facturacion.negocio.dto.factura.Establecimiento;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "establecimiento")
public class EstablecimientoProperties {

    private String nombre;
    private String nit;
    private String dv;
    private String direccion;
    private String telefono;
    private String email;
    private Integer municipaly;
    private String municipalityCodeDian;

    @Bean
    public Establecimiento establecimiento(EstablecimientoProperties props) {
        Establecimiento establecimiento = new Establecimiento();
        establecimiento.setName(props.getNombre());
        establecimiento.setNit(props.getNit());
        establecimiento.setDv(props.getDv());
        establecimiento.setAddress(props.getDireccion());
        establecimiento.setPhone_number(props.getTelefono());
        establecimiento.setEmail(props.getEmail());
        establecimiento.setMunicipality_id(props.getMunicipaly());
        establecimiento.setMunicipality_code_dian(props.getMunicipalityCodeDian());
        return establecimiento;
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

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getMunicipaly() {
        return municipaly;
    }

    public void setMunicipaly(Integer municipaly) {
        this.municipaly = municipaly;
    }

    public String getMunicipalityCodeDian() {
        return municipalityCodeDian;
    }

    public void setMunicipalityCodeDian(String municipalityCodeDian) {
        this.municipalityCodeDian = municipalityCodeDian;
    }
}