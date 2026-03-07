package com.facturacion.negocio.services.tirillaFctura;

import com.facturacion.negocio.domain.Cliente;
import com.facturacion.negocio.domain.DetallePedido;
import com.facturacion.negocio.domain.DocumentoFiscal;
import com.facturacion.negocio.dto.factura.Establecimiento;
import com.facturacion.negocio.enumerated.TipoImpuesto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Factura {
    private String empresa;
    private String nit;
    private String direccion;
    private String numeroFactura;
    private String fechaHora;
    private String cajero;
    private String cliente;
    private String identificacion;
    private List<FacturaItem> items;
    private List<Impuesto> impuestos;
    private BigDecimal valorBase =  BigDecimal.ZERO;
    private BigDecimal totalImpuestos = BigDecimal.ZERO;
    private BigDecimal total =  BigDecimal.ZERO;


    /*
    private String resolucion;
    private String rangoDesde;
    private String rangoHasta;
    private String vigencia;

     */
    private String qr;
    private String cufe;


    private static final DateTimeFormatter FACTURA_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final ZoneId ZONA_COLOMBIA =
            ZoneId.of("America/Bogota");

    protected Factura() {

    }


    public Factura(DocumentoFiscal doc, Establecimiento est) {
        this.setEmpresa(est.getName());
        this.setCajero(doc.getPedido().getUsuario().getNombre());
        this.setDireccion(est.getAddress());
        this.setNit(est.getNit());
        this.setNumeroFactura(doc.getNumeroDocumento());
        this.setFechaHora(
                doc.getAceptadoAt()
                        .atZone(ZONA_COLOMBIA)
                        .format(FACTURA_FORMAT)
        );
        Cliente cliente = doc.getCliente();
        this.setCliente(cliente.getNombre());
        this.setIdentificacion(cliente.getNumeroIdentificacion());
        this.listarItemsAndCalcularImpuestos(doc.getPedido().getDetallePedidos());
        this.setTotal(doc.getPedido().calcularTotal());
        this.setQr(doc.getQr());
        this.setCufe(doc.getCufe());
    }


    public void listarItemsAndCalcularImpuestos(List<DetallePedido> detalles) {
        List<FacturaItem> items = new ArrayList<>();
        List<Impuesto> impuestos = new ArrayList<>();
        List<TipoImpuesto> tipoImpuestos = new ArrayList<>();
        for (DetallePedido detalle : detalles) {
            FacturaItem item = FacturaItem.from(detalle);
           if(!tipoImpuestos.contains(item.getTipoImpuesto())){
               tipoImpuestos.add(item.getTipoImpuesto());
               impuestos.add(new Impuesto(item.getImpuesto(), item.getTipoImpuesto()));
           }
           for(Impuesto impuesto : impuestos){
               if(item.getTipoImpuesto().equals(impuesto.getTipoImpuesto())){
                   impuesto.sumarValorBase(item.calcularSubtotal());
               }
           }

            items.add(item);
        }

        for(Impuesto impuesto : impuestos){
            impuesto.calcularTotalImpuesto();
            this.valorBase = this.valorBase.add(impuesto.getBase());
            this.totalImpuestos = this.totalImpuestos.add(impuesto.getTotalImpuesto());
        }
        this.impuestos = impuestos;
        this.items = items;

    }

    public String getQr() {
        return qr;
    }

    public void setQr(String qr) {
        this.qr = qr;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getNit() {
        return nit;
    }

    public void setNit(String nit) {
        this.nit = nit;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }



    public String getNumeroFactura() {
        return numeroFactura;
    }

    public void setNumeroFactura(String numeroFactura) {
        this.numeroFactura = numeroFactura;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getCajero() {
        return cajero;
    }

    public void setCajero(String cajero) {
        this.cajero = cajero;
    }

    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public void setIdentificacion(String identificacion) {
        this.identificacion = identificacion;
    }

    public List<FacturaItem> getItems() {
        return items;
    }

    public void setItems(List<FacturaItem> items) {
        this.items = items;
    }

    public List<Impuesto> getImpuestos() {
        return impuestos;
    }

    public void setImpuestos(List<Impuesto> impuestos) {
        this.impuestos = impuestos;
    }

    public BigDecimal getValorBase() {
        return valorBase;
    }

    public void setValorBase(BigDecimal valorBase) {
        this.valorBase = valorBase;
    }

    public BigDecimal getTotalImpuestos() {
        return totalImpuestos;
    }

    public void setTotalImpuestos(BigDecimal totalImpuestos) {
        this.totalImpuestos = totalImpuestos;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getCufe() {
        return cufe;
    }

    public void setCufe(String cufe) {
        this.cufe = cufe;
    }
}