package com.facturacion.negocio.dto.response;

import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.domain.Usuario;

public class NegocioPlataformaResponseDTO {

    private Long id;
    private String nombre;
    private String nit;
    private String direccion;
    private String telefono;
    private boolean activo;
    private Boolean electronicaHabilitada;
    private String logoArchivo;
    private Long administradorId;
    private String administradorUsername;

    public static NegocioPlataformaResponseDTO from(Negocio negocio, Usuario administrador) {
        NegocioPlataformaResponseDTO dto = from(negocio);
        if (administrador != null) {
            dto.administradorId = administrador.getId();
            dto.administradorUsername = administrador.getUsername();
        }
        return dto;
    }

    public static NegocioPlataformaResponseDTO from(Negocio negocio) {
        NegocioPlataformaResponseDTO dto = new NegocioPlataformaResponseDTO();
        dto.id = negocio.getId();
        dto.nombre = negocio.getNombre();
        dto.nit = negocio.getNit();
        dto.direccion = negocio.getDireccion();
        dto.telefono = negocio.getTelefono();
        dto.activo = negocio.isActivo();
        dto.electronicaHabilitada = negocio.isElectronicaHabilitada();
        dto.logoArchivo = negocio.getLogoArchivo();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getNit() {
        return nit;
    }

    public String getDireccion() {
        return direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public boolean isActivo() {
        return activo;
    }

    public Boolean getElectronicaHabilitada() {
        return electronicaHabilitada;
    }

    public Long getAdministradorId() {
        return administradorId;
    }

    public String getAdministradorUsername() {
        return administradorUsername;
    }

    public String getLogoArchivo() {
        return logoArchivo;
    }

}
