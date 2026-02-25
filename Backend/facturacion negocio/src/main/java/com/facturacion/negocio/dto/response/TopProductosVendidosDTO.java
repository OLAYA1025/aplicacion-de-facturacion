package com.facturacion.negocio.dto.response;

import java.time.LocalDate;
import java.util.List;

public class TopProductosVendidosDTO {

    private String modo;
    private LocalDate desde;
    private LocalDate hasta;
    private List<String> labels;
    private List<Long> values;
    private Long totalVendidos;
    private List<TopProductoVendidoDTO> productos;

    public TopProductosVendidosDTO(String modo,
                                   LocalDate desde,
                                   LocalDate hasta,
                                   List<String> labels,
                                   List<Long> values,
                                   Long totalVendidos,
                                   List<TopProductoVendidoDTO> productos) {
        this.modo = modo;
        this.desde = desde;
        this.hasta = hasta;
        this.labels = labels;
        this.values = values;
        this.totalVendidos = totalVendidos;
        this.productos = productos;
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

    public List<Long> getValues() {
        return values;
    }

    public void setValues(List<Long> values) {
        this.values = values;
    }

    public Long getTotalVendidos() {
        return totalVendidos;
    }

    public void setTotalVendidos(Long totalVendidos) {
        this.totalVendidos = totalVendidos;
    }

    public List<TopProductoVendidoDTO> getProductos() {
        return productos;
    }

    public void setProductos(List<TopProductoVendidoDTO> productos) {
        this.productos = productos;
    }
}
