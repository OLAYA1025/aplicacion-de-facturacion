package com.facturacion.negocio.dto;

import com.facturacion.negocio.domain.Usuario;

public class CajaDTO {
    private Long id;
    private Usuario usuarioApertura;

    public Usuario getUsuarioApertura() {
        return usuarioApertura;
    }

    public Long getId() {
        return id;
    }
}
