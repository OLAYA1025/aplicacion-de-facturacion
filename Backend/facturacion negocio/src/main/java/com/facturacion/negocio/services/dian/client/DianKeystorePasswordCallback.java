package com.facturacion.negocio.services.dian.client;

import java.io.IOException;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.wss4j.common.ext.WSPasswordCallback;

public class DianKeystorePasswordCallback implements CallbackHandler {

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback callback : callbacks) {
            if (callback instanceof WSPasswordCallback pc) {
                if ("***REMOVED***".equals(pc.getIdentifier())) {
                    pc.setPassword("***REMOVED***");
                }
            }
        }
    }
}