package com.facturacion.negocio.dto.factura.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PdfData {
    @JsonProperty("file_name")
    private String fileName;

    @JsonProperty("pdf_base_64_encoded")
    private String pdfBase64Encoded;

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getPdfBase64Encoded() { return pdfBase64Encoded; }
    public void setPdfBase64Encoded(String pdfBase64Encoded) { this.pdfBase64Encoded = pdfBase64Encoded; }
}
