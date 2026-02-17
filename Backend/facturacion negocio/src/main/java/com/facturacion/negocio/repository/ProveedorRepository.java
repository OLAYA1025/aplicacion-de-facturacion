package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    boolean existsByCorreoIgnoreCase(String correo);
}
