package com.facturacion.negocio.services.dian.service;

import com.facturacion.negocio.domain.DocumentoFiscal;
import com.facturacion.negocio.repository.DocumentoFiscalRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;

@Service
public class DocumentoFiscalDianTxService {

    private final DocumentoFiscalRepository documentoFiscalRepository;

    public DocumentoFiscalDianTxService(DocumentoFiscalRepository documentoFiscalRepository) {
        this.documentoFiscalRepository = documentoFiscalRepository;
    }

    @Transactional
    public void asegurarEnviadoAt(Long documentoFiscalId) {
        DocumentoFiscal doc = documentoFiscalRepository.findByIdForUpdate(documentoFiscalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Documento fiscal no encontrado"));

        if (doc.getEnviadoAt() == null) {
            doc.setEnviadoAt(Instant.now());
            documentoFiscalRepository.save(doc);
        }
    }

    @Transactional
    public void guardarRequestXml(Long documentoFiscalId, String xml) {
        DocumentoFiscal doc = documentoFiscalRepository.findByIdForUpdate(documentoFiscalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Documento fiscal no encontrado"));

        doc.setRequestJson(xml);
        documentoFiscalRepository.save(doc);
    }

    @Transactional
    public void guardarCufe(Long documentoFiscalId, String cufe) {
        DocumentoFiscal doc = documentoFiscalRepository.findByIdForUpdate(documentoFiscalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Documento fiscal no encontrado"));

        doc.setCufe(cufe);
        documentoFiscalRepository.save(doc);
    }
}