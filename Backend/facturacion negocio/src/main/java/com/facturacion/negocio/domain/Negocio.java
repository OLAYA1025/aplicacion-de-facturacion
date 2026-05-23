package com.facturacion.negocio.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "negocio")
public class Negocio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 30)
    private String nit;

    @Column(length = 255)
    private String direccion;

    @Column(length = 30)
    private String telefono;

    @Column(nullable = false)
    private boolean activo;

    protected Negocio() {
    }

    public Negocio(String nombre, String nit, String direccion, String telefono, boolean activo) {
        setNombre(nombre);
        setNit(nit);
        setDireccion(direccion);
        setTelefono(telefono);
        this.activo = activo;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre negocio invalido");
        }
        this.nombre = nombre.trim();
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit == null || nit.isBlank() ? null : nit.trim();
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion == null || direccion.isBlank() ? null : direccion.trim();
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono == null || telefono.isBlank() ? null : telefono.trim();
    }

    public boolean isActivo() {
        return activo;
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }
}
