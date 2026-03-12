package com.facturacion.negocio.services.tirillaFactura;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.io.InputStream;
import java.util.Base64;

@Service
public class FacturaTemplateService {

    private final SpringTemplateEngine templateEngine;

    public FacturaTemplateService(SpringTemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }

    public String render(Factura factura) {

        Context context = new Context();
        context.setVariable("factura", factura);
        context.setVariable("logo", logoToBase64());

        return templateEngine.process("factura-termica", context);
    }


    public String logoToBase64() {
        try {
            InputStream is = getClass().getResourceAsStream("/static/images/montaraLogo.png");

            byte[] bytes = is.readAllBytes();

            String base64 = Base64.getEncoder().encodeToString(bytes);

            return "data:image/png;base64," + base64;

        } catch (Exception e) {
            throw new RuntimeException("Error convirtiendo logo a base64", e);
        }
    }
}