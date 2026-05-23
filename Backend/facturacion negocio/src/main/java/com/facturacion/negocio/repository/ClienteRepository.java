package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.Cliente;
import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.enumerated.EstadoDocumentoFiscal;
import com.facturacion.negocio.enumerated.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    Optional<Cliente> findByTipoIdentificacionAndNumeroIdentificacionAndNegocio(
            TipoDocumento tipo,
            String numero,
            Negocio negocio
    );

    Optional<Cliente> findByIdAndNegocio(Long id, Negocio negocio);
}
