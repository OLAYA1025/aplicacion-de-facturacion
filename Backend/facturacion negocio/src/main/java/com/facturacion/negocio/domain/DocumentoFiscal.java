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
                @Index(name = "idx_docfiscal_pedido", columnList = "pedido_id"),
                @Index(name = "idx_docfiscal_estado", columnList = "estado"),
                @Index(name = "idx_docfiscal_tipo", columnList = "tipo"),
                @Index(name = "idx_docfiscal_prefijo_consecutivo", columnList = "prefijo,consecutivo")
        },
        uniqueConstraints = {
                // Evita que se repita el mismo número por prefijo (muy útil)
                @UniqueConstraint(name = "uk_docfiscal_prefijo_consecutivo", columnNames = {"prefijo", "consecutivo"})
        }
)
public class DocumentoFiscal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    // Relación 1-1 con Pedido (dueño de la FK)
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoDocumentoFiscal tipo;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false, length = 30)
    private EstadoDocumentoFiscal estado = EstadoDocumentoFiscal.BORRADOR;

    // Numeración
    @Column(name = "prefijo", length = 10)
    private String prefijo; // Ej: "POS", "FE"

    @Column(name = "consecutivo")
    private Long consecutivo;

    // Identificadores DIAN (según aplique)
    @Column(name = "cufe", length = 128)
    private String cufe; // Factura electrónica

    @Column(name = "cude", length = 128)
    private String cude; // Documento equivalente electrónico POS

    @Column(name = "qr_texto", length = 2048)
    private String qrTexto;

    // Info del proveedor tecnológico
    @Column(name = "proveedor", length = 50)
    private String proveedor; // "FACTUS"

    @Column(name = "proveedor_documento_id", length = 100)
    private String proveedorDocumentoId;

    @Column(name = "proveedor_track_id", length = 100)
    private String proveedorTrackId;

    // Adquirente (cliente) - mínimo viable (solo requerido si FEV)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = true)
    private Cliente cliente;

    // Auditoría técnica: request/response con el PT
    @Lob
    @Column(name = "request_json")
    private String requestJson;

    @Lob
    @Column(name = "response_json")
    private String responseJson;

    @Column(name = "error_codigo", length = 50)
    private String errorCodigo;

    @Lob
    @Column(name = "error_mensaje")
    private String errorMensaje;

    // Representaciones: recomienda URL si no quieres inflar la BD
    @Column(name = "xml_url", length = 500)
    private String xmlUrl;

    @Column(name = "pdf_url", length = 500)
    private String pdfUrl;

    // Si prefieres guardar en BD, usa esto en vez de URLs:
    // @Lob @Basic(fetch = FetchType.LAZY) @Column(name="xml_data") private byte[] xmlData;
    // @Lob @Basic(fetch = FetchType.LAZY) @Column(name="pdf_data") private byte[] pdfData;

    // Tiempos (Instant recomendado)
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "enviado_at")
    private Instant enviadoAt;

    @Column(name = "aceptado_at")
    private Instant aceptadoAt;

    // ===== getters/setters mínimos =====

    public Long getId() { return id; }

    public Pedido getPedido() { return pedido; }

    public void setPedido(Pedido pedido) { this.pedido = pedido; }

    public TipoDocumentoFiscal getTipo() { return tipo; }

    public void setTipo(TipoDocumentoFiscal tipo) { this.tipo = tipo; }

    public EstadoDocumentoFiscal getEstado() { return estado; }

    public void setEstado(EstadoDocumentoFiscal estado) { this.estado = estado; }
}
