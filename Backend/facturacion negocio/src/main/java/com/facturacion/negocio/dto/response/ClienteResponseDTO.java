package com.facturacion.negocio.dto.response;

import com.facturacion.negocio.domain.Cliente;
import com.facturacion.negocio.enumerated.TipoDocumento;

public class ClienteResponseDTO {

    private TipoDocumento tipoIdentificacion;
    private String numeroIdentificacion;
    private String nombre;
    private String email;

    public static ClienteResponseDTO from(Cliente cliente) {
        ClienteResponseDTO dto = new ClienteResponseDTO();
        dto.tipoIdentificacion = cliente.getTipoIdentificacion();
        dto.numeroIdentificacion = cliente.getNumeroIdentificacion();
        dto.nombre = cliente.getNombre();
        dto.email = cliente.getEmail();
        return dto;
    }



    public TipoDocumento getTipoIdentificacion() {
        return tipoIdentificacion;
    }

    public String getNumeroIdentificacion() {
        return numeroIdentificacion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getEmail() {
        return email;
    }
}
