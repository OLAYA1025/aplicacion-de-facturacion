package com.facturacion.negocio.services;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.facturacion.negocio.domain.DetallePedido;
import com.facturacion.negocio.domain.Producto;
import com.facturacion.negocio.enumerated.TipoImpuesto;

public final class DetallePedidoAgrupador {

    private DetallePedidoAgrupador() {
    }

    public static List<DetallePedidoAgrupado> agruparPorProducto(List<DetallePedido> detalles) {
        Map<Long, DetallePedidoAgrupado> agrupados = new LinkedHashMap<>();
        if (detalles == null || detalles.isEmpty()) {
            return new ArrayList<>();
        }

        for (DetallePedido detalle : detalles) {
            if (detalle == null || detalle.getProducto() == null || detalle.getProducto().getId() == null) {
                continue;
            }

            Producto producto = detalle.getProducto();
            Long productoId = producto.getId();
            int cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : 0;
            if (cantidad <= 0) {
                continue;
            }

            DetallePedidoAgrupado acumulado = agrupados.get(productoId);
            if (acumulado == null) {
                BigDecimal precioUnitario = detalle.getPrecioUnitario() != null
                        ? detalle.getPrecioUnitario()
                        : BigDecimal.ZERO;
                agrupados.put(productoId, new DetallePedidoAgrupado(
                        producto,
                        cantidad,
                        precioUnitario,
                        detalle.getTipoImpuesto(),
                        detalle.getImpuestoPorcentaje()
                ));
            } else {
                acumulado.sumarCantidad(cantidad);
            }
        }

        return new ArrayList<>(agrupados.values());
    }

    public static final class DetallePedidoAgrupado {
        private final Producto producto;
        private final BigDecimal precioUnitario;
        private final TipoImpuesto tipoImpuesto;
        private final BigDecimal impuestoPorcentaje;
        private int cantidad;

        private DetallePedidoAgrupado(Producto producto,
                                      int cantidad,
                                      BigDecimal precioUnitario,
                                      TipoImpuesto tipoImpuesto,
                                      BigDecimal impuestoPorcentaje) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
            this.tipoImpuesto = tipoImpuesto;
            this.impuestoPorcentaje = impuestoPorcentaje;
        }

        public Producto getProducto() {
            return producto;
        }

        public BigDecimal getPrecioUnitario() {
            return precioUnitario;
        }

        public TipoImpuesto getTipoImpuesto() {
            return tipoImpuesto;
        }

        public BigDecimal getImpuestoPorcentaje() {
            return impuestoPorcentaje;
        }

        public int getCantidad() {
            return cantidad;
        }

        private void sumarCantidad(int cantidad) {
            this.cantidad += cantidad;
        }
    }
}
