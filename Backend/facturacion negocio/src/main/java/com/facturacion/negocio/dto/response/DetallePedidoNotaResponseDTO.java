package com.facturacion.negocio.dto.response;

import com.facturacion.negocio.domain.DetallePedidoNota;
import com.facturacion.negocio.domain.GrupoNotaCategoria;
import com.facturacion.negocio.domain.NotaPredefinida;

public class DetallePedidoNotaResponseDTO {
    private Long id;
    private Long notaPredefinidaId;
    private String descripcion;
    private Long grupoId;
    private String grupoNombre;

    public static DetallePedidoNotaResponseDTO from(DetallePedidoNota detallePedidoNota) {
        DetallePedidoNotaResponseDTO dto = new DetallePedidoNotaResponseDTO();
        dto.setId(detallePedidoNota.getId());
        NotaPredefinida nota = detallePedidoNota.getNotaPredefinida();
        if (nota != null) {
            dto.setNotaPredefinidaId(nota.getId());
            dto.setDescripcion(nota.getDescripcion());
            GrupoNotaCategoria grupo = nota.getGrupo();
            if (grupo != null) {
                dto.setGrupoId(grupo.getId());
                dto.setGrupoNombre(grupo.getNombre());
            }
        }
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getNotaPredefinidaId() {
        return notaPredefinidaId;
    }

    public void setNotaPredefinidaId(Long notaPredefinidaId) {
        this.notaPredefinidaId = notaPredefinidaId;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Long getGrupoId() {
        return grupoId;
    }

    public void setGrupoId(Long grupoId) {
        this.grupoId = grupoId;
    }

    public String getGrupoNombre() {
        return grupoNombre;
    }

    public void setGrupoNombre(String grupoNombre) {
        this.grupoNombre = grupoNombre;
    }
}
