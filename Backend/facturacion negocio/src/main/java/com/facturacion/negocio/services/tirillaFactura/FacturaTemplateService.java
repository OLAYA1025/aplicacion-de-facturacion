package com.facturacion.negocio.services.tirillaFactura;

import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

@Service
public class FacturaTemplateService {

    private final SpringTemplateEngine templateEngine;
    private final FacturaLogoService facturaLogoService;

    public FacturaTemplateService(SpringTemplateEngine templateEngine, FacturaLogoService facturaLogoService) {
        this.templateEngine = templateEngine;
        this.facturaLogoService = facturaLogoService;
    }

    public String render(Factura factura) {

        Context context = new Context();
        context.setVariable("factura", factura);
        context.setVariable("logo", facturaLogoService.logoToBase64(factura).orElse(null));

        return templateEngine.process("factura-termica", context);
    }

    public String renderPreFactura(PreFactura preFactura){
        Context context = new Context();
        context.setVariable("factura", preFactura);
        return templateEngine.process("prefactura-termica", context);
    }
}
