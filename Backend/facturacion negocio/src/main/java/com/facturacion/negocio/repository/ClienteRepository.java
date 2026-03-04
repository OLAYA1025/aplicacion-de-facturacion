package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.Cliente;
import com.facturacion.negocio.domain.DocumentoFiscal;
import com.facturacion.negocio.enumerated.EstadoDocumentoFiscal;
import com.facturacion.negocio.enumerated.TipoDocumento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {



    Optional<Cliente> findByTipoIdentificacionAndNumeroIdentificacion(TipoDocumento tipo, String numero);
}
