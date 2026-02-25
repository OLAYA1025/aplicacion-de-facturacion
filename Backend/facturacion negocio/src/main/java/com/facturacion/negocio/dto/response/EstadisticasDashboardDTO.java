package com.facturacion.negocio.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class EstadisticasDashboardDTO {

    private String modo;
    private LocalDate desde;
    private LocalDate hasta;
    private BigDecimal totalUtilidades;
    private Long totalCajasCerradas;
    private BigDecimal totalRetiros;
    private ResumenEstadisticasDTO graficoUtilidades;
    private TopProductosVendidosDTO graficoTopProductos;
    private List<HistorialCajaCerradaDTO> historialCajasCerradas;
    private PaginacionDTO paginacionCajasCerradas;
    private List<HistorialRetiroDTO> historialRetiros;
    private PaginacionDTO paginacionRetiros;

    public EstadisticasDashboardDTO(String modo,
                                    LocalDate desde,
                                    LocalDate hasta,
                                    BigDecimal totalUtilidades,
                                    Long totalCajasCerradas,
                                    BigDecimal totalRetiros,
                                    ResumenEstadisticasDTO graficoUtilidades,
                                    TopProductosVendidosDTO graficoTopProductos,
                                    List<HistorialCajaCerradaDTO> historialCajasCerradas,
                                    PaginacionDTO paginacionCajasCerradas,
                                    List<HistorialRetiroDTO> historialRetiros,
                                    PaginacionDTO paginacionRetiros) {
        this.modo = modo;
        this.desde = desde;
        this.hasta = hasta;
        this.totalUtilidades = totalUtilidades;
        this.totalCajasCerradas = totalCajasCerradas;
        this.totalRetiros = totalRetiros;
        this.graficoUtilidades = graficoUtilidades;
        this.graficoTopProductos = graficoTopProductos;
        this.historialCajasCerradas = historialCajasCerradas;
        this.paginacionCajasCerradas = paginacionCajasCerradas;
        this.historialRetiros = historialRetiros;
        this.paginacionRetiros = paginacionRetiros;
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

    public BigDecimal getTotalUtilidades() {
        return totalUtilidades;
    }

    public void setTotalUtilidades(BigDecimal totalUtilidades) {
        this.totalUtilidades = totalUtilidades;
    }

    public Long getTotalCajasCerradas() {
        return totalCajasCerradas;
    }

    public void setTotalCajasCerradas(Long totalCajasCerradas) {
        this.totalCajasCerradas = totalCajasCerradas;
    }

    public BigDecimal getTotalRetiros() {
        return totalRetiros;
    }

    public void setTotalRetiros(BigDecimal totalRetiros) {
        this.totalRetiros = totalRetiros;
    }

    public ResumenEstadisticasDTO getGraficoUtilidades() {
        return graficoUtilidades;
    }

    public void setGraficoUtilidades(ResumenEstadisticasDTO graficoUtilidades) {
        this.graficoUtilidades = graficoUtilidades;
    }

    public TopProductosVendidosDTO getGraficoTopProductos() {
        return graficoTopProductos;
    }

    public void setGraficoTopProductos(TopProductosVendidosDTO graficoTopProductos) {
        this.graficoTopProductos = graficoTopProductos;
    }

    public List<HistorialCajaCerradaDTO> getHistorialCajasCerradas() {
        return historialCajasCerradas;
    }

    public void setHistorialCajasCerradas(List<HistorialCajaCerradaDTO> historialCajasCerradas) {
        this.historialCajasCerradas = historialCajasCerradas;
    }

    public PaginacionDTO getPaginacionCajasCerradas() {
        return paginacionCajasCerradas;
    }

    public void setPaginacionCajasCerradas(PaginacionDTO paginacionCajasCerradas) {
        this.paginacionCajasCerradas = paginacionCajasCerradas;
    }

    public List<HistorialRetiroDTO> getHistorialRetiros() {
        return historialRetiros;
    }

    public void setHistorialRetiros(List<HistorialRetiroDTO> historialRetiros) {
        this.historialRetiros = historialRetiros;
    }

    public PaginacionDTO getPaginacionRetiros() {
        return paginacionRetiros;
    }

    public void setPaginacionRetiros(PaginacionDTO paginacionRetiros) {
        this.paginacionRetiros = paginacionRetiros;
    }
}
