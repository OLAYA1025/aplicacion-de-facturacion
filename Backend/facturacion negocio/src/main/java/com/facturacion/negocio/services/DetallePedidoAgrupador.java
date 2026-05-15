package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.DetallePedido;
import com.facturacion.negocio.domain.Producto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class DetallePedidoAgrupador {

    private DetallePedidoAgrupador() {
    }

    public static List<DetallePedidoAgrupado> agruparPorProducto(List<DetallePedido> detalles) {
        Map<Long, DetallePedidoAgrupado> agrupados = new LinkedHashMap<>();
        if (detalles == null || detalles.isEmpty()) {
            return new ArrayList<>();
        }

        for (DetallePedido detalle : detalles) {
            if (detalle == null || detalle.getProducto() == null || detalle.getProducto().getCodigo() == null) {
                continue;
            }

            Producto producto = detalle.getProducto();
            Long productoCodigo = producto.getCodigo();
            int cantidad = detalle.getCantidad() != null ? detalle.getCantidad() : 0;
            if (cantidad <= 0) {
                continue;
            }

            DetallePedidoAgrupado acumulado = agrupados.get(productoCodigo);
            if (acumulado == null) {
                BigDecimal precioUnitario = detalle.getPrecioUnitario() != null
                        ? detalle.getPrecioUnitario()
                        : BigDecimal.ZERO;
                agrupados.put(productoCodigo, new DetallePedidoAgrupado(producto, cantidad, precioUnitario));
            } else {
                acumulado.sumarCantidad(cantidad);
            }
        }

        return new ArrayList<>(agrupados.values());
    }

    public static final class DetallePedidoAgrupado {
        private final Producto producto;
        private final BigDecimal precioUnitario;
        private int cantidad;

        private DetallePedidoAgrupado(Producto producto, int cantidad, BigDecimal precioUnitario) {
            this.producto = producto;
            this.cantidad = cantidad;
            this.precioUnitario = precioUnitario;
        }

        public Producto getProducto() {
            return producto;
        }

        public BigDecimal getPrecioUnitario() {
            return precioUnitario;
        }

        public int getCantidad() {
            return cantidad;
        }

        private void sumarCantidad(int cantidad) {
            this.cantidad += cantidad;
        }
    }
}
