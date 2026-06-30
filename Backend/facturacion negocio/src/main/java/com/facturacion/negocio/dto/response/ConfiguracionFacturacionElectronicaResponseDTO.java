package com.facturacion.negocio.dto.response;

import com.facturacion.negocio.domain.ConfiguracionFacturacionElectronica;
import com.facturacion.negocio.domain.NumberingRange;
import com.facturacion.negocio.enumerated.ProveedorFacturacion;
import com.facturacion.negocio.security.EncryptionService;

import java.util.List;

public class ConfiguracionFacturacionElectronicaResponseDTO {

    private Long id;
    private Long negocioId;
    private ProveedorFacturacion proveedor;
    private String baseUrl;
    private String username;
    private String password;
    private String clientId;
    private String clientSecret;
    private String prefijoCode;
    private List<NumberingRangeResponseDTO> numberingRanges;

    public static ConfiguracionFacturacionElectronicaResponseDTO from(
            ConfiguracionFacturacionElectronica configuracion,
            List<NumberingRange> numberingRanges,
            EncryptionService encryptionService
    ) {
        ConfiguracionFacturacionElectronicaResponseDTO dto =
                new ConfiguracionFacturacionElectronicaResponseDTO();

        dto.id = configuracion.getId();
        dto.negocioId = configuracion.getNegocio().getId();
        dto.proveedor = configuracion.getProveedor();
        dto.baseUrl = configuracion.getBaseUrl();
        dto.username = configuracion.getUsername();
        dto.password = encryptionService.decrypt(configuracion.getPassword());
        dto.clientId = configuracion.getClientId();
        dto.clientSecret = encryptionService.decrypt(configuracion.getClientSecret());
        dto.prefijoCode = configuracion.getPrefijoCode();
        dto.numberingRanges = numberingRanges
                .stream()
                .map(NumberingRangeResponseDTO::from)
                .toList();

        return dto;
    }

    public Long getId() {
        return id;
    }

    public Long getNegocioId() {
        return negocioId;
    }

    public ProveedorFacturacion getProveedor() {
        return proveedor;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getPrefijoCode() {
        return prefijoCode;
    }

    public List<NumberingRangeResponseDTO> getNumberingRanges() {
        return numberingRanges;
    }
}
