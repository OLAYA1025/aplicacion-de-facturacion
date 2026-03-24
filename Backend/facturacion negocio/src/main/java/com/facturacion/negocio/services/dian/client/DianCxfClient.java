package com.facturacion.negocio.services.dian.client;

import colombia.dian.wcf.IWcfDianCustomerServices;
import colombia.dian.wcf.WcfDianCustomerServices;

import jakarta.xml.soap.SOAPMessage;

import jakarta.xml.ws.BindingProvider;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.binding.soap.interceptor.AbstractSoapInterceptor;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
//import org.apache.cxf.rt.security.SecurityConstants;
import org.apache.cxf.phase.Phase;
import org.apache.cxf.ws.security.SecurityConstants;
import org.apache.cxf.ws.security.wss4j.WSS4JOutInterceptor;
import org.apache.wss4j.common.ConfigurationConstants;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import jakarta.xml.soap.SOAPBody;


import javax.xml.namespace.QName;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.lang.System.out;

@Component
public class DianCxfClient {



    public String sendTestSetAsync(byte[] zipBytes, String zipName, String testSetId) {

        WcfDianCustomerServices service = new WcfDianCustomerServices();
        IWcfDianCustomerServices port = service.getWSHttpBindingIWcfDianCustomerServices();

        Client client = ClientProxy.getClient(port);

        client.getOutInterceptors().add(new AbstractSoapInterceptor(Phase.PREPARE_SEND) {

            @Override
            public void handleMessage(SoapMessage message) {

                try {

                    org.w3c.dom.Element body =
                            message.getContent(org.w3c.dom.Document.class)
                                    .getDocumentElement()
                                    .getElementsByTagNameNS(
                                            "http://www.w3.org/2003/05/soap-envelope",
                                            "Body"
                                    )
                                    .item(0);

                    body.setAttributeNS(
                            "http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd",
                            "wsu:Id",
                            "id-Body"
                    );

                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        });


        LoggingOutInterceptor out = new LoggingOutInterceptor();
        out.setPrettyLogging(true);

        LoggingInInterceptor in = new LoggingInInterceptor();
        in.setPrettyLogging(true);

        client.getOutInterceptors().add(out);
        client.getInInterceptors().add(in);

        BindingProvider bp = (BindingProvider) port;

        bp.getRequestContext().put(
                SecurityConstants.ASYMMETRIC_SIGNATURE_ALGORITHM,
                "http://www.w3.org/2001/04/xmldsig-more#rsa-sha256"
        );

        bp.getRequestContext().put(SecurityConstants.SIGNATURE_PROPERTIES, "***REMOVED***");
        bp.getRequestContext().put(SecurityConstants.SIGNATURE_USERNAME, "***REMOVED***");
        bp.getRequestContext().put(SecurityConstants.CALLBACK_HANDLER, new DianKeystorePasswordCallback());

        bp.getRequestContext().put("ws-security.signature.parts",
                "{Element}{http://www.w3.org/2003/05/soap-envelope}Body;" +
                        "{Element}{http://www.w3.org/2005/08/addressing}To;" +
                        "{Element}{http://docs.oasis-open.org/wss/2004/01/oasis-200401-wss-wssecurity-utility-1.0.xsd}Timestamp"
        );

        bp.getRequestContext().put(
                "ws-security.signature.digest.algorithm",
                "http://www.w3.org/2001/04/xmlenc#sha256"
        );

        bp.getRequestContext().put(
                "ws-security.signature.c14n.algorithm",
                "http://www.w3.org/2001/10/xml-exc-c14n#"
        );

        bp.getRequestContext().put(
                SecurityConstants.IS_BSP_COMPLIANT,
                "false"
        );

        bp.getRequestContext().put(
                SecurityConstants.MUST_UNDERSTAND,
                Boolean.TRUE
        );

        var response = port.sendTestSetAsync(zipName, zipBytes, testSetId);

        return response.toString();
    }


    public byte[] zipXml(String xml, String entryName) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();

            try (ZipOutputStream zos = new ZipOutputStream(baos, StandardCharsets.UTF_8)) {
                ZipEntry entry = new ZipEntry(entryName);
                zos.putNextEntry(entry);
                zos.write(xml.getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }

            return baos.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Error comprimiendo XML DIAN", e);
        }
    }

    public String toBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public String buildSendTestSetAsyncEnvelope(String zipBase64, String zipName, String testSetId) {
        return """
            <soap12:Envelope xmlns:soap12="http://www.w3.org/2003/05/soap-envelope"
                             xmlns:wcf="http://wcf.dian.colombia">
                <soap12:Header/>
                <soap12:Body>
                    <wcf:SendTestSetAsync>
                        <wcf:fileName>%s</wcf:fileName>
                        <wcf:contentFile>%s</wcf:contentFile>
                        <wcf:testSetId>%s</wcf:testSetId>
                    </wcf:SendTestSetAsync>
                </soap12:Body>
            </soap12:Envelope>
            """.formatted(zipName, zipBase64, testSetId);
    }

}
