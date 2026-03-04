package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.Caja;
import com.facturacion.negocio.domain.DetallePedido;
import com.facturacion.negocio.domain.Pedido;
import com.facturacion.negocio.domain.Usuario;
import com.facturacion.negocio.dto.response.PedidoListadoItemDTO;
import com.facturacion.negocio.enumerated.EstadoPedido;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import java.time.LocalDateTime;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @Query("SELECT d FROM DetallePedido d JOIN FETCH d.producto WHERE d.pedido.id = :pedidoId")
    List<DetallePedido> findDetallesPedidoByPedido(@Param("pedidoId") Long pedidoId);

    @Query("SELECT d FROM DetallePedido d JOIN FETCH d.producto WHERE d.pedido.id IN :pedidoIds")
    List<DetallePedido> findDetallesByPedidoIds(@Param("pedidoIds") List<Long> pedidoIds);

    @Query("SELECT p FROM Pedido p WHERE p.caja.estado = com.facturacion.negocio.enumerated.EstadoCaja.ABIERTA")
    List<Pedido> findPedidosByCajaAbierta();

    @Query("SELECT p FROM Pedido p WHERE p.caja.id = :cajaId")
    List<Pedido> findPedidosByCaja(@Param("cajaId") Long cajaId);

    @Query("SELECT new com.facturacion.negocio.dto.response.PedidoListadoItemDTO(" +
            "p.id, p.nombre, p.fecha, p.estado, p.metodoPago, u.id, u.username) " +
            "FROM Pedido p LEFT JOIN p.usuario u " +
            "WHERE p.caja.estado = com.facturacion.negocio.enumerated.EstadoCaja.ABIERTA")
    List<PedidoListadoItemDTO> findResumenPedidosByCajaAbierta();

    @Query("SELECT new com.facturacion.negocio.dto.response.PedidoListadoItemDTO(" +
            "p.id, p.nombre, p.fecha, p.estado, p.metodoPago, u.id, u.username) " +
            "FROM Pedido p LEFT JOIN p.usuario u " +
            "WHERE p.caja.id = :cajaId")
    List<PedidoListadoItemDTO> findResumenPedidosByCaja(@Param("cajaId") Long cajaId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Pedido p WHERE p.id = :id")
    Optional<Pedido> findByIdForUpdate(@Param("id") Long id);

    List<Pedido> findByEstado(EstadoPedido estadoPedido);

    List<Pedido> findByFechaBetween(
            LocalDateTime inicio,
            LocalDateTime fin
    );

}
