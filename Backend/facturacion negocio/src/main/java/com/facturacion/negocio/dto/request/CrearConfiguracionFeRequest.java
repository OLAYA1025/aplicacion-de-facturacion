package com.facturacion.negocio.dto.request;

import com.facturacion.negocio.enumerated.ProveedorFacturacion;

public record CrearConfiguracionFeRequest(Long negocioId,
                                          ProveedorFacturacion proveedor,
                                          String baseUrl,
                                          String username,
                                          String password,
                                          String clientId,
                                          String clientSecret,
                                          String prefijoCode) {


}

