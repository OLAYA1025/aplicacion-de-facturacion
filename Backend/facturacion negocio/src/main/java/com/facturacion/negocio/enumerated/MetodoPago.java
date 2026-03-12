package com.facturacion.negocio.enumerated;

public enum MetodoPago {
    EFECTIVO(10),
    TRANSFERENCIA(47),
    TARJETA_CREDITO(48),
    TARJETA_DEBITO(49);

    private Integer factusId;
    MetodoPago(Integer factusId) {
        this.factusId = factusId;
    }
    public Integer getfactusId() {
        return factusId;
    }
}
