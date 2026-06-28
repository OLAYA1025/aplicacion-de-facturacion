package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.*;
import com.facturacion.negocio.dto.request.ClienteFiscalDTO;
import com.facturacion.negocio.dto.request.DetallePedidoRequestDTO;
import com.facturacion.negocio.dto.response.PedidoListadoItemDTO;
import com.facturacion.negocio.enumerated.*;
import com.facturacion.negocio.repository.*;
import com.facturacion.negocio.services.tirillaFactura.Factura;
import com.facturacion.negocio.services.tirillaFactura.PdfService;
import com.facturacion.negocio.services.tirillaFactura.PreFactura;
import org.springframework.beans.factory.annotation.Value;
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
    private final ClienteRepository clienteRepository;
    private final DocumentoFiscalRepository documentoFiscalRepository;
    private final DocumentoFiscalService documentoFiscalService;
    private final NumberingRangeRepository numberingRangeRepository;
    private final SseService sseService;
    private final NotasPredefinidasService notasPredefinidasService;
    private final PdfService pdfService;
    private final Boolean electronicaHabilitada;
    private final TenantService tenantService;
    private final ConfiguracionFacturacionElectronicaRepository configuracionFacturacionElectronicaRepository;

    public PedidoService(PedidoRepository pedidoRepository,
                         MovimientoCajaRepository movimientoCajaRepository,
                         MovimientoInventarioRepository movimientoInventarioRepository,
                         ProductoRepository productoRepository,
                         UsuarioRepository usuarioRepository,
                         CajaRepository cajaRepository,
                         ClienteRepository clienteRepository,
                         DocumentoFiscalRepository documentoFiscalRepository,
                         DocumentoFiscalService documentoFiscalService,
                         NumberingRangeRepository numberingRangeRepository,
                         SseService sseService,
                         NotasPredefinidasService notasPredefinidasService,
                         PdfService pdfService,
                         TenantService tenantService,
                         @Value("${electronicaHabilitada}") Boolean electronicaHabilitada,
                         ConfiguracionFacturacionElectronicaRepository configuracionFacturacionElectronicaRepository) {
        this.pedidoRepository = pedidoRepository;
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.productoRepository = productoRepository;
        this.usuarioRepository = usuarioRepository;
        this.cajaRepository = cajaRepository;
        this.clienteRepository = clienteRepository;
        this.documentoFiscalRepository = documentoFiscalRepository;
        this.documentoFiscalService = documentoFiscalService;
        this.numberingRangeRepository = numberingRangeRepository;
        this.sseService = sseService;
        this.notasPredefinidasService = notasPredefinidasService;
        this.pdfService = pdfService;
        this.tenantService = tenantService;
        this.electronicaHabilitada = electronicaHabilitada;
        this.configuracionFacturacionElectronicaRepository = configuracionFacturacionElectronicaRepository;
    }


    public Pedido obtenerPedido(Long id){
        if(id == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envia un ID valido");
        }
         return pedidoRepository.findByIdAndNegocio(id, tenantService.getNegocioActual())
                .orElseThrow(()->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Pedido no encontrado")
                );


    }

    @Transactional(readOnly = true)
    public BigDecimal calcularTotal(Long pedidoId) {
        Pedido pedido = pedidoRepository.findByIdAndNegocio(pedidoId, tenantService.getNegocioActual())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "el pedido solicitada no existe")
                );

        return pedido.calcularTotal();
    }

    public List<PedidoListadoItemDTO> obtenerPedidosPorCajaAbierta() {
        Negocio negocio = tenantService.getNegocioActual();
        if(!cajaRepository.existeCajaAbierta(negocio)){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No hay ninguna caja abierta");
        }
        return pedidoRepository.findResumenPedidosByCajaAbierta(negocio);
    }

    public List<PedidoListadoItemDTO> obtenerPedidosPorCaja(Long idCaja) {
        Negocio negocio = tenantService.getNegocioActual();
        Caja caja = cajaRepository.findByIdAndNegocio(idCaja, negocio)
                .orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "la caja solicitada no existe")
                );
        return pedidoRepository.findResumenPedidosByCaja(caja.getId(), negocio);
    }

    public List<DetallePedido> obtenerDetalles(Long pedidoId) {
        if (pedidoId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pedidoId invalido");
        }

        return pedidoRepository.findDetallesPedidoByPedido(pedidoId, tenantService.getNegocioActual());
    }

    public List<DetallePedido> obtenerDetallesPorPedidos(List<Long> pedidoIds) {
        if (pedidoIds == null || pedidoIds.isEmpty()) {
            return new ArrayList<>();
        }

        return pedidoRepository.findDetallesByPedidoIds(pedidoIds, tenantService.getNegocioActual());
    }

    @Transactional
    public Pedido crearPedido(List<DetallePedidoRequestDTO> detallePedidos,
                              String nombre) {
        Negocio negocio = tenantService.getNegocioActual();
        Caja caja = cajaRepository.obtenerCajaAbierta(negocio)
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
        pedido.setNegocio(negocio);
        pedido = pedidoRepository.save(pedido);
        if (nombreVacio) {
            pedido.actualizarNombre(nombre);
            pedido = pedidoRepository.save(pedido);
        }
        sseService.emitirActualizacionPedido(pedido, detallesNormalizados);
        return pedido;
    }


    @Transactional(readOnly = true)
    public byte[] obtenerPdfTirillaPreFactura(Long pedidoId) {
        Pedido pedido = pedidoRepository.findByIdAndNegocio(pedidoId, tenantService.getNegocioActual())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Pedido no encontrado"));

        return pdfService.generarPdfPreFactura(new PreFactura(pedido));
    }


    @Transactional
    public Pedido editarPedido(Long pedidoId,
                               List<DetallePedidoRequestDTO> detallePedidos,
                               String nombre) {
        if(pedidoId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pedidoId invalido");
        };
        List<DetallePedido> detallesNormalizados = normalizarDetalles(detallePedidos);
        validarProductosActivos(detallesNormalizados);

        Pedido pedido = pedidoRepository.findByIdForUpdate(pedidoId, tenantService.getNegocioActual())
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

        Pedido pedidoActualizado = pedidoRepository.save(pedido);
        sseService.emitirActualizacionPedido(pedidoActualizado, detallesNormalizados);
        return pedidoActualizado;
    }



    @Transactional(readOnly = true)
    public Pedido buscarPorId(Long pedidoId) {
        return pedidoRepository.findByIdAndNegocio(pedidoId, tenantService.getNegocioActual())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
    }



    @Transactional
    public Pedido cobrarPedido(Long pedidoId,
                               Long usuarioId,
                               MetodoPago metodoPago,
                               TipoDocumentoFiscal tipoDocumentoFiscal,
                               ClienteFiscalDTO clienteDto,
                               Long rangeId) {

        // ===== Validaciones básicas =====
        if (pedidoId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pedidoId invalido");
        }
        if (usuarioId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "usuarioId invalido");
        }
        if (metodoPago == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "metodoPago invalido");
        }

        // Default para retail
        if (tipoDocumentoFiscal == null) {
            tipoDocumentoFiscal = TipoDocumentoFiscal.FEV_CONS_FINAL;
        }
        Negocio negocio = tenantService.getNegocioActual();

        // ===== 1) Bloquear y validar PEDIDO primero =====
        Pedido pedido = pedidoRepository.findByIdForUpdate(pedidoId, negocio)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));

        if (!pedido.isPendiente()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "el pedido debe estar pendiente para poder cobrarlo");
        }

        // Evitar doble documento fiscal / doble cobro por reintento
        if (pedido.getDocumentoFiscal() != null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El pedido ya tiene documento fiscal asociado");
            // Alternativa idempotente: return pedido;
        }

        // ===== 2) Validar usuario =====
        Usuario usuario = usuarioRepository.findByIdAndNegocio(usuarioId, negocio)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (!usuario.isActivo()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El usuario no esta activo");
        }

        // ===== 3) Validar y bloquear CAJA =====
        if (pedido.getCaja() == null || pedido.getCaja().getId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El pedido no esta asociado a ninguna caja");
        }

        Caja caja = cajaRepository.findByIdForUpdate(pedido.getCaja().getId(), negocio)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Caja no encontrada"));

        if (!caja.isAbierta()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La caja debe estar abierta para cobrar un pedido");
        }

        if(negocio.isElectronicaHabilitada()){
            // ===== 4) Resolver cliente SOLO si es FEV =====
            Cliente cliente = null;

            if (tipoDocumentoFiscal == TipoDocumentoFiscal.FEV) {
                if (clienteDto == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                            "debes enviar un cliente para crear una factura electronica");
                }

                // Validación mínima (email opcional)
                if (clienteDto.getTipoIdentificacion() == null
                        || clienteDto.getNumeroIdentificacion() == null
                        || clienteDto.getNombre() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envia los datos del cliente");
                }

                // Buscar por (tipo, numero); si no existe, crear
                cliente = clienteRepository
                        .findByTipoIdentificacionAndNumeroIdentificacionAndNegocio(
                                clienteDto.getTipoIdentificacion(),
                                clienteDto.getNumeroIdentificacion(),
                                negocio
                        )
                        .orElseGet(() -> clienteRepository.save(
                                tenantService.asignarNegocioActual(new Cliente(
                                        clienteDto.getTipoIdentificacion(),
                                        clienteDto.getNumeroIdentificacion(),
                                        clienteDto.getNombre(),
                                        clienteDto.getEmail()
                                ))
                        ));


            }

            if (tipoDocumentoFiscal == TipoDocumentoFiscal.FEV_CONS_FINAL) {
                cliente = clienteRepository
                        .findByTipoIdentificacionAndNumeroIdentificacionAndNegocio(
                                TipoDocumento.CC,
                                "222222222222",
                                negocio
                        )
                        .orElseGet(() -> clienteRepository.save(
                                tenantService.asignarNegocioActual(new Cliente(
                                        TipoDocumento.CC,
                                        "222222222222",
                                        "consumidor final",
                                        null
                                ))
                        ));

            }

            ConfiguracionFacturacionElectronica configElectronica = configuracionFacturacionElectronicaRepository.findByNegocioId(negocio.getId())
                    .orElseThrow(() -> new
                            ResponseStatusException(
                                    HttpStatus.NOT_FOUND, "El establecimiento no tiene una configuracion de facturacion electronica asociada") );

            // ===== 5) Crear DocumentoFiscal y asociarlo al pedido =====
            DocumentoFiscal documentoFiscal = DocumentoFiscal.crearBorrador(pedido, tipoDocumentoFiscal, configElectronica.getPrefijoCode());
            numberingRangeRepository.findFirstByConfiguracionIdAndActiveTrueOrderByIdAsc(configElectronica.getId())
                    .ifPresent(range -> {
                        documentoFiscal.setNumberingRange(range);
                        if (range.getProviderRangeId() != null) {
                            documentoFiscal.setNumberingRangeId(range.getProviderRangeId());
                        }
                    });


            if (cliente != null) {
                documentoFiscal.setCliente(cliente);
            }

            //asociar para que se persista correctamente
            pedido.setDocumentoFiscal(documentoFiscal);

            documentoFiscal.marcarPendienteEnvio(); //si se implementan los numbering ranges, se debe primero verificar si es o no de la DIAN
        }
        // ===== 6) Recalcular total, consumir inventario, pagar =====
        BigDecimal total = pedido.calcularTotal();
        if (total == null) total = BigDecimal.ZERO;

        consumirInventarioReservadoSiAplica(pedido);

        pedido.pagar(usuario, metodoPago);

        // El documento queda listo para que un worker lo envíe al PT
        /*
        NumberingRange numberingRange = numberingRangeRepository.findById(rangeId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El rango no existe"));

        if (!numberingRange.getActive()){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El rango esta deshabilitado");
        }

        if(numberingRange.getProvider() != ProveedorFacturacion.DIAN) {
            documentoFiscal.marcarPendienteEnvio();
            //TODO: implementar los numberingRanges despues
        }

         */
        // ===== 7) Movimiento de caja =====
        MovimientoCaja movimiento = new MovimientoCaja(
                total,
                TipoMovimiento.INGRESO,
                "Venta Pedido #" + pedido.getId() + ", Vendido por: " + usuario.getUsername(),
                usuario,
                caja
        );
        movimientoCajaRepository.save(movimiento);

        // ===== 8) Persistencia =====
        // Si en Pedido tienes cascade ALL hacia DocumentoFiscal, con esto basta:
        // return pedidoRepository.save(pedido);

        Pedido pedidoCobrado = pedidoRepository.save(pedido);
        sseService.emitirActualizacionPedido(pedidoCobrado);
        return pedidoCobrado;
    }



    @Transactional
    public Pedido cancelarPedido(Long pedidoId, Long usuarioId){
        if (pedidoId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "pedidoId invalido");
        }
        if(usuarioId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "usuarioId invalido");
        }
        Negocio negocio = tenantService.getNegocioActual();

        Pedido pedido = pedidoRepository.findByIdForUpdate(pedidoId, negocio)
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

        Usuario usuario = usuarioRepository.findByIdAndNegocio(usuarioId, negocio)
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
        Pedido pedidoCancelado = pedidoRepository.save(pedido);
        sseService.emitirActualizacionPedido(pedidoCancelado);
        return pedidoCancelado;
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

    private List<DetallePedido> normalizarDetalles(List<DetallePedidoRequestDTO> detallePedidos) {
        if (detallePedidos == null) {
            return new ArrayList<>();
        }
        List<DetallePedido> resultado = new ArrayList<>();
        for (DetallePedidoRequestDTO detalle : detallePedidos) {
            if (detalle == null || detalle.getProductoId() == null || detalle.getProductoId() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "producto invalido");
            }
            Integer cantidad = detalle.getCantidad();
            if (cantidad == null || cantidad <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "cantidad invalida");
            }
            Producto producto = productoRepository.findByIdAndNegocio(
                            detalle.getProductoId(),
                            tenantService.getNegocioActual()
                    )
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
            DetallePedido detallePedido = new DetallePedido(producto, cantidad);
            detallePedido.reemplazarNotas(
                    notasPredefinidasService.resolverNotasSeleccionadasParaProducto(
                            producto,
                            detalle.getNotaPredefinidaIds()
                    )
            );
            resultado.add(detallePedido);
        }
        return resultado;
    }
}
