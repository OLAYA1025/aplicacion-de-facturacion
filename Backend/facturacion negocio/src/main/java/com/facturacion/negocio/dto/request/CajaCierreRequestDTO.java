package com.facturacion.negocio.dto.request;

import java.math.BigDecimal;

public class CajaCierreRequestDTO {
    private Long idUsuarioCierre;
    private BigDecimal retiro;


    public Long getIdUsuarioCierre() {
        return idUsuarioCierre;
    }

    public void setIdUsuarioCierre(Long idUsuarioCierre) {
        this.idUsuarioCierre = idUsuarioCierre;
    }

    public BigDecimal getRetiro() {
        return retiro;
    }

    public void setRetiro(BigDecimal retiro) {
        this.retiro = retiro;
    }
}
