package com.facturacion.negocio.controllers;

import com.facturacion.negocio.domain.DetallePedido;
import com.facturacion.negocio.domain.Pedido;
import com.facturacion.negocio.domain.Producto;
import com.facturacion.negocio.dto.request.DetallePedidoRequestDTO;
import com.facturacion.negocio.dto.request.PedidoCreateRequestDTO;
import com.facturacion.negocio.dto.request.PedidoUpdateRequestDTO;
import com.facturacion.negocio.dto.request.PedidoUsuarioRequestDTO;
import com.facturacion.negocio.dto.response.PedidoListadoItemDTO;
import com.facturacion.negocio.dto.response.PedidoResponseDTO;
import com.facturacion.negocio.dto.response.PedidoTotalResponseDTO;
import com.facturacion.negocio.repository.ProductoRepository;
import com.facturacion.negocio.services.PedidoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/pedido")
public class PedidoController {

    private final PedidoService pedidoService;
    private final ProductoRepository productoRepository;

    public PedidoController(PedidoService pedidoService, ProductoRepository productoRepository) {
        this.pedidoService = pedidoService;
        this.productoRepository = productoRepository;
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

    @PostMapping
    public ResponseEntity<PedidoResponseDTO> crearPedido(@RequestBody PedidoCreateRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "body invalido");
        }
        List<DetallePedido> detalles = mapDetalles(request.getDetalles());
        Pedido pedido = pedidoService.crearPedido(detalles, request.getNombre());
        URI location = URI.create("/pedido/" + pedido.getId());
        return ResponseEntity.created(location).body(PedidoResponseDTO.from(pedido, detalles));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PedidoResponseDTO> editarPedido(@PathVariable Long id,
                                                         @RequestBody PedidoUpdateRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "body invalido");
        }
        List<DetallePedido> detalles = mapDetalles(request.getDetalles());
        Pedido pedido = pedidoService.editarPedido(id, detalles, request.getNombre());
        return ResponseEntity.ok(PedidoResponseDTO.from(pedido, detalles));
    }

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
                request.getCliente());
        List<DetallePedido> detalles = pedidoService.obtenerDetalles(id);
        return ResponseEntity.ok(PedidoResponseDTO.from(pedido, detalles));
    }

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

    private List<DetallePedido> mapDetalles(List<DetallePedidoRequestDTO> detalles) {
        if (detalles == null) {
            return new ArrayList<>();
        }

        Set<Long> productoIds = new HashSet<>();
        List<DetallePedido> resultado = new ArrayList<>();
        for (DetallePedidoRequestDTO detalle : detalles) {
            if (detalle == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "detalle invalido");
            }

            Long productoId = detalle.getProductoId();
            if (productoId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productoId invalido");
            }
            if (!productoIds.add(productoId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productoId repetido");
            }

            Integer cantidad = detalle.getCantidad();
            if (cantidad == null || cantidad <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cantidad invalida");
            }

            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));

            resultado.add(new DetallePedido(producto, cantidad));
        }

        return resultado;
    }
}
