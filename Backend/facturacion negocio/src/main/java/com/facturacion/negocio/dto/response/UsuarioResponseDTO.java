package com.facturacion.negocio.dto.response;

import com.facturacion.negocio.domain.Usuario;
import com.facturacion.negocio.enumerated.Rol;

public class UsuarioResponseDTO {
    Long id;
    String nombre;
    String username;
    String email;
    Rol rol;

    public static UsuarioResponseDTO from(Usuario usuario) {
        UsuarioResponseDTO usuarioResponseDTO = new UsuarioResponseDTO();
        usuarioResponseDTO.setId(usuario.getId());
        usuarioResponseDTO.setNombre(usuario.getNombre());
        usuarioResponseDTO.setUsername(usuario.getUsername());
        usuarioResponseDTO.setEmail(usuario.getEmail());
        usuarioResponseDTO.setRol(usuario.getRol());
        return usuarioResponseDTO;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }
}
