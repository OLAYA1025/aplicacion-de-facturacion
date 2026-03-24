package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.DocumentoFiscal;
import com.facturacion.negocio.domain.NumberingRange;
import com.facturacion.negocio.dto.factura.Establecimiento;
import com.facturacion.negocio.dto.factura.NumberAllocationResult;
import com.facturacion.negocio.dto.response.DocumentoFiscalListDTO;
import com.facturacion.negocio.dto.factura.FactusFactElectronicaRequestDTO;
import com.facturacion.negocio.enumerated.EstadoDocumentoFiscal;
import com.facturacion.negocio.errors.FactusSinConexionException;
import com.facturacion.negocio.mock.ProveedorTecnologicoClientFactus;
import com.facturacion.negocio.repository.DetallePedidoRepository;
import com.facturacion.negocio.repository.DocumentoFiscalRepository;
import com.facturacion.negocio.repository.NumberingRangeRepository;
import com.facturacion.negocio.repository.PedidoTotalProjection;
import com.facturacion.negocio.services.factus.FactusFactElectronicaRequestMapper;
import com.facturacion.negocio.services.tirillaFactura.Factura;
import com.facturacion.negocio.services.tirillaFactura.PdfService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class DocumentoFiscalService {

    private static final int MAX_PAGE_SIZE = 200;
    private static final Set<String> CAMPOS_ORDEN_PERMITIDOS =
            Set.of("id", "createdAt", "estado", "numeroDocumento", "referenceCode", "pedidoId");

    private final DocumentoFiscalRepository documentoFiscalRepository;
    private final DetallePedidoRepository detallePedidoRepository;
    private final ProveedorTecnologicoClientFactus proveedorTecnologicoClient;
    private final FactusFactElectronicaRequestMapper mapper;
    private final Establecimiento establecimiento;
    private final PdfService pdfService;
    private final DocumentoFiscalTxService documentoFiscalTxService;
    private final ZoneId zonaAplicacion;
    private final NumberingRangeService numberingRangeService;
    private final NumberingRangeRepository numberingRangeRepository;

    public DocumentoFiscalService(DocumentoFiscalRepository documentoFiscalRespository,
                                  DetallePedidoRepository detallePedidoRepository,
                                  ProveedorTecnologicoClientFactus proveedorTecnologicoClient,
                                  FactusFactElectronicaRequestMapper mapper,
                                  Establecimiento establecimiento,
                                  PdfService pdfService,
                                  DocumentoFiscalTxService documentoFiscalTxService,
                                  @Value("${app.time-zone:America/Bogota}") String zonaHoraria,
                                  NumberingRangeService numberingRangeService,
                                  NumberingRangeRepository numberingRangeRepository) {

        this.documentoFiscalRepository = documentoFiscalRespository;
        this.detallePedidoRepository = detallePedidoRepository;
        this.proveedorTecnologicoClient = proveedorTecnologicoClient;
        this.mapper = mapper;
        this.establecimiento = establecimiento;
        this.pdfService = pdfService;
        this.documentoFiscalTxService = documentoFiscalTxService;
        this.zonaAplicacion = ZoneId.of(zonaHoraria);
        this.numberingRangeService = numberingRangeService;
        this.numberingRangeRepository = numberingRangeRepository;
    }

    @Transactional
    public void enviarDocumentoDian(Long documentoFiscalId, Long numberingRangeId) {
        DocumentoFiscal doc = documentoFiscalRepository.findById(documentoFiscalId)
                .orElseThrow();

        NumberAllocationResult allocation = numberingRangeService.allocateNext(numberingRangeId);

        NumberingRange range = numberingRangeRepository.findById(allocation.rangeId())
                .orElseThrow();

        doc.setNumberingRange(range);
        doc.setConsecutivo(allocation.consecutive());
        doc.setNumeroDocumento(allocation.documentNumber());

        // luego:
        // DianInvoiceData data = dianInvoiceMapper.from(doc);
        // String xml = dianInvoiceXmlBuilder.build(data);
        // ...
    }

    @Transactional(readOnly = true)
    public DocumentoFiscal buscarPorId(Long id){
        return documentoFiscalRepository.findById(id).orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "documento fiscal no encontrado"));
    }

    @Transactional
    public boolean tomarDocumentoParaEnvio(Long documentoFiscalId) {
        return documentoFiscalTxService.tomarDocumentoParaEnvio(documentoFiscalId);
    }

    public void enviarAlProveedor(Long documentoFiscalId) {
        boolean tomado = tomarDocumentoParaEnvio(documentoFiscalId);

        if (!tomado) {
            return;
        }

        DocumentoFiscal doc = documentoFiscalRepository.findByIdForEnvio(documentoFiscalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Documento fiscal no encontrado"));

        try {
            FactusFactElectronicaRequestDTO request = mapper.from(doc);

            guardarRequestJson(documentoFiscalId, request);

            JsonNode resp = proveedorTecnologicoClient.validarFactura(request);

            procesarRespuestaExitosa(documentoFiscalId, resp);

        } catch (HttpClientErrorException e) {
            manejarHttpClientError(documentoFiscalId, e);

        } catch (FactusSinConexionException e) {
            marcarPendienteContingencia(
                    documentoFiscalId,
                    "SIN_CONEXION",
                    e.getMessage()
            );

        } catch (Exception e) {
            marcarPendienteContingencia(
                    documentoFiscalId,
                    "EXCEPTION",
                    e.getMessage()
            );
        }
    }

    @Transactional
    public void procesarRespuestaExitosa(Long documentoFiscalId, JsonNode resp) {
        documentoFiscalTxService.procesarRespuestaExitosa(documentoFiscalId, resp);
    }

    public void manejarHttpClientError(Long documentoFiscalId, HttpClientErrorException e) {
        int status = e.getStatusCode().value();
        String body = e.getResponseBodyAsString();

        if (status == 422) {
            marcarRechazado(documentoFiscalId, "422", body);
            return;
        }

        if (status == 409) {
            manejarDuplicado409(documentoFiscalId, body);
            return;
        }

        marcarPendienteContingencia(
                documentoFiscalId,
                "HTTP_" + status,
                body
        );
    }

    @Transactional
    public void manejarDuplicado409(Long documentoFiscalId, String detalle) {
        documentoFiscalTxService.manejarDuplicado409(documentoFiscalId, detalle);
    }

    @Transactional
    public void marcarRechazado(Long documentoFiscalId, String codigo, String detalle) {
        documentoFiscalTxService.marcarRechazado(documentoFiscalId, codigo, detalle);
    }

    @Transactional
    public void marcarPendienteContingencia(Long documentoFiscalId, String codigo, String detalle) {
        documentoFiscalTxService.marcarPendienteContingencia(documentoFiscalId, codigo, detalle);
    }

    @Transactional
    public void guardarRequestJson(Long documentoFiscalId, FactusFactElectronicaRequestDTO request) {
        documentoFiscalTxService.guardarRequestJson(documentoFiscalId, request);
    }

    @Transactional
    public void ReenviarFactura(Long id) throws JsonProcessingException {
        DocumentoFiscal doc = documentoFiscalRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Documento no encontrado")
        );
        if (doc.getEstado() != EstadoDocumentoFiscal.PENDIENTE_CONTINGENCIA){
            throw  new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La factura debe estar pendiente por contingencia para reenviar");
        }

        doc.reenviar();
    }

    public String obtenerMensajeError(Long id){
        DocumentoFiscal doc = documentoFiscalRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Documento no encontrado")
        );

        if (doc.getEstado() != EstadoDocumentoFiscal.PENDIENTE_CONTINGENCIA &&
                doc.getEstado() != EstadoDocumentoFiscal.RECHAZADO){
            throw
                    new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "solo puedes ver el mensaje de error de un documento pendiente por contingencia o rechazado");
        }
        return doc.getErrorMensaje();
    }

    @Transactional(readOnly = true)
    public byte[] obtenerPdf(Long documentoFiscalId) {
        DocumentoFiscal doc = documentoFiscalRepository.findById(documentoFiscalId).orElseThrow(()->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Documento de fiscal no encontrado"));

        if (doc.getEstado() != EstadoDocumentoFiscal.ACEPTADO_DIAN) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "PDF no disponible aún");
        }

        // según tu implementación: por numeroDocumento o por cufe
        return proveedorTecnologicoClient.obtenerPdf(doc.getNumeroDocumento());
    }

    @Transactional(readOnly = true)
    public byte[] obtenerPdfTirilla(Long documentoFiscalId) {
        DocumentoFiscal doc = documentoFiscalRepository.findById(documentoFiscalId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Documento fiscal no encontrado"));

        return pdfService.generarPdf(new Factura(doc, establecimiento));
    }


    @Transactional(readOnly = true)
    public Page<DocumentoFiscalListDTO> listarDocumentosFiscales(int page,
                                                                  int size,
                                                                  String q,
                                                                  List<String> estadosRaw,
                                                                  String fromRaw,
                                                                  String toRaw,
                                                                  String sortRaw) {
        validarPaginacion(page, size);
        List<EstadoDocumentoFiscal> estados = parseEstados(estadosRaw);
        Instant from = parseFecha(fromRaw, true);
        Instant to = parseFecha(toRaw, false);
        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rango de fechas invalido");
        }

        Pageable pageable = PageRequest.of(page, size, parseSort(sortRaw));
        Specification<DocumentoFiscal> spec = construirSpecification(q, estados, from, to);

        Page<DocumentoFiscal> documentosPage = documentoFiscalRepository.findAll(spec, pageable);
        Map<Long, BigDecimal> totalesPorPedidoId = calcularTotalesPorPedido(documentosPage.getContent());

        return documentosPage.map(documentoFiscal -> {
            Long pedidoId = documentoFiscal.getPedido() != null ? documentoFiscal.getPedido().getId() : null;
            BigDecimal total = pedidoId != null
                    ? totalesPorPedidoId.getOrDefault(pedidoId, BigDecimal.ZERO)
                    : null;
            return DocumentoFiscalListDTO.from(documentoFiscal, total);
        });
    }

    private Map<Long, BigDecimal> calcularTotalesPorPedido(List<DocumentoFiscal> documentosFiscales) {
        List<Long> pedidoIds = documentosFiscales.stream()
                .map(DocumentoFiscal::getPedido)
                .filter(Objects::nonNull)
                .map(pedido -> pedido.getId())
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (pedidoIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, BigDecimal> totalesPorPedido = new HashMap<>();
        for (PedidoTotalProjection totalProjection : detallePedidoRepository.findTotalesPorPedidoIds(pedidoIds)) {
            BigDecimal total = totalProjection.getTotal() != null ? totalProjection.getTotal() : BigDecimal.ZERO;
            totalesPorPedido.put(totalProjection.getPedidoId(), total);
        }
        return totalesPorPedido;
    }

    private void validarPaginacion(int page, int size) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page invalido");
        }
        if (size <= 0 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size invalido");
        }
    }

    private List<EstadoDocumentoFiscal> parseEstados(List<String> estadosRaw) {
        if (estadosRaw == null || estadosRaw.isEmpty()) {
            return List.of();
        }

        List<EstadoDocumentoFiscal> estados = new ArrayList<>();
        for (String estadoRaw : estadosRaw) {
            if (estadoRaw == null || estadoRaw.isBlank()) {
                continue;
            }
            try {
                estados.add(EstadoDocumentoFiscal.valueOf(estadoRaw.trim().toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "estado invalido: " + estadoRaw);
            }
        }
        return estados;
    }

    private Sort parseSort(String sortRaw) {
        if (sortRaw == null || sortRaw.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "createdAt");
        }

        String[] parts = sortRaw.split(",");
        String campo = parts[0].trim();
        if (!CAMPOS_ORDEN_PERMITIDOS.contains(campo)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "sort invalido");
        }

        Sort.Direction direccion = Sort.Direction.ASC;
        if (parts.length > 1 && !parts[1].trim().isEmpty()) {
            direccion = Sort.Direction.fromOptionalString(parts[1].trim().toLowerCase(Locale.ROOT))
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "sort invalido"));
        }

        String propiedad = "pedidoId".equals(campo) ? "pedido.id" : campo;
        return Sort.by(direccion, propiedad);
    }

    private Instant parseFecha(String raw, boolean isFrom) {
        if (raw == null || raw.isBlank()) {
            return null;
        }

        String value = raw.trim();

        try {
            LocalDate date = LocalDate.parse(value);
            if (isFrom) {
                return date.atStartOfDay(zonaAplicacion).toInstant();
            }
            return date.plusDays(1).atStartOfDay(zonaAplicacion).toInstant().minusNanos(1);
        } catch (DateTimeParseException ignored) {
        }

        try {
            return Instant.parse(value);
        } catch (DateTimeParseException ignored) {
        }

        try {
            return OffsetDateTime.parse(value).toInstant();
        } catch (DateTimeParseException ignored) {
        }

        try {
            TemporalAccessor parsed = DateTimeFormatter.ISO_LOCAL_DATE_TIME.parse(value);
            return ZonedDateTime.of(
                    LocalDate.from(parsed),
                    LocalTime.from(parsed),
                    zonaAplicacion
            ).toInstant();
        } catch (DateTimeParseException ignored) {
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, (isFrom ? "from" : "to") + " invalido");
    }

    private Specification<DocumentoFiscal> construirSpecification(String q,
                                                                  List<EstadoDocumentoFiscal> estados,
                                                                  Instant from,
                                                                  Instant to) {
        Specification<DocumentoFiscal> spec = (root, query, cb) -> cb.conjunction();

        String busqueda = q != null ? q.trim() : "";
        if (!busqueda.isEmpty()) {
            spec = spec.and((root, query, cb) -> {
                Join<Object, Object> cliente = root.join("cliente", JoinType.LEFT);
                Join<Object, Object> pedido = root.join("pedido", JoinType.LEFT);

                String likeValue = "%" + busqueda.toLowerCase(Locale.ROOT) + "%";
                List<Predicate> orPredicates = new ArrayList<>();
                orPredicates.add(cb.like(cb.lower(cb.coalesce(cliente.get("nombre"), "")), likeValue));
                orPredicates.add(cb.like(cb.lower(cb.coalesce(cliente.get("numeroIdentificacion"), "")), likeValue));
                orPredicates.add(cb.like(cb.lower(cb.coalesce(root.get("numeroDocumento"), "")), likeValue));
                orPredicates.add(cb.like(cb.lower(cb.coalesce(root.get("referenceCode"), "")), likeValue));

                if (esNumero(busqueda)) {
                    try {
                        Long pedidoId = Long.parseLong(busqueda);
                        orPredicates.add(cb.equal(pedido.get("id"), pedidoId));
                    } catch (NumberFormatException ignored) {
                    }
                }

                return cb.or(orPredicates.toArray(new Predicate[0]));
            });
        }

        if (estados != null && !estados.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("estado").in(estados));
        }

        if (from != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("createdAt"), from));
        }

        if (to != null) {
            spec = spec.and((root, query, cb) -> cb.lessThanOrEqualTo(root.get("createdAt"), to));
        }

        return spec;
    }

    private boolean esNumero(String value) {
        for (int i = 0; i < value.length(); i++) {
            if (!Character.isDigit(value.charAt(i))) {
                return false;
            }
        }
        return !value.isEmpty();
    }

}
