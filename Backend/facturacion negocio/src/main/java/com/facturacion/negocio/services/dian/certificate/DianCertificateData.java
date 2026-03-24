package com.facturacion.negocio.services.dian.certificate;

import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public record DianCertificateData(
        String alias,
        PrivateKey privateKey,
        X509Certificate certificate
) {
}