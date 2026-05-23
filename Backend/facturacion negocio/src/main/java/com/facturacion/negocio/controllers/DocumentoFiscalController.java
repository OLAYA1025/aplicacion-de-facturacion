package com.facturacion.negocio.controllers;

import com.facturacion.negocio.domain.DocumentoFiscal;
import com.facturacion.negocio.dto.response.DocumentoFiscalListDTO;
import com.facturacion.negocio.enumerated.TipoDocumentoFiscal;
import com.facturacion.negocio.services.DocumentoFiscalService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/documentos-fiscales")
public class DocumentoFiscalController {

    private final DocumentoFiscalService documentoFiscalService;

    public DocumentoFiscalController(DocumentoFiscalService documentoFiscalService) {
        this.documentoFiscalService = documentoFiscalService;
    }

    @GetMapping
    public ResponseEntity<Page<DocumentoFiscalListDTO>> listarDocumentosFiscales(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false, defaultValue = "") String q,
            @RequestParam(name = "estado", required = false) List<String> estados,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "createdAt,desc") String sort) {
        Page<DocumentoFiscalListDTO> response = documentoFiscalService.listarDocumentosFiscales(
                page,
                size,
                q,
                estados,
                from,
                to,
                sort
        );
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> pdf(@PathVariable Long id) {
        DocumentoFiscal doc = documentoFiscalService.buscarPorId(id);

        byte[] pdfBytes = documentoFiscalService.obtenerPdf(id);

        String filename = (doc.getTipo() == TipoDocumentoFiscal.FEV_CONS_FINAL?"FEV-CONSUMIDOR-FINAL-":"FEV-") + (doc.getNumeroDocumento() != null ? doc.getNumeroDocumento() : doc.getCufe()) + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(pdfBytes);
    }

    @GetMapping("/{id}/tirilla/pdf")
    public ResponseEntity<byte[]> pdfTirilla(@PathVariable Long id) {
        DocumentoFiscal doc = documentoFiscalService.buscarPorId(id);

        byte[] pdfBytes = documentoFiscalService.obtenerPdfTirilla(id);

        String filename = (doc.getTipo() == TipoDocumentoFiscal.FEV_CONS_FINAL?"FEV-CONSUMIDOR-FINAL-":"FEV-") + (doc.getNumeroDocumento() != null ? doc.getNumeroDocumento() : doc.getCufe()) + ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(pdfBytes);
    }

    @PostMapping("/{id}/reenviar")
    public ResponseEntity<Void> reenviarFactura(@PathVariable Long id) throws JsonProcessingException {
        documentoFiscalService.ReenviarFactura(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/mensaje-error")
    public ResponseEntity<String> obtenerMensajeError(@PathVariable Long id) {
        String mensajeError = documentoFiscalService.obtenerMensajeError(id);
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(mensajeError);
    }
}
