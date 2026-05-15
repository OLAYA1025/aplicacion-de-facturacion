package com.facturacion.negocio.services.tirillaFactura;


import com.facturacion.negocio.domain.DetallePedido;
import com.facturacion.negocio.domain.Pedido;
import com.facturacion.negocio.services.DetallePedidoAgrupador;
import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;


public class PreFactura {
    private String fechaHora;
    private List<FacturaItem> items;
    private BigDecimal total =  BigDecimal.ZERO;



    private static final DateTimeFormatter FACTURA_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final ZoneId ZONA_COLOMBIA =
            ZoneId.of("America/Bogota");

    protected PreFactura() {

    }


    public PreFactura(Pedido pedido) {
        this.setFechaHora(
                pedido.getFecha()
                        .atZone(ZONA_COLOMBIA)
                        .format(FACTURA_FORMAT)
        );

        this.setTotal(pedido.calcularTotal());
        this.listarItems(pedido.getDetallePedidos());
    }


    public void listarItems(List<DetallePedido> detalles) {
        List<FacturaItem> items = new ArrayList<>();

        List<DetallePedidoAgrupador.DetallePedidoAgrupado> detallesAgrupados = DetallePedidoAgrupador.agruparPorProducto(detalles);
        for (DetallePedidoAgrupador.DetallePedidoAgrupado detalleAgrupado : detallesAgrupados) {
            FacturaItem item = FacturaItem.from(
                    detalleAgrupado.getProducto(),
                    detalleAgrupado.getCantidad(),
                    detalleAgrupado.getPrecioUnitario()
            );
            items.add(item);

        }

        this.items = items;
    }

    public String getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(String fechaHora) {
        this.fechaHora = fechaHora;
    }

    public List<FacturaItem> getItems() {
        return items;
    }

    public void setItems(List<FacturaItem> items) {
        this.items = items;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }
}
