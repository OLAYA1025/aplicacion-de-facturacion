package com.facturacion.negocio.services.tirillaFctura;

import com.facturacion.negocio.enumerated.TipoImpuesto;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Impuesto {
    private BigDecimal impuestoPorcentaje;
    private TipoImpuesto tipoImpuesto;
    private BigDecimal base = BigDecimal.ZERO;
    private BigDecimal totalImpuesto =  BigDecimal.ZERO;

    public Impuesto(BigDecimal impuestoPorcentaje, TipoImpuesto tipoImpuesto){
        this.impuestoPorcentaje = impuestoPorcentaje;
        this.tipoImpuesto = tipoImpuesto;
    }

    public void sumarValorBase(BigDecimal base){
        if(base == null){
            base = BigDecimal.ZERO;
        }

        BigDecimal divisor = this.impuestoPorcentaje.add(new BigDecimal("100")).divide(new BigDecimal("100"),2, RoundingMode.HALF_UP);

        base = base.divide(divisor, 2, RoundingMode.HALF_UP);
        this.base = this.base.add(base);
    }



    public void calcularTotalImpuesto(){
        BigDecimal porcentaje = this.impuestoPorcentaje.divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);

        this.totalImpuesto = this.base.multiply(porcentaje).setScale(2, RoundingMode.HALF_UP);
    }


    public BigDecimal getImpuestoPorcentaje() {
        return impuestoPorcentaje;
    }

    public void setImpuestoPorcentaje(BigDecimal impuestoPorcentaje) {
        this.impuestoPorcentaje = impuestoPorcentaje;
    }

    public TipoImpuesto getTipoImpuesto() {
        return tipoImpuesto;
    }

    public void setTipoImpuesto(TipoImpuesto tipoImpuesto) {
        this.tipoImpuesto = tipoImpuesto;
    }

    public BigDecimal getBase() {
        return base.setScale(2, RoundingMode.HALF_UP);
    }

    public void setBase(BigDecimal base) {
        this.base = base;
    }

    public BigDecimal getTotalImpuesto() {
        return totalImpuesto.setScale(2, RoundingMode.HALF_UP);
    }

    public void setTotalImpuesto(BigDecimal totalImpuesto) {
        this.totalImpuesto = totalImpuesto;
    }
}
