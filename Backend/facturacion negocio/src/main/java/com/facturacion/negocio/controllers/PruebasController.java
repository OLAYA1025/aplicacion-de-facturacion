package com.facturacion.negocio.controllers;


import com.facturacion.negocio.services.dian.certificate.DianCertificateData;
import com.facturacion.negocio.services.dian.certificate.DianCertificateLoader;
import com.facturacion.negocio.services.dian.service.DianInvoiceService;
import com.facturacion.negocio.services.dian.xml.DianXmlSigner;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/prueba")
public class PruebasController {

    private final DianCertificateLoader dianCertificateLoader;
    private final DianXmlSigner dianXmlSigner;
    private final DianInvoiceService dianInvoiceService;


    public PruebasController(DianCertificateLoader dianCertificateLoader,
                             DianXmlSigner dianXmlSigner,
                             DianInvoiceService dianInvoiceService) {

        this.dianCertificateLoader = dianCertificateLoader;
        this.dianXmlSigner = dianXmlSigner;
        this.dianInvoiceService = dianInvoiceService;
    }

    @GetMapping("/{docId}")
    public ResponseEntity<String> prueba(@PathVariable Long docId) {
        DianCertificateData cert = dianCertificateLoader.load();
        System.out.println(cert.alias());
        System.out.println(cert.certificate().getSubjectX500Principal().getName());



        return ResponseEntity.ok(cert.certificate().getSubjectX500Principal().getName());
    }
}
