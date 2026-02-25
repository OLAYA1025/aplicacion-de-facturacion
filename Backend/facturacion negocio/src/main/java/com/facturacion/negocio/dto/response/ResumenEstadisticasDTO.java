package com.facturacion.negocio.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class ResumenEstadisticasDTO {

    private String modo;
    private LocalDate desde;
    private LocalDate hasta;
    private List<String> labels;
    private List<BigDecimal> values;
    private BigDecimal total;

    public ResumenEstadisticasDTO(String modo,
                                  LocalDate desde,
                                  LocalDate hasta,
                                  List<String> labels,
                                  List<BigDecimal> values,
                                  BigDecimal total) {
        this.modo = modo;
        this.desde = desde;
        this.hasta = hasta;
        this.labels = labels;
        this.values = values;
        this.total = total;
    }

    public String getModo() {
        return modo;
    }

    public void setModo(String modo) {
        this.modo = modo;
    }

    public LocalDate getDesde() {
        return desde;
    }

    public void setDesde(LocalDate desde) {
        this.desde = desde;
    }

    public LocalDate getHasta() {
        return hasta;
    }

    public void setHasta(LocalDate hasta) {
        this.hasta = hasta;
    }

    public List<String> getLabels() {
        return labels;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public List<BigDecimal> getValues() {
        return values;
    }

    public void setValues(List<BigDecimal> values) {
        this.values = values;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

}
