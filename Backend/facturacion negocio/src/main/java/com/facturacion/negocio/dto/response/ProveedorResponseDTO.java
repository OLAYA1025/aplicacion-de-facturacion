package com.facturacion.negocio.dto.response;

import com.facturacion.negocio.domain.Proveedor;

public class ProveedorResponseDTO {

    private Long id;
    private String nombre;
    private String telefono;
    private String correo;

    public static ProveedorResponseDTO from(Proveedor proveedor) {
        ProveedorResponseDTO dto = new ProveedorResponseDTO();
        dto.id = proveedor.getId();
        dto.nombre = proveedor.getNombre();
        dto.telefono = proveedor.getTelefono();
        dto.correo = proveedor.getCorreo();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getTelefono() {
        return telefono;
    }

    public String getCorreo() {
        return correo;
    }
}
