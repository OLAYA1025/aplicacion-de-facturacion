package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.DocumentoFiscal;
import com.facturacion.negocio.dto.factura.FactusFactElectronicaRequestDTO;
import com.facturacion.negocio.enumerated.EstadoDocumentoFiscal;
import com.facturacion.negocio.repository.DocumentoFiscalRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DocumentoFiscalTxService {

    private final DocumentoFiscalRepository documentoFiscalRepository;
    private final ObjectMapper objectMapper;

    public DocumentoFiscalTxService(DocumentoFiscalRepository documentoFiscalRepository, ObjectMapper objectMapper) {
        this.documentoFiscalRepository = documentoFiscalRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public boolean tomarDocumentoParaEnvio(Long documentoFiscalId) {
        DocumentoFiscal doc = documentoFiscalRepository.findByIdForUpdate(documentoFiscalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Documento fiscal no encontrado"));

        if (doc.getEstado() == EstadoDocumentoFiscal.ACEPTADO_DIAN) {
            return false;
        }

        if (doc.getEstado() == EstadoDocumentoFiscal.RECHAZADO) {
            return false;
        }

        if (doc.getEstado() == EstadoDocumentoFiscal.ENVIADO_PT) {
            return false;
        }

        if (doc.getEstado() == EstadoDocumentoFiscal.PENDIENTE_CONTINGENCIA) {
            doc.reenviar();
        }

        if (doc.getEstado() != EstadoDocumentoFiscal.PENDIENTE_ENVIO) {
            return false;
        }

        doc.marcarEnviadoPT("FACTUS", null);
        documentoFiscalRepository.saveAndFlush(doc);
        return true;
    }

    @Transactional
    public void procesarRespuestaExitosa(Long documentoFiscalId, JsonNode resp) {
        DocumentoFiscal doc = documentoFiscalRepository.findByIdForUpdate(documentoFiscalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Documento fiscal no encontrado"));

        doc.setResponseJson(resp.toString());

        String cufe = resp.at("/data/bill/cufe").asText(null);
        String number = resp.at("/data/bill/number").asText(null);
        String qr = resp.at("/data/bill/qr_image").asText(null);

        if (number == null || cufe == null) {
            doc.marcarRechazado("RESP_INVALIDA", "No viene number/cufe");
            documentoFiscalRepository.save(doc);
            return;
        }

        doc.setCufe(cufe);
        doc.setNumeroDocumento(number);
        doc.setQr(qr);

        doc.marcarAceptadoDian(number, cufe, qr);

        documentoFiscalRepository.save(doc);
    }

    @Transactional
    public void manejarDuplicado409(Long documentoFiscalId, String detalle) {
        DocumentoFiscal doc = documentoFiscalRepository.findByIdForUpdate(documentoFiscalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Documento fiscal no encontrado"));

        if (doc.getEstado() == EstadoDocumentoFiscal.ACEPTADO_DIAN) {
            return;
        }

        doc.marcarPendienteContingencia(
                "409_DUPLICADO",
                "Duplicado reference_code: " + doc.getReferenceCode() + ". Detalle: " + detalle
        );

        documentoFiscalRepository.save(doc);
    }

    @Transactional
    public void marcarRechazado(Long documentoFiscalId, String codigo, String detalle) {
        DocumentoFiscal doc = documentoFiscalRepository.findByIdForUpdate(documentoFiscalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Documento fiscal no encontrado"));

        doc.marcarRechazado(codigo, detalle);
        documentoFiscalRepository.save(doc);
    }

    @Transactional
    public void marcarPendienteContingencia(Long documentoFiscalId, String codigo, String detalle) {
        DocumentoFiscal doc = documentoFiscalRepository.findByIdForUpdate(documentoFiscalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Documento fiscal no encontrado"));

        doc.marcarPendienteContingencia(codigo, detalle);
        documentoFiscalRepository.save(doc);
    }

    @Transactional
    public void guardarRequestJson(Long documentoFiscalId, FactusFactElectronicaRequestDTO request) {
        DocumentoFiscal doc = documentoFiscalRepository.findByIdForUpdate(documentoFiscalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Documento fiscal no encontrado"));

        try {
            doc.setRequestJson(objectMapper.writeValueAsString(request));
            documentoFiscalRepository.save(doc);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error serializando request del documento fiscal", e);
        }
    }
}
