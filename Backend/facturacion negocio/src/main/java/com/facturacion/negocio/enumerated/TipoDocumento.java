package com.facturacion.negocio.enumerated;

public enum TipoDocumento {
    NIT(6),
    CC(3),
    CE(4),
    PAS(7);

    private final int factusId;

    TipoDocumento(int factusId) {
        this.factusId = factusId;
    }

    public int getFactusId() {
        return factusId;
    }

}
