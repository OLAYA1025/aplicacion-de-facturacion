package com.facturacion.negocio.dto.response;

import com.facturacion.negocio.domain.Proveedor;
import com.facturacion.negocio.enumerated.TipoDocumento;

public class ProveedorResponseDTO {

    private Long id;
    private String nombre;
    private String telefono;
    private String correo;
    private TipoDocumento tipoDocumento;

    public static ProveedorResponseDTO from(Proveedor proveedor) {
        ProveedorResponseDTO dto = new ProveedorResponseDTO();
        dto.id = proveedor.getId();
        dto.nombre = proveedor.getNombre();
        dto.telefono = proveedor.getTelefono();
        dto.correo = proveedor.getCorreo();
        dto.tipoDocumento = proveedor.getTipoDocumento();
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

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }
}
