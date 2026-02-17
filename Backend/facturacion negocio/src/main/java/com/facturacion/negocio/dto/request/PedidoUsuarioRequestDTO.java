package com.facturacion.negocio.dto.request;

import com.facturacion.negocio.enumerated.MetodoPago;

public class PedidoUsuarioRequestDTO {
    private Long usuarioId;
    private MetodoPago metodoPago;

    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }
}
