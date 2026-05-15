package com.facturacion.negocio.dto.request;

import com.facturacion.negocio.enumerated.TipoSeleccionNota;

public class GrupoNotaCategoriaRequestDTO {
    private String nombre;
    private Boolean obligatorio;
    private TipoSeleccionNota tipoSeleccion;

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
}
