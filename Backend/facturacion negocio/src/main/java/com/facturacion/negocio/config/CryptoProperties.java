package com.facturacion.negocio.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.crypto")
public record CryptoProperties(
        String secret
) {}