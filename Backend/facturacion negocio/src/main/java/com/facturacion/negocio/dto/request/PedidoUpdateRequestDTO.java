package com.facturacion.negocio.dto.request;

import java.util.List;

public class PedidoUpdateRequestDTO {
    private String nombre;
    private List<DetallePedidoRequestDTO> detalles;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public List<DetallePedidoRequestDTO> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetallePedidoRequestDTO> detalles) {
        this.detalles = detalles;
    }
}
