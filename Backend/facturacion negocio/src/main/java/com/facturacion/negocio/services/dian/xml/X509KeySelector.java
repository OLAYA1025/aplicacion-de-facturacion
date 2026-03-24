package com.facturacion.negocio.services.dian.xml;

import javax.xml.crypto.*;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import java.security.PublicKey;
import java.security.cert.X509Certificate;

public class X509KeySelector extends KeySelector {

    @Override
    public KeySelectorResult select(
            KeyInfo keyInfo,
            Purpose purpose,
            AlgorithmMethod method,
            XMLCryptoContext context
    ) throws KeySelectorException {

        if (keyInfo == null) {
            throw new KeySelectorException("KeyInfo no encontrado");
        }

        for (Object info : keyInfo.getContent()) {
            XMLStructure xmlStructure = (XMLStructure) info;

            if (xmlStructure instanceof X509Data x509Data) {
                for (Object data : x509Data.getContent()) {
                    if (data instanceof X509Certificate certificate) {
                        PublicKey publicKey = certificate.getPublicKey();
                        return () -> publicKey;
                    }
                }
            }
        }

        throw new KeySelectorException("No se encontro certificado X509 en KeyInfo");
    }
}