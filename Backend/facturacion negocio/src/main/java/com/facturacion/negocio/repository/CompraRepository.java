package com.facturacion.negocio.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.facturacion.negocio.domain.Compra;
import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.enumerated.EstadoCompra;

import jakarta.persistence.LockModeType;

public interface CompraRepository extends JpaRepository<Compra, Long> {

    boolean existsByProveedorIdAndNumeroFacturaAndNegocio(Long proveedorId, String numeroFactura, Negocio negocio);

    @Override
    @EntityGraph(attributePaths = {"proveedor"})
    Page<Compra> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"proveedor"})
    Page<Compra> findByFechaCreacionGreaterThanEqualAndNegocio(Instant desde, Negocio negocio, Pageable pageable);

    @EntityGraph(attributePaths = {"proveedor"})
    Page<Compra> findByFechaCreacionLessThanAndNegocio(Instant hastaExclusiva, Negocio negocio, Pageable pageable);

    @EntityGraph(attributePaths = {"proveedor"})
    Page<Compra> findByFechaCreacionGreaterThanEqualAndFechaCreacionLessThanAndNegocio(Instant desde,
                                                                                        Instant hastaExclusiva,
                                                                                        Negocio negocio,
                                                                                        Pageable pageable);

    @EntityGraph(attributePaths = {"proveedor"})
    Page<Compra> findByEstadoAndNegocio(EstadoCompra estado, Negocio negocio, Pageable pageable);

    @EntityGraph(attributePaths = {"proveedor"})
    Page<Compra> findByEstadoAndFechaCreacionGreaterThanEqualAndNegocio(EstadoCompra estado,
                                                                        Instant desde,
                                                                        Negocio negocio,
                                                                        Pageable pageable);

    @EntityGraph(attributePaths = {"proveedor"})
    Page<Compra> findByEstadoAndFechaCreacionLessThanAndNegocio(EstadoCompra estado,
                                                                Instant hastaExclusiva,
                                                                Negocio negocio,
                                                                Pageable pageable);

    @EntityGraph(attributePaths = {"proveedor"})
    Page<Compra> findByEstadoAndFechaCreacionGreaterThanEqualAndFechaCreacionLessThanAndNegocio(EstadoCompra estado,
                                                                                                 Instant desde,
                                                                                                 Instant hastaExclusiva,
                                                                                                 Negocio negocio,
                                                                                                 Pageable pageable);

    @Query("""
            SELECT COALESCE(SUM(c.total), 0)
            FROM Compra c
            WHERE c.estado = :estado
              AND c.negocio = :negocio
              AND c.fechaConfirmacion >= :desde
              AND c.fechaConfirmacion < :hasta
            """)
    BigDecimal calcularTotalConfirmadasPorFechaConfirmacionRango(@Param("estado") EstadoCompra estado,
                                                                  @Param("negocio") Negocio negocio,
                                                                  @Param("desde") Instant desde,
                                                                  @Param("hasta") Instant hasta);

    @Query("""
            SELECT DISTINCT c FROM Compra c
            LEFT JOIN FETCH c.proveedor
            LEFT JOIN FETCH c.detalles d
            LEFT JOIN FETCH d.producto
            WHERE c.id = :id
              AND c.negocio = :negocio
            """)
    Optional<Compra> findByIdConDetalles(@Param("id") Long id, @Param("negocio") Negocio negocio);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Compra c WHERE c.id = :id AND c.negocio = :negocio")
    Optional<Compra> findByIdForUpdate(@Param("id") Long id, @Param("negocio") Negocio negocio);
}
