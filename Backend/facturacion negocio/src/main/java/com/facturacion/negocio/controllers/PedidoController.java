package com.facturacion.negocio.controllers;

import com.facturacion.negocio.domain.DetallePedido;
import com.facturacion.negocio.domain.DocumentoFiscal;
import com.facturacion.negocio.domain.Pedido;
import com.facturacion.negocio.dto.request.PedidoCreateRequestDTO;
import com.facturacion.negocio.dto.request.PedidoUpdateRequestDTO;
import com.facturacion.negocio.dto.request.PedidoUsuarioRequestDTO;
import com.facturacion.negocio.dto.response.PedidoListadoItemDTO;
import com.facturacion.negocio.dto.response.PedidoResponseDTO;
import com.facturacion.negocio.dto.response.PedidoTotalResponseDTO;
import com.facturacion.negocio.enumerated.EstadoDocumentoFiscal;
import com.facturacion.negocio.enumerated.TipoDocumentoFiscal;
import com.facturacion.negocio.services.DocumentoFiscalService;
import com.facturacion.negocio.services.PedidoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pedido")
public class PedidoController {

    private final PedidoService pedidoService;
    private final DocumentoFiscalService documentoFiscalService;

    public PedidoController(PedidoService pedidoService, DocumentoFiscalService documentoFiscalService) {
        this.pedidoService = pedidoService;
        this.documentoFiscalService = documentoFiscalService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> obtenerPedido(@PathVariable Long id) {
        Pedido pedido = pedidoService.obtenerPedido(id);
        List<DetallePedido> detalles = pedidoService.obtenerDetalles(id);
        return ResponseEntity.ok(PedidoResponseDTO.from(pedido, detalles));
    }

    @GetMapping("/{id}/total")
    public ResponseEntity<PedidoTotalResponseDTO> obtenerTotal(@PathVariable Long id) {
        BigDecimal total = pedidoService.calcularTotal(id);
        return ResponseEntity.ok(PedidoTotalResponseDTO.from(id, total));
    }

    @GetMapping("/caja/abierta")
    public ResponseEntity<List<PedidoResponseDTO>> obtenerPedidosCajaAbierta() {
        List<PedidoListadoItemDTO> pedidos = pedidoService.obtenerPedidosPorCajaAbierta();
        List<Long> pedidoIds = new ArrayList<>(pedidos.size());
        for (PedidoListadoItemDTO pedido : pedidos) {
            pedidoIds.add(pedido.id());
        }

        List<DetallePedido> detalles = pedidoService.obtenerDetallesPorPedidos(pedidoIds);
        Map<Long, List<DetallePedido>> detallesPorPedido = new HashMap<>();
        for (DetallePedido detalle : detalles) {
            Long pedidoId = detalle.getPedidoId();
            if (pedidoId == null) {
                continue;
            }

            List<DetallePedido> lista = detallesPorPedido.get(pedidoId);
            if (lista == null) {
                lista = new ArrayList<>();
                detallesPorPedido.put(pedidoId, lista);
            }
            lista.add(detalle);
        }

        List<PedidoResponseDTO> response = new ArrayList<>(pedidos.size());
        for (PedidoListadoItemDTO pedido : pedidos) {
            List<DetallePedido> detallesPedido = detallesPorPedido.get(pedido.id());
            if (detallesPedido == null) {
                detallesPedido = new ArrayList<>();
            }
            response.add(PedidoResponseDTO.fromListado(pedido, detallesPedido));
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/caja/{cajaId}")
    public ResponseEntity<List<PedidoResponseDTO>> obtenerPedidosPorCaja(@PathVariable Long cajaId) {
        List<PedidoListadoItemDTO> pedidos = pedidoService.obtenerPedidosPorCaja(cajaId);
        List<Long> pedidoIds = new ArrayList<>(pedidos.size());
        for (PedidoListadoItemDTO pedido : pedidos) {
            pedidoIds.add(pedido.id());
        }

        List<DetallePedido> detalles = pedidoService.obtenerDetallesPorPedidos(pedidoIds);
        Map<Long, List<DetallePedido>> detallesPorPedido = new HashMap<>();
        for (DetallePedido detalle : detalles) {
            Long pedidoId = detalle.getPedidoId();
            if (pedidoId == null) {
                continue;
            }

            List<DetallePedido> lista = detallesPorPedido.get(pedidoId);
            if (lista == null) {
                lista = new ArrayList<>();
                detallesPorPedido.put(pedidoId, lista);
            }
            lista.add(detalle);
        }

        List<PedidoResponseDTO> response = new ArrayList<>(pedidos.size());
        for (PedidoListadoItemDTO pedido : pedidos) {
            List<DetallePedido> detallesPedido = detallesPorPedido.get(pedido.id());
            if (detallesPedido == null) {
                detallesPedido = new ArrayList<>();
            }
            response.add(PedidoResponseDTO.fromListado(pedido, detallesPedido));
        }
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'MESERO')")
    @PostMapping
    public ResponseEntity<PedidoResponseDTO> crearPedido(@RequestBody PedidoCreateRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "body invalido");
        }
        Pedido pedido = pedidoService.crearPedido(request.getDetalles(), request.getNombre());
        List<DetallePedido> detalles = pedidoService.obtenerDetalles(pedido.getId());
        URI location = URI.create("/pedido/" + pedido.getId());
        return ResponseEntity.created(location).body(PedidoResponseDTO.from(pedido, detalles));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO', 'MESERO')")
    @PutMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> editarPedido(@PathVariable Long id,
                                                         @RequestBody PedidoUpdateRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "body invalido");
        }
        Pedido pedido = pedidoService.editarPedido(id, request.getDetalles(), request.getNombre());
        List<DetallePedido> detalles = pedidoService.obtenerDetalles(pedido.getId());
        return ResponseEntity.ok(PedidoResponseDTO.from(pedido, detalles));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    @PostMapping("/{id}/cobrar")
    public ResponseEntity<PedidoResponseDTO> cobrarPedido(@PathVariable Long id,
                                                          @RequestBody PedidoUsuarioRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "body invalido");
        }
        Pedido pedido = pedidoService.cobrarPedido(id,
                request.getUsuarioId(),
                request.getMetodoPago(),
                request.getTipoDocumentoFiscal(),
                request.getCliente(),
                request.getRangeId());
        List<DetallePedido> detalles = pedidoService.obtenerDetalles(id);
        return ResponseEntity.ok(PedidoResponseDTO.from(pedido, detalles));
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    @PostMapping("/{id}/imprimir")
    public ResponseEntity<?> imprimirPedido(@PathVariable Long id) {
        Pedido pedido = pedidoService.buscarPorId(id);

        DocumentoFiscal doc = pedido.getDocumentoFiscal();
        if (doc == null) {
            byte[] pdfBytes = pedidoService.obtenerPdfTirillaPreFactura(id);

            String filename = "PEDIDO-"+pedido.getId()+".pdf";

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .body(pdfBytes);

        }

        if (doc.getEstado() != EstadoDocumentoFiscal.ACEPTADO_DIAN) {
            documentoFiscalService.enviarAlProveedor(doc.getId());
        }

        DocumentoFiscal actualizado = documentoFiscalService.buscarPorId(doc.getId());

        if (actualizado.getEstado() != EstadoDocumentoFiscal.ACEPTADO_DIAN) {
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(
                    Map.of(
                            "mensaje", "El pedido ya fue cobrado, pero la factura quedó pendiente de envío",
                            "pedidoId", pedido.getId(),
                            "documentoFiscalId", actualizado.getId(),
                            "estadoDocumento", actualizado.getEstado().name()
                    )
            );
        }

        byte[] pdf = documentoFiscalService.obtenerPdfTirilla(actualizado.getId());

        String filename =
                (actualizado.getTipo() == TipoDocumentoFiscal.FEV_CONS_FINAL ? "FEV-CONSUMIDOR-FINAL-" : "FEV-") +
                        (actualizado.getNumeroDocumento() != null ? actualizado.getNumeroDocumento() : actualizado.getCufe()) +
                        ".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(pdf);
    }

    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'CAJERO')")
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<PedidoResponseDTO> cancelarPedido(@PathVariable Long id,
                                                            @RequestBody PedidoUsuarioRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "body invalido");
        }
        Pedido pedido = pedidoService.cancelarPedido(id, request.getUsuarioId());
        List<DetallePedido> detalles = pedidoService.obtenerDetalles(id);
        return ResponseEntity.ok(PedidoResponseDTO.from(pedido, detalles));
    }

    @GetMapping("/{id}/tirilla/pdf")
    public ResponseEntity<byte[]> pdfTirilla(@PathVariable Long id) {
        Pedido pedido = pedidoService.buscarPorId(id);

        byte[] pdfBytes = pedidoService.obtenerPdfTirillaPreFactura(id);

        String filename = "PEDIDO-"+pedido.getId()+".pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                .body(pdfBytes);
    }
}
