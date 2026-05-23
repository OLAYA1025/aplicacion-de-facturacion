package com.facturacion.negocio.controllers;

import com.facturacion.negocio.domain.DocumentoFiscal;
import com.facturacion.negocio.dto.factura.dian.EnviarDocumentoDianDTO;
import com.facturacion.negocio.dto.factura.dian.PruebaDianResponseDTO;
import com.facturacion.negocio.services.dian.certificate.DianCertificateData;
import com.facturacion.negocio.services.dian.certificate.DianCertificateLoader;
import com.facturacion.negocio.services.dian.client.DianCxfClient;
import com.facturacion.negocio.services.dian.client.DianSoapClient;
import com.facturacion.negocio.services.dian.client.DianSoapSmokeTest;
import com.facturacion.negocio.services.dian.service.DianInvoiceService;
import com.facturacion.negocio.services.dian.service.DianXmlService;
import com.facturacion.negocio.services.dian.xml.DianXmlSigner;
import com.facturacion.negocio.services.DocumentoFiscalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.w3c.dom.Document;

@RestController
@RequestMapping("/invoice")
public class DianInvoiceController {

    private final DianCertificateLoader dianCertificateLoader;
    private final DianXmlSigner dianXmlSigner;
    private final DianInvoiceService dianInvoiceService;
    private final DianSoapClient dianSoapClient;
    private final DianSoapSmokeTest dianSoapSmokeTest;
    private final DianCxfClient  dianCxfClient;
    private final DocumentoFiscalService documentoFiscalService;


    public DianInvoiceController(DianCertificateLoader dianCertificateLoader,
                                 DianXmlSigner dianXmlSigner,
                                 DianInvoiceService dianInvoiceService,
                                 DianSoapClient dianSoapClient,
                                 DianSoapSmokeTest dianSoapSmokeTest,
                                 DianCxfClient dianCxfClient,
                                 DocumentoFiscalService documentoFiscalService) {

        this.dianCertificateLoader = dianCertificateLoader;
        this.dianXmlSigner = dianXmlSigner;
        this.dianInvoiceService = dianInvoiceService;
        this.dianSoapClient = dianSoapClient;
        this.dianSoapSmokeTest = dianSoapSmokeTest;
        this.dianCxfClient = dianCxfClient;
        this.documentoFiscalService = documentoFiscalService;
    }

    @GetMapping("/prueba")
    public ResponseEntity<PruebaDianResponseDTO> prueba(@RequestBody EnviarDocumentoDianDTO dto) {

        DianCertificateData cert = dianCertificateLoader.load();



        String xml;
        try {
            xml = dianInvoiceService.enviarDocumentoDian(
                    dto.getDocumentoFiscalId(),
                    dto.getNumberingRangeId()
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage(), e);
        }

        String signedXml = dianXmlSigner.sign(xml);
        Boolean valid = dianXmlSigner.verify(signedXml);

        DocumentoFiscal doc = documentoFiscalService.buscarPorId(dto.getDocumentoFiscalId());
        String invoiceNumber =doc.getNumeroDocumento(); // o desde data/doc
        byte[] zipBytes = dianSoapClient.zipXml(signedXml, invoiceNumber + ".xml");



        String res = dianCxfClient.sendTestSetAsync(
                zipBytes,
                invoiceNumber + ".zip",
                "6c5be962-2188-49a7-b249-38529677343c"
        );





        PruebaDianResponseDTO response = new PruebaDianResponseDTO();
        response.setRes(res);

        response.setAlias(cert.alias());
        /*
        response.setCertificateSubject(cert.certificate().getSubjectX500Principal().getName());
        response.setXmlLength(signedXml.length());
        response.setXml(signedXml);
        response.setSignatureValid(valid);
         */

        return ResponseEntity.ok(response);
    }

    @GetMapping("/prueba2")
    public void probarClient(){
        DianCertificateData cert = dianCertificateLoader.load();
        System.out.println("alias:" + cert.alias());
        dianSoapSmokeTest.probar();
    }
}
