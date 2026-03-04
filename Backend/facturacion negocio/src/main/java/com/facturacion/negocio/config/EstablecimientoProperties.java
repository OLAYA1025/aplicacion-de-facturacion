package com.facturacion.negocio.config;

import com.facturacion.negocio.dto.factura.Establecimiento;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "establecimiento")
public class EstablecimientoProperties {

    private String nombre;
    private String direccion;
    private String telefono;
    private String email;
    private Integer municipaly;


    @Bean
    public Establecimiento establecimiento(EstablecimientoProperties props) {
        Establecimiento establecimiento = new Establecimiento();
        establecimiento.setName(props.getNombre());
        establecimiento.setAddress(props.getDireccion());
        establecimiento.setPhone_number(props.getTelefono());
        establecimiento.setEmail(props.getEmail());
        establecimiento.setMunicipality_id(props.getMunicipaly());
        return establecimiento;
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
}