package com.facturacion.negocio.enumerated;

public enum TipoDocumento {
    NIT(6, "31", 2),
    CC(3, "13", 1),
    CE(4, "22", 1),
    PAS(7, "41", 1);

    private final String dianCode;
    private final int factusId;
    private final int legalOrganizacionId;

    TipoDocumento(int factusId, String dianCode,  int legalOrganizacionId) {
        this.factusId = factusId;
        this.dianCode = dianCode;
        this.legalOrganizacionId = legalOrganizacionId;
    }

    public int getFactusId() {
        return factusId;
    }

    public String getDianCode() {
        return dianCode;
    }
    public int getLegalOrganizacionId() {
        return legalOrganizacionId;
    }
}
