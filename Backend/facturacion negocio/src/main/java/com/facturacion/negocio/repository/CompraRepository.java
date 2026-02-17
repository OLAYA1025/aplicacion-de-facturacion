package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.Compra;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CompraRepository extends JpaRepository<Compra, Long> {

    boolean existsByProveedorIdAndNumeroFactura(Long proveedorId, String numeroFactura);

    @Query("""
            SELECT DISTINCT c FROM Compra c
            LEFT JOIN FETCH c.proveedor
            LEFT JOIN FETCH c.detalles d
            LEFT JOIN FETCH d.producto
            ORDER BY c.id DESC
            """)
    List<Compra> findAllConDetalles();

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
