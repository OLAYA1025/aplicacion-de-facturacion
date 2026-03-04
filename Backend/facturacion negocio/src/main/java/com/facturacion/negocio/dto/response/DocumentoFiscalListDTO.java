package com.facturacion.negocio.dto.response;

import com.facturacion.negocio.domain.Cliente;
import com.facturacion.negocio.domain.DocumentoFiscal;
import com.facturacion.negocio.enumerated.EstadoDocumentoFiscal;
import com.facturacion.negocio.enumerated.TipoDocumentoFiscal;

import java.math.BigDecimal;
import java.time.Instant;

public class DocumentoFiscalListDTO {

    private Long id;
    private Long pedidoId;
    private TipoDocumentoFiscal tipoDocumentoFiscal;
    private EstadoDocumentoFiscal estado;
    private String numeroDocumento;
    private String cufe;
    private String referenceCode;
    private Instant createdAt;
    private Instant updatedAt;
    private Instant aceptadoAt;
    private String clienteNombre;
    private String clienteIdentificacion;
    private BigDecimal total;

    public static DocumentoFiscalListDTO from(DocumentoFiscal documentoFiscal) {
        return from(documentoFiscal, null);
    }

    public static DocumentoFiscalListDTO from(DocumentoFiscal documentoFiscal, BigDecimal total) {
        DocumentoFiscalListDTO dto = new DocumentoFiscalListDTO();
        dto.id = documentoFiscal.getId();
        dto.pedidoId = documentoFiscal.getPedido() != null ? documentoFiscal.getPedido().getId() : null;
        dto.tipoDocumentoFiscal = documentoFiscal.getTipo();
        dto.estado = documentoFiscal.getEstado();
        dto.numeroDocumento = documentoFiscal.getNumeroDocumento();
        dto.cufe = documentoFiscal.getCufe();
        dto.referenceCode = documentoFiscal.getReferenceCode();
        dto.createdAt = documentoFiscal.getCreatedAt();
        dto.updatedAt = documentoFiscal.getUpdatedAt();
        dto.aceptadoAt = documentoFiscal.getAceptadoAt();


        Cliente cliente = documentoFiscal.getCliente();
        if (cliente != null) {
            dto.clienteNombre = cliente.getNombre();
            dto.clienteIdentificacion = cliente.getNumeroIdentificacion();
        }

        dto.total = total;
        return dto;
    }

    public Long getId() {
        return id;
    }

    public Long getPedidoId() {
        return pedidoId;
    }

    public TipoDocumentoFiscal getTipoDocumentoFiscal() {
        return tipoDocumentoFiscal;
    }

    public EstadoDocumentoFiscal getEstado() {
        return estado;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public String getCufe() {
        return cufe;
    }

    public String getReferenceCode() {
        return referenceCode;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public Instant getAceptadoAt() {
        return aceptadoAt;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public String getClienteIdentificacion() {
        return clienteIdentificacion;
    }

    public BigDecimal getTotal() {
        return total;
    }
}
