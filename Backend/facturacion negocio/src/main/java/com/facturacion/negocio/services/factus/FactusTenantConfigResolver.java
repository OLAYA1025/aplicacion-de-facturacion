package com.facturacion.negocio.services.factus;

import com.facturacion.negocio.domain.ConfiguracionFacturacionElectronica;
import com.facturacion.negocio.domain.DocumentoFiscal;
import com.facturacion.negocio.enumerated.ProveedorFacturacion;
import com.facturacion.negocio.repository.ConfiguracionFacturacionElectronicaRepository;
import com.facturacion.negocio.security.EncryptionService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class FactusTenantConfigResolver {

    private final ConfiguracionFacturacionElectronicaRepository configuracionRepository;
    private final EncryptionService encryptionService;

    public FactusTenantConfigResolver(ConfiguracionFacturacionElectronicaRepository configuracionRepository,
                                      EncryptionService encryptionService) {
        this.configuracionRepository = configuracionRepository;
        this.encryptionService = encryptionService;
    }

    public ConfiguracionFacturacionElectronica configuracionPara(DocumentoFiscal doc) {
        Long negocioId = doc.getNegocio() != null ? doc.getNegocio().getId() : null;
        if (negocioId == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El documento fiscal no tiene negocio asociado");
        }

        ConfiguracionFacturacionElectronica config = configuracionRepository.findByNegocioId(negocioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El negocio no tiene configuracion de facturacion electronica"
                ));

        if (config.getProveedor() != ProveedorFacturacion.FACTUS) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La configuracion de facturacion electronica no usa Factus"
            );
        }

        return config;
    }

    public FactusTenantConfig factusConfigPara(DocumentoFiscal doc) {
        return toFactusTenantConfig(configuracionPara(doc));
    }

    public FactusTenantConfig toFactusTenantConfig(ConfiguracionFacturacionElectronica config) {
        return new FactusTenantConfig(
                config.getId(),
                config.getBaseUrl(),
                config.getUsername(),
                encryptionService.decrypt(config.getPassword()),
                config.getClientId(),
                encryptionService.decrypt(config.getClientSecret())
        );
    }
}
