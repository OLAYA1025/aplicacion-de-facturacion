package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.DocumentoFiscal;
import com.facturacion.negocio.enumerated.EstadoDocumentoFiscal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public interface DocumentoFiscalRepository extends JpaRepository<DocumentoFiscal, Long>, JpaSpecificationExecutor<DocumentoFiscal> {
    Optional<DocumentoFiscal> findByPedidoId(Long pedidoId);

    List<DocumentoFiscal> findTop50ByEstadoOrderByIdAsc(EstadoDocumentoFiscal estado);

    @EntityGraph(attributePaths = {"cliente", "pedido"})
    Page<DocumentoFiscal> findAll(Specification<DocumentoFiscal> spec, Pageable pageable);
}
