package com.facturacion.negocio.dto.request;

public class CambiarPasswordUsuarioRequestDTO {

    private String nuevaPassword;

    public String getNuevaPassword() {
        return nuevaPassword;
    }

    public void setNuevaPassword(String nuevaPassword) {
        this.nuevaPassword = nuevaPassword;
    }
}
