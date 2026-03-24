package com.facturacion.negocio.services.dian.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class DianSoapClient {


    private final RestTemplate restTemplate;
    private final String dianHabUrl;

    public DianSoapClient(
            RestTemplate restTemplate,
            @Value("${dian.hab.url:https://vpfe-hab.dian.gov.co/WcfDianCustomerServices.svc}") String dianHabUrl
    ) {
        this.restTemplate = restTemplate;
        this.dianHabUrl = dianHabUrl;
    }

    public String sendTestSetAsync(String envelope) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/soap+xml; charset=utf-8"));

        HttpEntity<String> entity = new HttpEntity<>(envelope, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                    dianHabUrl,
                    HttpMethod.POST,
                    entity,
                    String.class
            );
            return response.getBody();
        } catch (HttpStatusCodeException e) {
            String body = e.getResponseBodyAsString();
            throw new IllegalStateException(
                    "HTTP " + e.getStatusCode().value() + " DIAN: " + body,
                    e
            );
        }
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