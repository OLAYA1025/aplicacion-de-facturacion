package com.facturacion.negocio.dto.factura.dian;

public class PruebaDianResponseDTO {

    private String alias;
    private String certificateSubject;
    private Integer xmlLength;
    private String xml;
    private String envelope;
    private String res;
    private Boolean signatureValid;


    public String getEnvelope() {
        return envelope;
    }

    public void setEnvelope(String envelope) {
        this.envelope = envelope;
    }

    public String getRes() {
        return res;
    }

    public void setRes(String res) {
        this.res = res;
    }

    public Boolean getSignatureValid() {
        return signatureValid;
    }

    public void setSignatureValid(Boolean signatureValid) {
        this.signatureValid = signatureValid;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getCertificateSubject() {
        return certificateSubject;
    }

    public void setCertificateSubject(String certificateSubject) {
        this.certificateSubject = certificateSubject;
    }


    public Integer getXmlLength() {
        return xmlLength;
    }

    public void setXmlLength(Integer xmlLength) {
        this.xmlLength = xmlLength;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }
}