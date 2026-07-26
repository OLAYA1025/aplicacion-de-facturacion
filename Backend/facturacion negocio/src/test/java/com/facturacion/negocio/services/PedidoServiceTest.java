package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.Caja;
import com.facturacion.negocio.domain.BaseTenantEntity;
import com.facturacion.negocio.domain.Categoria;
import com.facturacion.negocio.domain.Cliente;
import com.facturacion.negocio.domain.ConfiguracionFacturacionElectronica;
import com.facturacion.negocio.domain.DetallePedido;
import com.facturacion.negocio.domain.DocumentoFiscal;
import com.facturacion.negocio.domain.MovimientoCaja;
import com.facturacion.negocio.domain.MovimientoInventario;
import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.domain.Pedido;
import com.facturacion.negocio.domain.Producto;
import com.facturacion.negocio.domain.Usuario;
import com.facturacion.negocio.dto.request.DetallePedidoRequestDTO;
import com.facturacion.negocio.enumerated.EstadoPedido;
import com.facturacion.negocio.enumerated.EstadoDocumentoFiscal;
import com.facturacion.negocio.enumerated.MetodoPago;
import com.facturacion.negocio.enumerated.ProveedorFacturacion;
import com.facturacion.negocio.enumerated.Rol;
import com.facturacion.negocio.enumerated.TipoDocumento;
import com.facturacion.negocio.enumerated.TipoDocumentoFiscal;
import com.facturacion.negocio.enumerated.TipoGestionProducto;
import com.facturacion.negocio.enumerated.TipoImpuesto;
import com.facturacion.negocio.enumerated.TipoMovimiento;
import com.facturacion.negocio.enumerated.TipoMovimientoInventario;
import com.facturacion.negocio.repository.CajaRepository;
import com.facturacion.negocio.repository.ClienteRepository;
import com.facturacion.negocio.repository.ConfiguracionFacturacionElectronicaRepository;
import com.facturacion.negocio.repository.DocumentoFiscalRepository;
import com.facturacion.negocio.repository.MovimientoCajaRepository;
import com.facturacion.negocio.repository.MovimientoInventarioRepository;
import com.facturacion.negocio.repository.NumberingRangeRepository;
import com.facturacion.negocio.repository.PedidoRepository;
import com.facturacion.negocio.repository.ProductoRepository;
import com.facturacion.negocio.repository.UsuarioRepository;
import com.facturacion.negocio.services.tirillaFactura.PdfService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private MovimientoCajaRepository movimientoCajaRepository;
    @Mock
    private MovimientoInventarioRepository movimientoInventarioRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private CajaRepository cajaRepository;
    @Mock
    private ClienteRepository clienteRepository;
    @Mock
    private DocumentoFiscalRepository documentoFiscalRepository;
    @Mock
    private DocumentoFiscalService documentoFiscalService;
    @Mock
    private NumberingRangeRepository numberingRangeRepository;
    @Mock
    private SseService sseService;
    @Mock
    private NotasPredefinidasService notasPredefinidasService;
    @Mock
    private PdfService pdfService;
    @Mock
    private TenantService tenantService;
    @Mock
    private ConfiguracionFacturacionElectronicaRepository configuracionFacturacionElectronicaRepository;

    private PedidoService pedidoService;
    private Negocio negocio;

    @BeforeEach
    void setUp() {
        negocio = new Negocio("Negocio Test", null, null, null, true);
        negocio.habilitarFacturacion();
        ReflectionTestUtils.setField(negocio, "id", 1L);
        when(tenantService.getNegocioActual()).thenReturn(negocio);
        ConfiguracionFacturacionElectronica config = new ConfiguracionFacturacionElectronica(
                ProveedorFacturacion.FACTUS,
                "http://localhost",
                "tenant",
                "password",
                "client",
                "secret",
                "PED"
        );
        ReflectionTestUtils.setField(config, "id", 1L);
        config.setNegocio(negocio);
        lenient().when(configuracionFacturacionElectronicaRepository.findByNegocioId(1L))
                .thenReturn(Optional.of(config));
        lenient().when(numberingRangeRepository.findFirstByConfiguracionIdAndActiveTrueOrderByIdAsc(1L))
                .thenReturn(Optional.empty());
        lenient().when(tenantService.asignarNegocioActual(any())).thenAnswer(invocation -> {
            BaseTenantEntity entity = invocation.getArgument(0);
            entity.setNegocio(negocio);
            return entity;
        });
        pedidoService = new PedidoService(
                pedidoRepository,
                movimientoCajaRepository,
                movimientoInventarioRepository,
                productoRepository,
                usuarioRepository,
                cajaRepository,
                clienteRepository,
                documentoFiscalRepository,
                documentoFiscalService,
                numberingRangeRepository,
                sseService,
                notasPredefinidasService,
                pdfService,
                tenantService,
                true,
                configuracionFacturacionElectronicaRepository
        );
    }

    @Nested
    class CrearPedido {

        @Test
        void debeRetornarConflictCuandoNoHayCajaAbierta() {
            when(cajaRepository.obtenerCajaAbierta(negocio)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> pedidoService.crearPedido(List.of(), "Mesa 1"))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException response = (ResponseStatusException) ex;
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                        assertThat(response.getReason()).isEqualTo("No puede crear un pedido en una caja cerrada");
                    });
        }

        @Test
        void debeCrearPedidoReservarInventarioYEmitirEvento() {
            Caja caja = cajaAbierta(10L);
            Producto producto = productoInventariable(100L, 15, new BigDecimal("12000.00"));

            when(cajaRepository.obtenerCajaAbierta(negocio)).thenReturn(Optional.of(caja));
            when(productoRepository.findByIdAndNegocio(100L, negocio)).thenReturn(Optional.of(producto));
            when(notasPredefinidasService.resolverNotasSeleccionadasParaProducto(eq(producto), any()))
                    .thenReturn(List.of());
            when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Pedido pedido = pedidoService.crearPedido(List.of(detalleRequest(100L, 3)), "Mesa 5");

            assertThat(pedido.getEstado()).isEqualTo(EstadoPedido.PENDIENTE);
            assertThat(pedido.getDetallePedidos()).hasSize(1);
            assertThat(producto.getStockReservado()).isEqualTo(3);
            verify(pedidoRepository).save(any(Pedido.class));
            verify(sseService).emitirActualizacionPedido(any(Pedido.class), anyList());
        }

        @Test
        void debeAsignarNombrePorDefectoConIdCuandoNombreEsVacio() {
            Caja caja = cajaAbierta(11L);
            Producto producto = productoInventariable(101L, 10, new BigDecimal("10000.00"));

            when(cajaRepository.obtenerCajaAbierta(negocio)).thenReturn(Optional.of(caja));
            when(productoRepository.findByIdAndNegocio(101L, negocio)).thenReturn(Optional.of(producto));
            when(notasPredefinidasService.resolverNotasSeleccionadasParaProducto(eq(producto), any()))
                    .thenReturn(List.of());
            when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> {
                Pedido pedido = invocation.getArgument(0);
                if (pedido.getId() == null) {
                    ReflectionTestUtils.setField(pedido, "id", 77L);
                }
                return pedido;
            });

            Pedido pedido = pedidoService.crearPedido(List.of(detalleRequest(101L, 1)), "   ");

            assertThat(pedido.getNombre()).isEqualTo("Pedido #77");
            verify(pedidoRepository, times(2)).save(any(Pedido.class));
        }
    }

    @Nested
    class CobrarPedido {

        @Test
        void debeCobrarPedidoConsumirInventarioYGenerarMovimientos() {
            Caja caja = cajaAbierta(20L);
            Usuario cajero = usuarioActivo(2L, "cajero");
            Pedido pedido = pedidoPendiente(99L, caja, productoInventariable(200L, 10, new BigDecimal("5000.00")), 3);
            pedido.getDetallePedidos().forEach(detalle -> detalle.getProducto().reservarStock(detalle.getCantidad()));

            when(pedidoRepository.findByIdForUpdate(99L, negocio)).thenReturn(Optional.of(pedido));
            when(usuarioRepository.findByIdAndNegocio(2L, negocio)).thenReturn(Optional.of(cajero));
            when(cajaRepository.findByIdForUpdate(20L, negocio)).thenReturn(Optional.of(caja));
            when(clienteRepository.findByTipoIdentificacionAndNumeroIdentificacionAndNegocio(TipoDocumento.CC, "222222222222", negocio))
                    .thenReturn(Optional.empty());
            when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(movimientoCajaRepository.save(any(MovimientoCaja.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(movimientoInventarioRepository.save(any(MovimientoInventario.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            Pedido pedidoCobrado = pedidoService.cobrarPedido(
                    99L,
                    2L,
                    MetodoPago.EFECTIVO,
                    null,
                    null,
                    null
            );

            assertThat(pedidoCobrado.getEstado()).isEqualTo(EstadoPedido.PAGO);
            assertThat(pedidoCobrado.getMetodoPago()).isEqualTo(MetodoPago.EFECTIVO);
            assertThat(pedidoCobrado.getDocumentoFiscal()).isNotNull();
            assertThat(pedidoCobrado.getDocumentoFiscal().getEstado()).isEqualTo(EstadoDocumentoFiscal.PENDIENTE_ENVIO);
            assertThat(pedidoCobrado.getDocumentoFiscal().getCliente().getNumeroIdentificacion()).isEqualTo("222222222222");

            Producto producto = pedidoCobrado.getDetallePedidos().get(0).getProducto();
            assertThat(producto.getStockActual()).isEqualTo(7);
            assertThat(producto.getStockReservado()).isZero();

            ArgumentCaptor<MovimientoInventario> inventarioCaptor = ArgumentCaptor.forClass(MovimientoInventario.class);
            verify(movimientoInventarioRepository).save(inventarioCaptor.capture());
            assertThat(inventarioCaptor.getValue().getTipo()).isEqualTo(TipoMovimientoInventario.VENTA);
            assertThat(inventarioCaptor.getValue().getCantidad()).isEqualTo(-3);

            ArgumentCaptor<MovimientoCaja> cajaCaptor = ArgumentCaptor.forClass(MovimientoCaja.class);
            verify(movimientoCajaRepository).save(cajaCaptor.capture());
            assertThat(cajaCaptor.getValue().getTipo()).isEqualTo(TipoMovimiento.INGRESO);
            assertThat(cajaCaptor.getValue().getMonto()).isEqualByComparingTo("15000.00");

            verify(sseService).emitirActualizacionPedido(any(Pedido.class));
        }

        @Test
        void debeRetornarBadRequestCuandoFacturaElectronicaNoRecibeCliente() {
            Caja caja = cajaAbierta(21L);
            Usuario cajero = usuarioActivo(2L, "cajero");
            Pedido pedido = pedidoPendiente(100L, caja, productoInventariable(201L, 5, new BigDecimal("7000.00")), 1);

            when(pedidoRepository.findByIdForUpdate(100L, negocio)).thenReturn(Optional.of(pedido));
            when(usuarioRepository.findByIdAndNegocio(2L, negocio)).thenReturn(Optional.of(cajero));
            when(cajaRepository.findByIdForUpdate(21L, negocio)).thenReturn(Optional.of(caja));

            assertThatThrownBy(() -> pedidoService.cobrarPedido(
                    100L,
                    2L,
                    MetodoPago.TRANSFERENCIA,
                    TipoDocumentoFiscal.FEV,
                    null,
                    null
            ))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException response = (ResponseStatusException) ex;
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(response.getReason())
                                .isEqualTo("debes enviar un cliente para crear una factura electronica");
                    });

            verify(movimientoCajaRepository, never()).save(any(MovimientoCaja.class));
            verify(pedidoRepository, never()).save(any(Pedido.class));
        }

        @Test
        void debeRetornarConflictSiPedidoYaTieneDocumentoFiscal() {
            Caja caja = cajaAbierta(22L);
            Pedido pedido = pedidoPendiente(101L, caja, productoInventariable(202L, 5, new BigDecimal("6000.00")), 1);
            pedido.setDocumentoFiscal(DocumentoFiscal.crearBorrador(pedido, TipoDocumentoFiscal.FEV_CONS_FINAL, "PED"));

            when(pedidoRepository.findByIdForUpdate(101L, negocio)).thenReturn(Optional.of(pedido));

            assertThatThrownBy(() -> pedidoService.cobrarPedido(
                    101L,
                    2L,
                    MetodoPago.EFECTIVO,
                    TipoDocumentoFiscal.FEV_CONS_FINAL,
                    null,
                    null
            ))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException response = (ResponseStatusException) ex;
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                        assertThat(response.getReason()).isEqualTo("El pedido ya tiene documento fiscal asociado");
                    });
        }
    }

    @Nested
    class CancelarPedido {

        @Test
        void debeCancelarPedidoYLiberarStockReservado() {
            Caja caja = cajaAbierta(30L);
            Producto producto = productoInventariable(300L, 8, new BigDecimal("9000.00"));
            Pedido pedido = pedidoPendiente(200L, caja, producto, 2);
            producto.reservarStock(2);
            Usuario usuario = usuarioActivo(5L, "admin");

            when(pedidoRepository.findByIdForUpdate(200L, negocio)).thenReturn(Optional.of(pedido));
            when(usuarioRepository.findByIdAndNegocio(5L, negocio)).thenReturn(Optional.of(usuario));
            when(pedidoRepository.save(any(Pedido.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Pedido pedidoCancelado = pedidoService.cancelarPedido(200L, 5L);

            assertThat(pedidoCancelado.getEstado()).isEqualTo(EstadoPedido.CANCELADO);
            assertThat(producto.getStockReservado()).isZero();
            verify(sseService).emitirActualizacionPedido(any(Pedido.class));
        }
    }

    private Pedido pedidoPendiente(Long id, Caja caja, Producto producto, int cantidad) {
        Pedido pedido = new Pedido("Pedido " + id, List.of(new DetallePedido(producto, cantidad)), caja);
        ReflectionTestUtils.setField(pedido, "id", id);
        return pedido;
    }

    private DetallePedidoRequestDTO detalleRequest(Long productoId, Integer cantidad) {
        DetallePedidoRequestDTO detalle = new DetallePedidoRequestDTO();
        detalle.setProductoId(productoId);
        detalle.setCantidad(cantidad);
        return detalle;
    }

    private Producto productoInventariable(Long id, int stockInicial, BigDecimal precio) {
        Producto producto = new Producto(
                "Producto " + id,
                precio,
                BigDecimal.ZERO,
                "Producto para test",
                categoria(id),
                TipoGestionProducto.INVENTARIABLE,
                TipoImpuesto.IVA,
                BigDecimal.valueOf(19),
                stockInicial,
                1
        );
        producto.setId(id);
        return producto;
    }

    private Caja cajaAbierta(Long id) {
        Usuario usuarioApertura = usuarioActivo(1L, "apertura");
        Caja caja = new Caja(new BigDecimal("100000.00"), usuarioApertura);
        ReflectionTestUtils.setField(caja, "id", id);
        return caja;
    }

    private Usuario usuarioActivo(Long id, String username) {
        Usuario usuario = new Usuario("Usuario " + username, username, "hash", username + "@mail.test", Rol.CAJERO);
        usuario.setNegocio(negocio);
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    private Categoria categoria(Long id) {
        Categoria categoria = new Categoria("Categoria " + id);
        categoria.setNegocio(negocio);
        return categoria;
    }
}
