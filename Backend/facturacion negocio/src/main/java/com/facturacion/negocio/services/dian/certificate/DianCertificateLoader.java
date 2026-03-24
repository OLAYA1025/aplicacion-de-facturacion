package com.facturacion.negocio.services.dian.certificate;

import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Enumeration;

@Component
public class DianCertificateLoader {

    private final Resource certificateResource;
    private final String certificatePassword;

    public DianCertificateLoader(
            @Value("${dian.certificate.path}") Resource certificateResource,
            @Value("${dian.certificate.password}") String certificatePassword
    ) {
        this.certificateResource = certificateResource;
        this.certificatePassword = certificatePassword;
    }

    public DianCertificateData load() {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");

            try (InputStream is = certificateResource.getInputStream()) {
                keyStore.load(is, certificatePassword.toCharArray());
            }

            Enumeration<String> aliases = keyStore.aliases();
            if (!aliases.hasMoreElements()) {
                throw new IllegalStateException("El certificado PKCS12 no contiene alias");
            }

            String alias = aliases.nextElement();

            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, certificatePassword.toCharArray());
            if (privateKey == null) {
                throw new IllegalStateException("No se pudo obtener la llave privada del certificado");
            }

            X509Certificate certificate = (X509Certificate) keyStore.getCertificate(alias);
            if (certificate == null) {
                throw new IllegalStateException("No se pudo obtener el certificado X509");
            }

            return new DianCertificateData(alias, privateKey, certificate);

        } catch (Exception e) {
            throw new IllegalStateException("Error cargando el certificado DIAN", e);
        }
    }
}