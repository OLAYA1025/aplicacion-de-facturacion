package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.Caja;
import com.facturacion.negocio.domain.MovimientoCaja;
import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.domain.Usuario;
import com.facturacion.negocio.dto.response.MovimientoCajaResponseDTO;
import com.facturacion.negocio.enumerated.TipoMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovimientoCajaRepository extends JpaRepository<MovimientoCaja, Long> {

    @Override
    @EntityGraph(attributePaths = {"usuario", "caja"})
    Optional<MovimientoCaja> findById(Long id);

    @EntityGraph(attributePaths = {"usuario", "caja"})
    Optional<MovimientoCaja> findByIdAndNegocio(Long id, Negocio negocio);

    @EntityGraph(attributePaths = {"usuario", "caja"})
    @Query("SELECT m FROM MovimientoCaja m WHERE m.tipo = :tipoMovimiento AND m.negocio = :negocio")
    List<MovimientoCaja> findByTipoMovimiento(@Param("tipoMovimiento") TipoMovimiento tipoMovimiento,
                                              @Param("negocio") Negocio negocio);

    @Query("""
            SELECT COALESCE(SUM(m.monto), 0)
            FROM MovimientoCaja m
            WHERE m.caja = :caja
              AND m.tipo = :tipoMovimiento
              AND m.negocio = :negocio
            """)
    BigDecimal calcularTotalesByTipoAndCaja(@Param("tipoMovimiento") TipoMovimiento tipoMovimiento,
                                            @Param("caja") Caja caja,
                                            @Param("negocio") Negocio negocio);

    @Query("""
            SELECT COALESCE(SUM(m.monto),0)
            FROM MovimientoCaja m
            WHERE m.tipo = :tipoMovimiento
              AND m.negocio = :negocio
            """)
    BigDecimal calcularTotalByMovimiento(@Param("tipoMovimiento") TipoMovimiento tipoMovimiento,
                                         @Param("negocio") Negocio negocio);

    @Query("SELECT COALESCE(SUM(m.monto),0) FROM MovimientoCaja m " +
            "WHERE m.tipo = :tipoMovimiento AND m.negocio = :negocio AND m.fecha >= :desde AND m.fecha < :hasta")
    BigDecimal calcularTotalByMovimientoAndFechaRango(@Param("tipoMovimiento") TipoMovimiento tipoMovimiento,
                                                       @Param("negocio") Negocio negocio,
                                                       @Param("desde") Instant desde,
                                                       @Param("hasta") Instant hasta);

    @EntityGraph(attributePaths = {"usuario", "caja"})
    @Query("""
            SELECT m
            FROM MovimientoCaja m
            WHERE m.tipo = :tipoMovimiento
              AND m.negocio = :negocio
              AND m.fecha BETWEEN :desde AND :hasta
            """)
    List<MovimientoCaja> findByTipoMovimientoAndDateBetween(
            @Param("tipoMovimiento") TipoMovimiento tipoMovimiento,
            @Param("negocio") Negocio negocio,
            @Param("desde") Instant desde,
            @Param("hasta") Instant hasta);

    @EntityGraph(attributePaths = {"usuario", "caja"})
    Page<MovimientoCaja> findByTipoAndFechaGreaterThanEqualAndFechaLessThanAndNegocioOrderByFechaDesc(
            TipoMovimiento tipo,
            Instant desdeInclusive,
            Instant hastaExclusive,
            Negocio negocio,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"usuario", "caja"})
    @Query("SELECT m FROM MovimientoCaja m WHERE m.negocio = :negocio AND m.fecha BETWEEN :desde AND :hasta")
    List<MovimientoCaja> findByDateBetween(
            @Param("negocio") Negocio negocio,
            @Param("desde") Instant desde,
            @Param("hasta") Instant hasta);

    @EntityGraph(attributePaths = {"usuario", "caja"})
    @Query("SELECT m FROM MovimientoCaja m WHERE m.usuario = :usuario AND m.negocio = :negocio")
    List<MovimientoCaja> findByUsuario(@Param("usuario") Usuario usuario, @Param("negocio") Negocio negocio);

    @EntityGraph(attributePaths = {"usuario", "caja"})
    @Query("SELECT m FROM MovimientoCaja m WHERE m.caja = :caja AND m.negocio = :negocio")
    List<MovimientoCaja> findByCaja(@Param("caja") Caja caja, @Param("negocio") Negocio negocio);

    @Query("SELECT new com.facturacion.negocio.dto.response.MovimientoCajaResponseDTO(" +
            "m.id, m.monto, m.tipo, m.usuario.username, m.motivo, m.fecha) " +
            "FROM MovimientoCaja m WHERE m.caja = :caja AND m.negocio = :negocio ORDER BY m.fecha DESC")
    List<MovimientoCajaResponseDTO> findResumenByCaja(@Param("caja") Caja caja, @Param("negocio") Negocio negocio);
    /*
    List<MovimientoCaja> findByTipoMovimientoAndUsuario(
            Usuario usuario,
            TipoMovimiento tipoMovimiento);
            */


}
