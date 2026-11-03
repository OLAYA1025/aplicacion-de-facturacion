package com.facturacion.negocio.services.dian.client;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DianKeystorePasswordCallback implements CallbackHandler {

    private final String certificateAlias;
    private final String certificatePassword;

    public DianKeystorePasswordCallback(
            @Value("${DIAN_CERTIFICATE_ALIAS:}") String certificateAlias,
            @Value("${DIAN_CERTIFICATE_PASSWORD:}") String certificatePassword
    ) {
        this.certificateAlias = certificateAlias;
        this.certificatePassword = certificatePassword;
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof WSPasswordCallback pc) {
                if (certificateAlias.equals(pc.getIdentifier())) {
                    pc.setPassword(certificatePassword);
                }
            }
        }
    }
}
