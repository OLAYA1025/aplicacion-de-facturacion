package com.facturacion.negocio.dto.response;

import com.facturacion.negocio.enumerated.EstadoPedido;
import com.facturacion.negocio.enumerated.MetodoPago;

import java.time.Instant;

public record PedidoListadoItemDTO(
        Long id,
        String nombre,
        Instant fecha,
        EstadoPedido estado,
        MetodoPago metodoPago,
        Long usuarioId,
        String usuarioUsername
) {
}
