package com.facturacion.negocio.enumerated;

public enum TipoImpuesto {
    IVA(1, "01"),
    INC(4, "04");

    private final int tributeId; // usado por Factus
    private final String IdDian; // usado por Dian


    TipoImpuesto(int tributeId,  String IdDian) {
        this.tributeId = tributeId;
        this.IdDian = IdDian;
    }

    public int getTributeId() {
        return tributeId;
    }

    public String getIdDian() {
        return IdDian;
    }
}
