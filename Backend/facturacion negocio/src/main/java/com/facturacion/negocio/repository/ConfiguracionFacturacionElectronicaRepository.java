package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.ConfiguracionFacturacionElectronica;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ConfiguracionFacturacionElectronicaRepository
        extends JpaRepository<ConfiguracionFacturacionElectronica, Long> {


    Optional<ConfiguracionFacturacionElectronica> findByNegocioId(Long negocioId);
}