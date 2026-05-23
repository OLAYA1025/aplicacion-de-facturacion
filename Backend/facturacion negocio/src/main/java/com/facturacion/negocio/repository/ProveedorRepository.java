package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.domain.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {

    boolean existsByCorreoIgnoreCaseAndNegocio(String correo, Negocio negocio);

    boolean existsByCorreoIgnoreCaseAndIdNotAndNegocio(String correo, Long id, Negocio negocio);

    Optional<Proveedor> findByIdAndNegocio(Long id, Negocio negocio);

    List<Proveedor> findAllByNegocioOrderByNombreAsc(Negocio negocio);
}
