package com.facturacion.negocio.dto.request;

import com.facturacion.negocio.enumerated.ProveedorFacturacion;

public record EditarConfiguracionFeRequest(ProveedorFacturacion proveedor,
                                           String baseUrl,
                                           String username,
                                           String password,
                                           String clientId,
                                           String clientSecret,
                                           String prefijoCode) {
}
