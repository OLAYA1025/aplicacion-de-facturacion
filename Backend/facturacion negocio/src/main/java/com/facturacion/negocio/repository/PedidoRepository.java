package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.domain.DetallePedido;
import com.facturacion.negocio.domain.Pedido;
import com.facturacion.negocio.dto.response.PedidoListadoItemDTO;
import com.facturacion.negocio.enumerated.EstadoPedido;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    Optional<Pedido> findByIdAndNegocio(Long id, Negocio negocio);

    @Query("""
            SELECT DISTINCT d
            FROM DetallePedido d
            JOIN FETCH d.producto p
            LEFT JOIN FETCH d.detallePedidoNotas dn
            LEFT JOIN FETCH dn.notaPredefinida np
            LEFT JOIN FETCH np.grupo g
            WHERE d.pedido.id = :pedidoId
              AND d.pedido.negocio = :negocio
            """)
    List<DetallePedido> findDetallesPedidoByPedido(@Param("pedidoId") Long pedidoId,
                                                   @Param("negocio") Negocio negocio);

    @Query("""
            SELECT DISTINCT d
            FROM DetallePedido d
            JOIN FETCH d.producto p
            LEFT JOIN FETCH d.detallePedidoNotas dn
            LEFT JOIN FETCH dn.notaPredefinida np
            LEFT JOIN FETCH np.grupo g
            WHERE d.pedido.id IN :pedidoIds
              AND d.pedido.negocio = :negocio
            """)
    List<DetallePedido> findDetallesByPedidoIds(@Param("pedidoIds") List<Long> pedidoIds,
                                                @Param("negocio") Negocio negocio);

    @Query("""
            SELECT DISTINCT p
            FROM Pedido p
            LEFT JOIN FETCH p.detallePedidos d
            WHERE p.caja.estado = com.facturacion.negocio.enumerated.EstadoCaja.ABIERTA
              AND p.negocio = :negocio
            """)
    List<Pedido> findPedidosByCajaAbierta(@Param("negocio") Negocio negocio);

    @Query("""
            SELECT DISTINCT p
            FROM Pedido p
            LEFT JOIN FETCH p.detallePedidos d
            WHERE p.caja.id = :cajaId
              AND p.negocio = :negocio
            """)
    List<Pedido> findPedidosByCaja(@Param("cajaId") Long cajaId, @Param("negocio") Negocio negocio);

    @Query("SELECT new com.facturacion.negocio.dto.response.PedidoListadoItemDTO(" +
            "p.id, p.nombre, p.fecha, p.estado, p.metodoPago, u.id, u.username) " +
            "FROM Pedido p LEFT JOIN p.usuario u " +
            "WHERE p.caja.estado = com.facturacion.negocio.enumerated.EstadoCaja.ABIERTA " +
            "AND p.negocio = :negocio")
    List<PedidoListadoItemDTO> findResumenPedidosByCajaAbierta(@Param("negocio") Negocio negocio);

    @Query("SELECT new com.facturacion.negocio.dto.response.PedidoListadoItemDTO(" +
            "p.id, p.nombre, p.fecha, p.estado, p.metodoPago, u.id, u.username) " +
            "FROM Pedido p LEFT JOIN p.usuario u " +
            "WHERE p.caja.id = :cajaId AND p.negocio = :negocio")
    List<PedidoListadoItemDTO> findResumenPedidosByCaja(@Param("cajaId") Long cajaId,
                                                        @Param("negocio") Negocio negocio);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pedido p WHERE p.id = :id AND p.negocio = :negocio")
    Optional<Pedido> findByIdForUpdate(@Param("id") Long id, @Param("negocio") Negocio negocio);

    List<Pedido> findByEstadoAndNegocio(EstadoPedido estadoPedido, Negocio negocio);

    List<Pedido> findByFechaBetweenAndNegocio(
            Instant inicio,
            Instant fin,
            Negocio negocio
    );

}
