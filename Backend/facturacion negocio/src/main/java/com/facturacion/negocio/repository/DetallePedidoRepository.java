package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.DetallePedido;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    @Query("""
            SELECT d.producto.codigo AS productoCodigo,
                   d.producto.nombre AS productoNombre,
                   SUM(d.cantidad) AS cantidadVendida
            FROM DetallePedido d
            WHERE d.pedido.estado = com.facturacion.negocio.enumerated.EstadoPedido.PAGO
              AND d.pedido.fecha >= :desde
              AND d.pedido.fecha < :hasta
            GROUP BY d.producto.codigo, d.producto.nombre
            ORDER BY SUM(d.cantidad) DESC, d.producto.nombre ASC
            """)
    List<TopProductoVendidoProjection> findTopProductosVendidos(
            @Param("desde") Instant desde,
            @Param("hasta") Instant hasta,
            Pageable pageable
    );

    @Query("""
            SELECT d.pedido.id AS pedidoId,
                   COALESCE(SUM(d.precioUnitario * d.cantidad), 0) AS total
            FROM DetallePedido d
            WHERE d.pedido.id IN :pedidoIds
            GROUP BY d.pedido.id
            """)
    List<PedidoTotalProjection> findTotalesPorPedidoIds(@Param("pedidoIds") List<Long> pedidoIds);

}
