package com.facturacion.negocio.scheduler;

import com.facturacion.negocio.domain.DocumentoFiscal;
import com.facturacion.negocio.enumerated.EstadoDocumentoFiscal;
import com.facturacion.negocio.repository.DocumentoFiscalRepository;
import com.facturacion.negocio.services.DocumentoFiscalService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;



import java.time.Instant;
import java.util.List;

@Component
public class DocumentoFiscalScheduler {

    private final DocumentoFiscalRepository documentoFiscalRepository;
    private final DocumentoFiscalService documentoFiscalService;

    public DocumentoFiscalScheduler(DocumentoFiscalRepository documentoFiscalRepository,
                                    DocumentoFiscalService documentoFiscalService) {
        this.documentoFiscalRepository = documentoFiscalRepository;
        this.documentoFiscalService = documentoFiscalService;
    }

    @Scheduled(fixedDelayString = "${fiscal.scheduler.delay-ms:10000}")
    public void enviarPendientes() {
        List<DocumentoFiscal> pendientesEnvio =
                documentoFiscalRepository.findTop50ByEstadoOrderByIdAsc(EstadoDocumentoFiscal.PENDIENTE_ENVIO);

        for (DocumentoFiscal doc : pendientesEnvio) {
            try {
                documentoFiscalService.enviarAlProveedor(doc.getId());

            } catch (Exception e) {
                // Importante: no tumbar el scheduler
                // Aquí luego metemos logger
                System.err.println("Error enviando DocumentoFiscal id=" + doc.getId() + ": " + e.getMessage());
            }
        }

        List<DocumentoFiscal> enviadosPt =
                documentoFiscalRepository.findTop50ByEstadoOrderByIdAsc(EstadoDocumentoFiscal.ENVIADO_PT);

        Instant hace15Segundos = Instant.now().minusSeconds(15);
        for (DocumentoFiscal doc : enviadosPt) {
            if (doc.getEnviadoAt() != null && doc.getEnviadoAt().isAfter(hace15Segundos)) {
                continue;
            }

            try {
                documentoFiscalService.enviarAlProveedor(doc.getId());
            } catch (Exception e) {
                System.err.println("Error reintentando DocumentoFiscal ENVIADO_PT id=" + doc.getId() + ": " + e.getMessage());
            }
        }
    }
}
