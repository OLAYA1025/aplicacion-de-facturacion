package com.facturacion.negocio.enumerated;

public enum MetodoPago {
    EFECTIVO(10),
    TRANSFERENCIA(47),
    TARJETA(48);

    private Integer factusId;
    MetodoPago(Integer factusId) {
        this.factusId = factusId;
    }
    public Integer getfactusId() {
        return factusId;
    }
}
