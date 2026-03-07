package com.facturacion.negocio.enumerated;

public enum TipoImpuesto {
    IVA(1),
    INC(4);

    private final int tributeId;

    TipoImpuesto(int tributeId) {
        this.tributeId = tributeId;
    }

    public int getTributeId() {
        return tributeId;
    }
}
