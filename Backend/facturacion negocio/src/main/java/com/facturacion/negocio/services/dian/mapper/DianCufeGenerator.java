package com.facturacion.negocio.services.dian.mapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;

@Component
public class DianCufeGenerator {

    private final ZoneId ZONE_ID;

    public DianCufeGenerator(@Value("${app.time-zone:America/Bogota}") String ZONE_ID){
        this.ZONE_ID = ZoneId.of(ZONE_ID);

    }

    public String generate(
            String invoiceNumber,
            Instant issuedAt,
            BigDecimal subtotal,
            BigDecimal ivaTotal,
            BigDecimal incTotal,
            BigDecimal total,
            String supplierNit,
            String customerDocument,
            String technicalKey,
            String environmentType

    ) {

        String base = buildBaseString(
                invoiceNumber,
                issuedAt,
                subtotal,
                ivaTotal,
                incTotal,
                total,
                supplierNit,
                customerDocument,
                technicalKey,
                environmentType
        );

        return sha384(base);
    }

    private String buildBaseString(
            String invoiceNumber,
            Instant issuedAt,
            BigDecimal subtotal,
            BigDecimal ivaTotal,
            BigDecimal incTotal,
            BigDecimal total,
            String supplierNit,
            String customerDocument,
            String technicalKey,
            String environmentType
    ) {

        StringBuilder sb = new StringBuilder();

        sb.append(invoiceNumber);
        sb.append(formatDate(issuedAt));
        sb.append(formatTime(issuedAt));
        sb.append(money(subtotal));

        if (ivaTotal != null && ivaTotal.compareTo(BigDecimal.ZERO) > 0) {
            sb.append("01");
            sb.append(money(ivaTotal));
        }

        if (incTotal != null && incTotal.compareTo(BigDecimal.ZERO) > 0) {
            sb.append("04");
            sb.append(money(incTotal));
        }

        sb.append(money(total));
        sb.append(supplierNit);
        sb.append(customerDocument);
        sb.append(technicalKey);
        sb.append(environmentType);

        return sb.toString();
    }

    private String formatDate(Instant issuedAt) {
        return issuedAt.atZone(ZONE_ID).toLocalDate().toString();
    }

    private String formatTime(Instant issuedAt) {
        return issuedAt.atZone(ZONE_ID).toLocalTime().withNano(0).toString();
    }

    private String money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String sha384(String input) {

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-384");

            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();

            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Error generando CUFE", e);
        }
    }
}
