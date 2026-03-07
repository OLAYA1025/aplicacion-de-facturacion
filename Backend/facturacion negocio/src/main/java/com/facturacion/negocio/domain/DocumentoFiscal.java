package com.facturacion.negocio.domain;

import com.facturacion.negocio.enumerated.EstadoDocumentoFiscal;
import com.facturacion.negocio.enumerated.TipoDocumentoFiscal;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;


@Entity
@Table(
        name = "documentos_fiscales",
        indexes = {
                @Index(name = "idx_docfiscal_estado", columnList = "estado"),
                @Index(name = "idx_docfiscal_tipo", columnList = "tipo"),
                @Index(name = "idx_docfiscal_pedido", columnList = "pedido_id"),
                @Index(name = "idx_docfiscal_ref", columnList = "reference_code")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_docfiscal_pedido", columnNames = {"pedido_id"}),
                @UniqueConstraint(name = "uk_docfiscal_ref", columnNames = {"reference_code"})
        }
)
public class DocumentoFiscal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 1:1 con Pedido
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = true)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoDocumentoFiscal tipo; // FEV / DEE_POS / FEV_CONS_FINAL

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoDocumentoFiscal estado;

    /**
     * reference_code que mandas a Factus para evitar duplicados.
     * Recomendado: "PED-" + pedidoId
     */
    @Column(name = "reference_code", nullable = false, length = 60)
    private String referenceCode;

    /**
     * Rango de numeración en Factus (numbering_range_id)
     */
    @Column(name = "numbering_range_id")
    private Integer numberingRangeId = 8;

    /**
     * Resultado Factus / DIAN
     */
    @Column(name = "numero_documento", length = 50)
    private String numeroDocumento; // ej: "SETP990000493"

    @Column(name = "cufe", length = 128)
    private String cufe; // para FEV

    @Column(name = "cude", length = 128)
    private String cude; // para DEE POS

    @Column(name = "qr", columnDefinition="text")
    private String qr;

    @Column(name = "proveedor", length = 30)
    private String proveedor; // "FACTUS"

    @Column(name = "proveedor_track_id", length = 128)
    private String proveedorTrackId; // puede ser CUFE/CUDE o el ID interno del proveedor

    /**
     * Auditoría técnica (opcional pero útil)
     */

    @Column(name = "request_json", columnDefinition="text")
    private String requestJson;


    @Column(name = "response_json", columnDefinition="text")
    private String responseJson;

    @Column(name = "error_codigo", length = 50)
    private String errorCodigo;


    @Column(name = "error_mensaje", columnDefinition="text")
    private String errorMensaje;

    @Column(name = "enviado_at")
    private Instant enviadoAt;

    @Column(name = "aceptado_at")
    private Instant aceptadoAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected DocumentoFiscal() {
    }

    private DocumentoFiscal(Pedido pedido, TipoDocumentoFiscal tipo, String referenceCode) {
        this.pedido = pedido;
        this.tipo = tipo;
        this.referenceCode = referenceCode;
        this.estado = EstadoDocumentoFiscal.BORRADOR;
    }

    public static DocumentoFiscal crearBorrador(Pedido pedido, TipoDocumentoFiscal tipo) {
        if (pedido == null) throw new IllegalArgumentException("pedido requerido");
        if (tipo == null) throw new IllegalArgumentException("tipo requerido");
        String referenceCode = "PED-" + pedido.getId();
        return new DocumentoFiscal(pedido, tipo, referenceCode);
    }

    public void marcarPendienteEnvio() {
        if (estado != EstadoDocumentoFiscal.BORRADOR) {
            throw new IllegalStateException("Solo BORRADOR -> PENDIENTE_ENVIO");
        }
        estado = EstadoDocumentoFiscal.PENDIENTE_ENVIO;
    }

    public void marcarEnviadoPT(String proveedor, String trackId) {
        if (estado != EstadoDocumentoFiscal.PENDIENTE_ENVIO) {
            throw new IllegalStateException("Solo PENDIENTE_ENVIO -> ENVIADO_PT");
        }
        this.estado = EstadoDocumentoFiscal.ENVIADO_PT;
        this.proveedor = proveedor;
        this.proveedorTrackId = trackId;
        this.enviadoAt = Instant.now();
    }

    public void marcarAceptadoDian(String numeroDocumento, String cufe, String qr) {
        this.estado = EstadoDocumentoFiscal.ACEPTADO_DIAN;
        this.numeroDocumento = numeroDocumento;
        this.cufe = cufe;
        this.qr = qr;
        this.aceptadoAt = Instant.now();
    }

    public void marcarRechazado(String codigo, String mensaje) {
        this.estado = EstadoDocumentoFiscal.RECHAZADO;
        this.errorCodigo = codigo;
        this.errorMensaje = mensaje;
    }

    public void marcarPendienteContingencia(String codigo, String mensaje) {
        this.estado = EstadoDocumentoFiscal.PENDIENTE_CONTINGENCIA;
        this.errorCodigo = codigo;
        this.errorMensaje = mensaje;

    }
    public void reenviar(){
        if(this.estado != EstadoDocumentoFiscal.PENDIENTE_CONTINGENCIA){
            throw new IllegalStateException("Solo se pueden enviar facturas pendientes por contingencia");
        }

        this.estado = EstadoDocumentoFiscal.PENDIENTE_ENVIO;
        this.updatedAt = Instant.now();
        this.errorCodigo = null;
        this.errorMensaje = null;
    }

    // ===== getters/setters mínimos =====

    public Long getId() { return id; }

    public Pedido getPedido() { return pedido; }

    public void setPedido(Pedido pedido) { this.pedido = pedido; }

    public TipoDocumentoFiscal getTipo() { return tipo; }

    public EstadoDocumentoFiscal getEstado() { return estado; }

    public String getReferenceCode() { return referenceCode; }

    public Integer getNumberingRangeId() { return numberingRangeId; }

    public void setNumberingRangeId(Integer numberingRangeId) { this.numberingRangeId = numberingRangeId; }

    public void setRequestJson(String requestJson) { this.requestJson = requestJson; }

    public void setResponseJson(String responseJson) { this.responseJson = responseJson; }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getQr() {
        return qr;
    }

    public void setQr(String qr) {
        this.qr = qr;
    }

    public String getCufe() {
        return cufe;
    }

    public void setCufe(String cufe) {
        this.cufe = cufe;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getErrorMensaje() {
        return errorMensaje;
    }

    public void setErrorMensaje(String errorMensaje) {
        this.errorMensaje = errorMensaje;
    }

    public String getErrorCodigo() {
        return errorCodigo;
    }

    public void setErrorCodigo(String errorCodigo) {
        this.errorCodigo = errorCodigo;
    }

    public Instant getEnviadoAt() {
        return enviadoAt;
    }

    public void setEnviadoAt(Instant enviadoAt) {
        this.enviadoAt = enviadoAt;
    }

    public Instant getAceptadoAt() {
        return aceptadoAt;
    }

    public void setAceptadoAt(Instant aceptadoAt) {
        this.aceptadoAt = aceptadoAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setEstado(EstadoDocumentoFiscal estado) {
        this.estado = estado;
    }
}
