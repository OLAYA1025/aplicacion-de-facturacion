package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.domain.DocumentoFiscal;
import com.facturacion.negocio.enumerated.EstadoDocumentoFiscal;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DocumentoFiscalRepository extends JpaRepository<DocumentoFiscal, Long>, JpaSpecificationExecutor<DocumentoFiscal> {
    Optional<DocumentoFiscal> findByPedidoIdAndNegocio(Long pedidoId, Negocio negocio);

    Optional<DocumentoFiscal> findByIdAndNegocio(Long id, Negocio negocio);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DocumentoFiscal d where d.id = :id")
    Optional<DocumentoFiscal> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from DocumentoFiscal d where d.id = :id and d.negocio = :negocio")
    Optional<DocumentoFiscal> findByIdForUpdate(@Param("id") Long id, @Param("negocio") Negocio negocio);

    @EntityGraph(attributePaths = {
            "negocio",
            "numberingRange",
            "numberingRange.configuracion",
            "pedido",
            "pedido.detallePedidos",
            "pedido.detallePedidos.producto",
            "cliente"
    })
    @Query("select d from DocumentoFiscal d where d.id = :id")
    Optional<DocumentoFiscal> findByIdForEnvio(@Param("id") Long id);

    List<DocumentoFiscal> findTop20ByEstadoOrderByIdAsc(EstadoDocumentoFiscal estado);

    @EntityGraph(attributePaths = {"cliente", "pedido"})
    Page<DocumentoFiscal> findAll(Specification<DocumentoFiscal> spec, Pageable pageable);
}
