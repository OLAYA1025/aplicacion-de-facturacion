package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.CompraDetalle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompraDetalleRepository extends JpaRepository<CompraDetalle, Long> {
}
