package com.facturacion.negocio.services.factus;

public record FactusTenantConfig(
        Long id,
        String baseUrl,
        String username,
        String password,
        String clientId,
        String clientSecret
) {
}
