package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.DocumentoFiscal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocumentoFiscalRepository extends JpaRepository<DocumentoFiscal, Long> {
    Optional<DocumentoFiscal> findByPedidoId(Long pedidoId);
}
