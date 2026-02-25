package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.*;
import com.facturacion.negocio.enumerated.TipoGestionProducto;
import com.facturacion.negocio.enumerated.MetodoPago;
import com.facturacion.negocio.enumerated.TipoMovimiento;
import com.facturacion.negocio.enumerated.TipoMovimientoInventario;
import com.facturacion.negocio.repository.CajaRepository;
import com.facturacion.negocio.repository.MovimientoCajaRepository;
import com.facturacion.negocio.repository.MovimientoInventarioRepository;
import com.facturacion.negocio.repository.PedidoRepository;
import com.facturacion.negocio.repository.ProductoRepository;
import com.facturacion.negocio.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final ProductoRepository productoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CajaRepository cajaRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         MovimientoCajaRepository movimientoCajaRepository,
                         MovimientoInventarioRepository movimientoInventarioRepository,
                         ProductoRepository productoRepository,
                         UsuarioRepository usuarioRepository,
                         CajaRepository cajaRepository) {
        this.pedidoRepository = pedidoRepository;
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.cajaRepository = cajaRepository;
    }


    public Pedido obtenerPedido(Long id){
        if(id == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envia un ID valido");
        }
         return pedidoRepository.findById(id)
                .orElseThrow(()->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pedido no encontrado")
                );


    }

    public BigDecimal calcularTotal(Long pedidoId) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "el pedido solicitada no existe")
                );

        return pedido.calcularTotal();
    }

    public List<Pedido> obtenerPedidosPorCajaAbierta() {
        if(!cajaRepository.existeCajaAbierta()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay ninguna caja abierta");
        }
        return pedidoRepository.findPedidosByCajaAbierta();
    }

    public List<Pedido> obtenerPedidosPorCaja(Long idCaja) {
        Caja caja = cajaRepository.findById(idCaja)
                .orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "la caja solicitada no existe")
                );
        return pedidoRepository.findPedidosByCaja(caja.getId());
    }

    public List<DetallePedido> obtenerDetalles(Long pedidoId) {
        if (pedidoId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pedidoId invalido");
        }

        return pedidoRepository.findDetallesPedidoByPedido(pedidoId);
    }

    public List<DetallePedido> obtenerDetallesPorPedidos(List<Long> pedidoIds) {
        if (pedidoIds == null || pedidoIds.isEmpty()) {
            return new ArrayList<>();
        }

        return pedidoRepository.findDetallesByPedidoIds(pedidoIds);
    }

    @Transactional
    public Pedido crearPedido(List<DetallePedido> detallePedidos,
                              String nombre) {
        Caja caja = cajaRepository.obtenerCajaAbierta()
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "No puede crear un pedido en una caja cerrada")
                );
        if(!caja.isAbierta()){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "No puede crear un pedido en una caja cerrada");
        }

        List<DetallePedido> detallesNormalizados = normalizarDetalles(detallePedidos);
        validarProductosActivos(detallesNormalizados);
        reservarInventarioSiAplica(detallesNormalizados);

        boolean nombreVacio = nombre == null || nombre.trim().isEmpty();
        Pedido pedido = new Pedido(nombre, detallesNormalizados, caja);
        pedido = pedidoRepository.save(pedido);
        if (nombreVacio) {
            pedido.actualizarNombre(nombre);
            pedido = pedidoRepository.save(pedido);
        }
        return pedido;
    }


    @Transactional
    public Pedido editarPedido(Long pedidoId,
                               List<DetallePedido> detallePedidos,
                               String nombre) {
        if(pedidoId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pedidoId invalido");
        };
        List<DetallePedido> detallesNormalizados = normalizarDetalles(detallePedidos);
        validarProductosActivos(detallesNormalizados);

        Pedido pedido = pedidoRepository.findByIdForUpdate(pedidoId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "el pedido solicitado no existe")
                );
        if(!pedido.isPendiente()){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "el pedido debe estar pendiente para editarlo"
            );
        }

        List<DetallePedido> detallesActuales = new ArrayList<>(pedido.getDetallePedidos());
        liberarInventarioReservadoSiAplica(detallesActuales);
        reservarInventarioSiAplica(detallesNormalizados);

        pedido.actualizarPedido(nombre, new ArrayList<>());
        pedidoRepository.flush();
        pedido.actualizarPedido(nombre, detallesNormalizados);

        return pedidoRepository.save(pedido);
    }


    @Transactional
    public Pedido cobrarPedido(Long pedidoId, Long usuarioId, MetodoPago metodoPago) {

        if(pedidoId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "pedidoId invalido"
            );
        }

        if (usuarioId == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "usuarioId invalido"
            );
        }
        if (metodoPago == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "metodoPago invalido"
            );
        }

        Pedido pedido = pedidoRepository.findByIdForUpdate(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pedido no encontrado")
                );

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado"));


        if (!pedido.isPendiente()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "el pedido debe estar pendiente para poder cobrarlo"
            );
        }

        if (!usuario.isActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario no esta activo"
            );
        }

        if (pedido.getCaja() == null || pedido.getCaja().getId() == null) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El pedido no esta asociado a ninguna caja"
            );
        }

        // bloqueo de la fila caja para evitar concurrencia
        Caja caja = cajaRepository.findByIdForUpdate(pedido.getCaja().getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Caja no encontrada")
                );


        if (!caja.isAbierta()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La caja debe estar abierta para cobrar un pedido"
            );
        }

        // recalcular total justo antes de crear movimiento
        BigDecimal total = pedido.calcularTotal();
        if (total == null) total = BigDecimal.ZERO;

        consumirInventarioReservadoSiAplica(pedido);

        // marcar pedido como pagado, y se marca el usuario que cobro el pedido
        pedido.pagar(usuario, metodoPago);

        // crear y persistir movimiento
        MovimientoCaja movimiento = new MovimientoCaja(
                total,
                TipoMovimiento.INGRESO,
                "Venta Pedido #" + pedido.getId() + ", Vendido por: "+usuario.getUsername(),
                usuario,
                caja
        );
        movimientoCajaRepository.save(movimiento);


        return pedidoRepository.save(pedido);
    }



    @Transactional
    public Pedido cancelarPedido(Long pedidoId, Long usuarioId){
        if (pedidoId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pedidoId invalido");
        }
        if(usuarioId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "usuarioId invalido");
        }

        Pedido pedido = pedidoRepository.findByIdForUpdate(pedidoId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "No se encontro el pedido solicitado")
                );

        if(!pedido.isPendiente()){
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "el pedido debe estar pendiente para cancelar"
            );
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Usuario no encontrado")
                );

        if (!usuario.isActivo()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "El usuario debe estar activo");
        }

        liberarInventarioReservadoSiAplica(new ArrayList<>(pedido.getDetallePedidos()));

        pedido.cancelar(usuario);
        return pedidoRepository.save(pedido);
    }

    private void validarProductosActivos(List<DetallePedido> detallePedidos) {
        if (detallePedidos == null) {
            return;
        }
        for (DetallePedido detalle : detallePedidos) {
            if (detalle == null || detalle.getProducto() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "producto invalido");
            }
            if (detalle.getCantidad() == null || detalle.getCantidad() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cantidad invalida");
            }
            if (!Boolean.TRUE.equals(detalle.getProducto().isActivo())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Producto inactivo");
            }
        }
    }

    private void consumirInventarioReservadoSiAplica(Pedido pedido) {
        for (DetallePedido detalle : pedido.getDetallePedidos()) {
            Producto producto = detalle.getProducto();
            if (producto == null || producto.getTipoGestion() != TipoGestionProducto.INVENTARIABLE) {
                continue;
            }
            int reservadoActual = producto.getStockReservado() != null ? producto.getStockReservado() : 0;
            int faltanteReservado = detalle.getCantidad() - reservadoActual;
            if (faltanteReservado > 0) {
                // Compatibilidad con pedidos pendientes creados antes del modelo de reservas.
                producto.reservarStock(faltanteReservado);
            }
            producto.consumirStockReservado(detalle.getCantidad());
            movimientoInventarioRepository.save(new MovimientoInventario(
                    producto,
                    TipoMovimientoInventario.VENTA,
                    -detalle.getCantidad(),
                    "PEDIDO",
                    pedido.getId(),
                    "Descuento por venta de pedido #" + pedido.getId()
            ));
        }
    }

    private void reservarInventarioSiAplica(List<DetallePedido> detallePedidos) {
        for (DetallePedido detalle : detallePedidos) {
            Producto producto = detalle.getProducto();
            if (producto == null || producto.getTipoGestion() != TipoGestionProducto.INVENTARIABLE) {
                continue;
            }
            producto.reservarStock(detalle.getCantidad());
        }
    }

    private void liberarInventarioReservadoSiAplica(List<DetallePedido> detallePedidos) {
        for (DetallePedido detalle : detallePedidos) {
            Producto producto = detalle.getProducto();
            if (producto == null || producto.getTipoGestion() != TipoGestionProducto.INVENTARIABLE) {
                continue;
            }
            int reservadoActual = producto.getStockReservado() != null ? producto.getStockReservado() : 0;
            if (reservadoActual <= 0) {
                continue;
            }
            int cantidadALiberar = Math.min(detalle.getCantidad(), reservadoActual);
            producto.liberarStockReservado(cantidadALiberar);
        }
    }

    private List<DetallePedido> normalizarDetalles(List<DetallePedido> detallePedidos) {
        if (detallePedidos == null) {
            return new ArrayList<>();
        }
        List<DetallePedido> resultado = new ArrayList<>();
        for (DetallePedido detalle : detallePedidos) {
            if (detalle == null || detalle.getProducto() == null || detalle.getProducto().getCodigo() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "producto invalido");
            }
            Integer cantidad = detalle.getCantidad();
            if (cantidad == null || cantidad <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cantidad invalida");
            }
            Producto producto = productoRepository.findById(detalle.getProducto().getCodigo())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
            resultado.add(new DetallePedido(producto, cantidad));
        }
        return resultado;
    }
}
