package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.Compra;
import com.facturacion.negocio.enumerated.EstadoCompra;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface CompraRepository extends JpaRepository<Compra, Long> {

    boolean existsByProveedorIdAndNumeroFactura(Long proveedorId, String numeroFactura);

    @Override
    @EntityGraph(attributePaths = {"proveedor"})
    Page<Compra> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"proveedor"})
    Page<Compra> findByFechaCreacionGreaterThanEqual(Instant desde, Pageable pageable);

    @EntityGraph(attributePaths = {"proveedor"})
    Page<Compra> findByFechaCreacionLessThan(Instant hastaExclusiva, Pageable pageable);

    @EntityGraph(attributePaths = {"proveedor"})
    Page<Compra> findByFechaCreacionGreaterThanEqualAndFechaCreacionLessThan(Instant desde,
                                                                              Instant hastaExclusiva,
                                                                              Pageable pageable);

    @EntityGraph(attributePaths = {"proveedor"})
    Page<Compra> findByEstado(EstadoCompra estado, Pageable pageable);

    @EntityGraph(attributePaths = {"proveedor"})
    Page<Compra> findByEstadoAndFechaCreacionGreaterThanEqual(EstadoCompra estado,
                                                               Instant desde,
                                                               Pageable pageable);

    @EntityGraph(attributePaths = {"proveedor"})
    Page<Compra> findByEstadoAndFechaCreacionLessThan(EstadoCompra estado,
                                                       Instant hastaExclusiva,
                                                       Pageable pageable);

    @EntityGraph(attributePaths = {"proveedor"})
    Page<Compra> findByEstadoAndFechaCreacionGreaterThanEqualAndFechaCreacionLessThan(EstadoCompra estado,
                                                                                        Instant desde,
                                                                                        Instant hastaExclusiva,
                                                                                        Pageable pageable);

    @Query("""
            SELECT DISTINCT c FROM Compra c
            LEFT JOIN FETCH c.proveedor
            LEFT JOIN FETCH c.detalles d
            LEFT JOIN FETCH d.producto
            WHERE c.id = :id
            """)
    Optional<Compra> findByIdConDetalles(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Compra c WHERE c.id = :id")
    Optional<Compra> findByIdForUpdate(@Param("id") Long id);
}
