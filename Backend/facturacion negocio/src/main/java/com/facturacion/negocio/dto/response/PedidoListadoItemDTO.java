package com.facturacion.negocio.dto.response;

import com.facturacion.negocio.enumerated.EstadoPedido;
import com.facturacion.negocio.enumerated.MetodoPago;

import java.time.LocalDateTime;

public record PedidoListadoItemDTO(
        Long id,
        String nombre,
        LocalDateTime fecha,
        EstadoPedido estado,
        MetodoPago metodoPago,
        Long usuarioId,
        String usuarioUsername
) {
}
