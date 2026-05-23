package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.Negocio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NegocioRepository extends JpaRepository<Negocio, Long> {

    Optional<Negocio> findFirstByActivoTrueOrderByIdAsc();

    List<Negocio> findAllByOrderByIdAsc();

    boolean existsByIdAndActivoTrue(Long id);
}
