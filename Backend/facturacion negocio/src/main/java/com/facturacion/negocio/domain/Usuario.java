package com.facturacion.negocio.domain;

import com.facturacion.negocio.enumerated.Rol;
import jakarta.persistence.*;

@Entity
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean activo;

    @Column(nullable = false)
    private String nombre;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    protected Usuario() {
    }

    public Usuario(String nombre, String username, String password, String email, Rol rol) {
        this.activo = true;
        this.nombre = nombre;
        this.username = username;
        this.password = password;
        this.email = email;
        this.rol = rol;
    }

    public Rol getRol() {
        return rol;
    }

    public void volverAdministrador() {
        this.rol = Rol.ADMINISTRADOR;
    }

    public void volverCajero() {
        this.rol = Rol.CAJERO;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
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

    public Long getId() {
        return id;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

}


