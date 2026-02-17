package com.facturacion.negocio.dto.response;

import java.math.BigDecimal;

public class PedidoTotalResponseDTO {
    private Long pedidoId;
    private BigDecimal total;

    public static PedidoTotalResponseDTO from(Long pedidoId, BigDecimal total) {
        PedidoTotalResponseDTO dto = new PedidoTotalResponseDTO();
        dto.setPedidoId(pedidoId);
        dto.setTotal(total);
        return dto;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public void setPedidoId(Long pedidoId) {
        this.pedidoId = pedidoId;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
