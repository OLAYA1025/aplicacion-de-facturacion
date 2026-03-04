package com.facturacion.negocio.dto.request;

import com.facturacion.negocio.enumerated.TipoDocumento;

public class ClienteFiscalDTO {
    private TipoDocumento tipoIdentificacion;   // "CC" / "NIT"
    private String numeroIdentificacion; // "900123456"
    private String nombre;               // "Empresa SAS"
    private String email;


    public TipoDocumento getTipoIdentificacion() {
        return tipoIdentificacion;
    }

    public void setTipoIdentificacion(TipoDocumento tipoIdentificacion) {
        this.tipoIdentificacion = tipoIdentificacion;
    }

    public String getNumeroIdentificacion() {
        return numeroIdentificacion;
    }

    public void setNumeroIdentificacion(String numeroIdentificacion) {
        this.numeroIdentificacion = numeroIdentificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
