package com.facturacion.negocio.dto.response;

import com.facturacion.negocio.domain.NotaPredefinida;

public class NotaPredefinidaResponseDTO {
    private Long id;
    private String descripcion;

    public static NotaPredefinidaResponseDTO from(NotaPredefinida nota) {
        NotaPredefinidaResponseDTO dto = new NotaPredefinidaResponseDTO();
        dto.setId(nota.getId());
        dto.setDescripcion(nota.getDescripcion());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
