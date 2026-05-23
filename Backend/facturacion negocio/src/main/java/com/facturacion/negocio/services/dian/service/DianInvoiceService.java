package com.facturacion.negocio.services.dian.service;

import com.facturacion.negocio.domain.DocumentoFiscal;
import com.facturacion.negocio.domain.NumberingRange;
import com.facturacion.negocio.dto.factura.NumberAllocationResult;
import com.facturacion.negocio.repository.DocumentoFiscalRepository;
import com.facturacion.negocio.repository.NumberingRangeRepository;
import com.facturacion.negocio.services.NumberingRangeService;
import com.facturacion.negocio.services.TenantService;
import com.facturacion.negocio.services.dian.dto.DianInvoiceData;
import com.facturacion.negocio.services.dian.mapper.DianCufeGenerator;
import com.facturacion.negocio.services.dian.mapper.DianInvoiceMapper;
import com.facturacion.negocio.services.dian.xml.DianInvoiceXmlBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class DianInvoiceService {

    private final DianInvoiceMapper dianInvoiceMapper;
    private final DianCufeGenerator dianCufeGenerator;
    private final DocumentoFiscalRepository documentoFiscalRepository;
    private final NumberingRangeService numberingRangeService;
    private final NumberingRangeRepository numberingRangeRepository;
    private final DianInvoiceXmlBuilder dianInvoiceXmlBuilder;
    private final DocumentoFiscalDianTxService documentoFiscalDianTxService;
    private final TenantService tenantService;

    public DianInvoiceService(DianInvoiceMapper dianInvoiceMapper,
                              DianCufeGenerator dianCufeGenerator,
                              DocumentoFiscalRepository documentoFiscalRepository,
                              NumberingRangeService numberingRangeService,
                              NumberingRangeRepository numberingRangeRepository,
                              DianInvoiceXmlBuilder dianInvoiceXmlBuilder,
                              DocumentoFiscalDianTxService documentoFiscalDianTxService,
                              TenantService tenantService) {

        this.dianInvoiceMapper = dianInvoiceMapper;
        this.dianCufeGenerator = dianCufeGenerator;
        this.documentoFiscalRepository = documentoFiscalRepository;
        this.numberingRangeService = numberingRangeService;
        this.numberingRangeRepository = numberingRangeRepository;
        this.dianInvoiceXmlBuilder = dianInvoiceXmlBuilder;
        this.documentoFiscalDianTxService = documentoFiscalDianTxService;
        this.tenantService = tenantService;
    }

    //cambiar a void despues
    @Transactional
    public String enviarDocumentoDian(Long documentoFiscalId, Long numberingRangeId) throws Exception {
        DocumentoFiscal doc = tenantService.getNegocioIdActualOptional()
                .flatMap(negocioId -> documentoFiscalRepository.findByIdAndNegocio(
                        documentoFiscalId,
                        tenantService.getNegocioActual()
                ))
                .orElseGet(() -> documentoFiscalRepository.findById(documentoFiscalId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Documento fiscal no encontrado"
                        )));

        NumberAllocationResult allocation = numberingRangeService.allocateNext(numberingRangeId);

        NumberingRange range = tenantService.getNegocioIdActualOptional()
                .flatMap(negocioId -> numberingRangeRepository.findByIdAndNegocioId(allocation.rangeId(), negocioId))
                .orElseGet(() -> numberingRangeRepository.findById(allocation.rangeId())
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Rango de numeracion no encontrado"
                        )));

        doc.setNumberingRange(range);
        doc.setConsecutivo(allocation.consecutive());
        doc.setNumeroDocumento(allocation.documentNumber());

        if (doc.getEnviadoAt() == null) {
            doc.setEnviadoAt(Instant.now());
        }

        documentoFiscalRepository.save(doc);

        DianInvoiceData data = dianInvoiceMapper.from(doc);

        String cufe = dianCufeGenerator.generate(
                data.getInvoiceNumber(),
                data.getIssuedAt(),
                data.getSubtotal(),
                data.getIvaTotal(),
                data.getIncTotal(),
                data.getTotal(),
                data.getSupplier().getNit(),
                data.getCustomer().getIdentificationNumber(),
                range.getTechnicalKey(),
                "2"
        );
        System.out.println("CUFE: " + cufe);

        data.setCufe(cufe);
        doc.setCufe(cufe);

        System.out.println(data.toString());
        documentoFiscalRepository.save(doc);

        String xml = dianInvoiceXmlBuilder.build(data);

        documentoFiscalDianTxService.guardarRequestXml(doc.getId(), xml);
        return xml;
    }

}
