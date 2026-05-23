package com.facturacion.negocio.dto.response;

import com.facturacion.negocio.domain.Categoria;

public class CategoriaResponseDTO {

    private Long id;
    private String nombre;

    public static CategoriaResponseDTO from(Categoria categoria) {
        CategoriaResponseDTO dto = new CategoriaResponseDTO();
        dto.setId(categoria.getId());
        dto.setNombre(categoria.getNombre());
        return dto;
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
}
