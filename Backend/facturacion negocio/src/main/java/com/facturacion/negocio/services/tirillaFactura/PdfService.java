package com.facturacion.negocio.services.tirillaFactura;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

    private final FacturaTemplateService facturaTemplateService;

    public PdfService(FacturaTemplateService facturaTemplateService) {
        this.facturaTemplateService = facturaTemplateService;
    }

    public byte[] generarPdf(Factura factura) {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            String html = facturaTemplateService.render(factura);

            PdfRendererBuilder builder = new PdfRendererBuilder();

            builder.withHtmlContent(html, null);
            builder.toStream(os);
            builder.run();

            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF", e);
        }
    }
}
