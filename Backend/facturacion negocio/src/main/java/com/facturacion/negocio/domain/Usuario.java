package com.facturacion.negocio.domain;

import com.facturacion.negocio.enumerated.Rol;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_usuario_negocio_username", columnNames = {"negocio_id", "username"})
        }
)
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private boolean activo;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Rol rol;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "negocio_id")
    private Negocio negocio;

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

    public Negocio getNegocio() {
        return negocio;
    }

    public void setNegocio(Negocio negocio) {
        this.negocio = negocio;
    }

    @PrePersist
    @PreUpdate
    private void validarNegocioPorRol() {
        if (rol == null) {
            throw new IllegalStateException("rol invalido");
        }
        if (rol == Rol.SUPER_ADMIN) {
            if (negocio != null) {
                throw new IllegalStateException("SUPER_ADMIN no puede tener negocio asociado");
            }
            return;
        }
        if (negocio == null) {
            throw new IllegalStateException("usuarios de negocio requieren negocio asociado");
        }
    }
}
