package com.facturacion.negocio.dto.response;

import com.facturacion.negocio.domain.GrupoNotaCategoria;
import com.facturacion.negocio.domain.NotaPredefinida;
import com.facturacion.negocio.enumerated.TipoSeleccionNota;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GrupoNotaCategoriaResponseDTO {
    private Long id;
    private String nombre;
    private Boolean obligatorio;
    private TipoSeleccionNota tipoSeleccion;
    private Long categoriaId;
    private List<NotaPredefinidaResponseDTO> notas = new ArrayList<>();

    public static GrupoNotaCategoriaResponseDTO from(GrupoNotaCategoria grupo) {
        GrupoNotaCategoriaResponseDTO dto = new GrupoNotaCategoriaResponseDTO();
        dto.setId(grupo.getId());
        dto.setNombre(grupo.getNombre());
        dto.setObligatorio(grupo.getObligatorio());
        dto.setTipoSeleccion(grupo.getTipoSeleccion());
        dto.setCategoriaId(grupo.getCategoria() != null ? grupo.getCategoria().getId() : null);

        List<NotaPredefinida> notasOrdenadas = new ArrayList<>(grupo.getNotas());
        notasOrdenadas.sort(Comparator.comparing(NotaPredefinida::getDescripcion, String.CASE_INSENSITIVE_ORDER));
        List<NotaPredefinidaResponseDTO> notasDto = new ArrayList<>(notasOrdenadas.size());
        for (NotaPredefinida nota : notasOrdenadas) {
            notasDto.add(NotaPredefinidaResponseDTO.from(nota));
        }
        dto.setNotas(notasDto);
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

    public Boolean getObligatorio() {
        return obligatorio;
    }

    public void setObligatorio(Boolean obligatorio) {
        this.obligatorio = obligatorio;
    }

    public TipoSeleccionNota getTipoSeleccion() {
        return tipoSeleccion;
    }

    public void setTipoSeleccion(TipoSeleccionNota tipoSeleccion) {
        this.tipoSeleccion = tipoSeleccion;
    }

    public Long getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(Long categoriaId) {
        this.categoriaId = categoriaId;
    }

    public List<NotaPredefinidaResponseDTO> getNotas() {
        return notas;
    }

    public void setNotas(List<NotaPredefinidaResponseDTO> notas) {
        this.notas = notas;
    }
}
