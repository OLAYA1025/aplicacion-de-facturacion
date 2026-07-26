package com.facturacion.negocio.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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

import com.facturacion.negocio.domain.Caja;
import com.facturacion.negocio.domain.Categoria;
import com.facturacion.negocio.domain.DetallePedido;
import com.facturacion.negocio.domain.MovimientoCaja;
import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.domain.Pedido;
import com.facturacion.negocio.domain.Producto;
import com.facturacion.negocio.domain.Usuario;
import com.facturacion.negocio.dto.response.CajaResumenResponseDTO;
import com.facturacion.negocio.enumerated.EstadoCaja;
import com.facturacion.negocio.enumerated.MetodoPago;
import com.facturacion.negocio.enumerated.Rol;
import com.facturacion.negocio.enumerated.TipoMovimiento;
import com.facturacion.negocio.repository.CajaRepository;
import com.facturacion.negocio.repository.MovimientoCajaRepository;
import com.facturacion.negocio.repository.PedidoRepository;
import com.facturacion.negocio.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class CajaServiceTest {

    @Mock
    private CajaRepository cajaRepository;
    @Mock
    private MovimientoCajaRepository movimientoCajaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PedidoRepository pedidoRepository;
    @Mock
    private TenantService tenantService;

    private CajaService cajaService;
    private Negocio negocio;

    @BeforeEach
    void setUp() {
        negocio = new Negocio("Negocio Test", null, null, null, true);
        ReflectionTestUtils.setField(negocio, "id", 1L);
        when(tenantService.getNegocioActual()).thenReturn(negocio);
        cajaService = new CajaService(
                cajaRepository,
                movimientoCajaRepository,
                usuarioRepository,
                pedidoRepository,
                tenantService
        );
    }

    @Nested
    class AbrirCaja {

        @Test
        void debeUsarMontoFinalDeUltimaCajaCerradaComoMontoInicial() {
            Usuario usuario = usuarioActivo(1L, "admin");
            Caja ultimaCajaCerrada = new Caja(BigDecimal.ZERO, usuario);
            ultimaCajaCerrada.setEstado(EstadoCaja.CERRADA);
            ultimaCajaCerrada.setMontoFinal(new BigDecimal("350000.00"));

            when(usuarioRepository.findByIdAndNegocio(1L, negocio)).thenReturn(Optional.of(usuario));
            when(cajaRepository.existeCajaAbierta(negocio)).thenReturn(false);
            when(cajaRepository.obtenerUltimaCajaCerrada(negocio)).thenReturn(Optional.of(ultimaCajaCerrada));
            when(cajaRepository.save(any(Caja.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Caja cajaAbierta = cajaService.abrirCaja(1L);

            assertThat(cajaAbierta.getMontoInicial()).isEqualByComparingTo("350000.00");
            assertThat(cajaAbierta.isAbierta()).isTrue();
        }

        @Test
        void debeRetornarConflictCuandoYaExisteCajaAbierta() {
            Usuario usuario = usuarioActivo(1L, "admin");

            when(usuarioRepository.findByIdAndNegocio(1L, negocio)).thenReturn(Optional.of(usuario));
            when(cajaRepository.existeCajaAbierta(negocio)).thenReturn(true);

            assertThatThrownBy(() -> cajaService.abrirCaja(1L))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException response = (ResponseStatusException) ex;
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                        assertThat(response.getReason()).isEqualTo("Ya hay una caja abierta");
                    });
        }
    }

    @Nested
    class CerrarCaja {

        @Test
        void debeRetornarConflictSiHayPedidosPendientes() {
            Caja cajaAbierta = cajaAbierta(10L, new BigDecimal("100000.00"));
            Usuario usuarioCierre = usuarioActivo(2L, "cierre");
            Pedido pedidoPendiente = pedidoPendiente(cajaAbierta);

            when(cajaRepository.obtenerCajaAbierta(negocio)).thenReturn(Optional.of(cajaAbierta));
            when(usuarioRepository.findByIdAndNegocio(2L, negocio)).thenReturn(Optional.of(usuarioCierre));
            when(pedidoRepository.findPedidosByCaja(10L, negocio)).thenReturn(List.of(pedidoPendiente));

            assertThatThrownBy(() -> cajaService.cerrarCaja(2L, BigDecimal.ZERO))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException response = (ResponseStatusException) ex;
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                        assertThat(response.getReason()).isEqualTo("no se puede cerrar caja con pedidos pendientes");
                    });
        }

        @Test
        void debeCerrarCajaYRegistrarRetiroCuandoEsValido() {
            Caja cajaAbierta = cajaAbierta(10L, new BigDecimal("100000.00"));
            Usuario usuarioCierre = usuarioActivo(2L, "cierre");
            Pedido pedidoEfectivo = pedidoPagado(
                    cajaAbierta,
                new BigDecimal("50000.00"),
                    MetodoPago.EFECTIVO,
                    usuarioActivo(3L, "cajero")
            );
            Pedido pedidoTransferencia = pedidoPagado(
                cajaAbierta,
                new BigDecimal("40000.00"),
                MetodoPago.TRANSFERENCIA,
                usuarioActivo(3L, "cajero")
            );

            when(cajaRepository.obtenerCajaAbierta(negocio)).thenReturn(Optional.of(cajaAbierta));
            when(usuarioRepository.findByIdAndNegocio(2L, negocio)).thenReturn(Optional.of(usuarioCierre));
            when(pedidoRepository.findPedidosByCaja(10L, negocio)).thenReturn(List.of(pedidoEfectivo, pedidoTransferencia));
            when(movimientoCajaRepository.save(any(MovimientoCaja.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(cajaRepository.save(any(Caja.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Caja cajaCerrada = cajaService.cerrarCaja(2L, new BigDecimal("20000.00"));

            assertThat(cajaCerrada.isAbierta()).isFalse();
            assertThat(cajaCerrada.getMontoFinal()).isEqualByComparingTo("130000.00");
            assertThat(cajaCerrada.getUtilidad()).isEqualByComparingTo("90000.00");
            assertThat(cajaCerrada.getUsuarioCierre()).isEqualTo(usuarioCierre);

            ArgumentCaptor<MovimientoCaja> movimientoCaptor = ArgumentCaptor.forClass(MovimientoCaja.class);
            verify(movimientoCajaRepository).save(movimientoCaptor.capture());
            assertThat(movimientoCaptor.getValue().getTipo()).isEqualTo(TipoMovimiento.RETIRO);
            assertThat(movimientoCaptor.getValue().getMonto()).isEqualByComparingTo("20000.00");
        }

        @Test
        void debeRechazarRetiroCuandoSuperaElEfectivoDisponible() {
            Caja cajaAbierta = cajaAbierta(10L, BigDecimal.ZERO);
            Usuario usuarioCierre = usuarioActivo(2L, "cierre");
            Pedido pedidoEfectivo = pedidoPagado(
                cajaAbierta,
                new BigDecimal("50000.00"),
                MetodoPago.EFECTIVO,
                usuarioActivo(3L, "cajero")
            );
            Pedido pedidoTransferencia = pedidoPagado(
                cajaAbierta,
                new BigDecimal("50000.00"),
                MetodoPago.TRANSFERENCIA,
                usuarioActivo(3L, "cajero")
            );

            when(cajaRepository.obtenerCajaAbierta(negocio)).thenReturn(Optional.of(cajaAbierta));
            when(usuarioRepository.findByIdAndNegocio(2L, negocio)).thenReturn(Optional.of(usuarioCierre));
            when(pedidoRepository.findPedidosByCaja(10L, negocio)).thenReturn(List.of(pedidoEfectivo, pedidoTransferencia));

            assertThatThrownBy(() -> cajaService.cerrarCaja(2L, new BigDecimal("60000.00")))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                ResponseStatusException response = (ResponseStatusException) ex;
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
                assertThat(response.getReason()).isEqualTo("Saldo insuficiente para realizar el retiro");
                });
        }
    }

    @Nested
    class ObtenerCajaResumen {

        @Test
        void debeCalcularIngresosPorMetodoYEstructuraDelResumen() {
            Caja cajaAbierta = cajaAbierta(25L, new BigDecimal("100000.00"));
            Usuario cajero = usuarioActivo(9L, "cajero");

            Pedido efectivo = pedidoPagado(cajaAbierta, new BigDecimal("30000.00"), MetodoPago.EFECTIVO, cajero);
            Pedido transferencia = pedidoPagado(cajaAbierta, new BigDecimal("40000.00"), MetodoPago.TRANSFERENCIA, cajero);
            Pedido credito = pedidoPagado(cajaAbierta, new BigDecimal("50000.00"), MetodoPago.TARJETA_CREDITO, cajero);
            Pedido debito = pedidoPagado(cajaAbierta, new BigDecimal("20000.00"), MetodoPago.TARJETA_DEBITO, cajero);
            Pedido pendiente = pedidoPendiente(cajaAbierta);

            when(cajaRepository.obtenerCajaAbierta(negocio)).thenReturn(Optional.of(cajaAbierta));
            when(pedidoRepository.findPedidosByCaja(25L, negocio))
                    .thenReturn(List.of(efectivo, transferencia, credito, debito, pendiente));
            when(movimientoCajaRepository.calcularTotalesByTipoAndCaja(TipoMovimiento.EGRESO, cajaAbierta, negocio))
                    .thenReturn(new BigDecimal("15000.00"));
            when(movimientoCajaRepository.calcularTotalesByTipoAndCaja(TipoMovimiento.RETIRO, cajaAbierta, negocio))
                    .thenReturn(new BigDecimal("5000.00"));

            CajaResumenResponseDTO resumen = cajaService.obtenerCajaResumen();

            assertThat(resumen.getMontoInicial()).isEqualByComparingTo("100000.00");
            assertThat(resumen.getIngresos()).isEqualByComparingTo("140000.00");
            assertThat(resumen.getIngresosEfectivo()).isEqualByComparingTo("30000.00");
            assertThat(resumen.getIngresosTransferencia()).isEqualByComparingTo("40000.00");
            assertThat(resumen.getIngresosTarjeta()).isEqualByComparingTo("70000.00");
            assertThat(resumen.getEgresos()).isEqualByComparingTo("15000.00");
            assertThat(resumen.getRetiros()).isEqualByComparingTo("5000.00");
            assertThat(resumen.getSaldo()).isEqualByComparingTo("110000.00");
        }
    }

    private Pedido pedidoPendiente(Caja caja) {
        Producto producto = new Producto("Producto Pendiente", new BigDecimal("10000.00"),BigDecimal.ZERO, "test", categoria());
        return new Pedido("Pedido pendiente", List.of(new DetallePedido(producto, 1)), caja);
    }

    private Pedido pedidoPagado(Caja caja, BigDecimal precio, MetodoPago metodoPago, Usuario usuario) {
        Producto producto = new Producto("Producto " + precio, precio,BigDecimal.ZERO, "test", categoria());
        Pedido pedido = new Pedido("Pedido pagado", List.of(new DetallePedido(producto, 1)), caja);
        pedido.pagar(usuario, metodoPago);
        return pedido;
    }

    private Caja cajaAbierta(Long id, BigDecimal montoInicial) {
        Usuario usuarioApertura = usuarioActivo(1L, "apertura");
        Caja caja = new Caja(montoInicial, usuarioApertura);
        ReflectionTestUtils.setField(caja, "id", id);
        return caja;
    }

    private Usuario usuarioActivo(Long id, String username) {
        Usuario usuario = new Usuario("Usuario " + username, username, "hash", username + "@mail.test", Rol.ADMINISTRADOR);
        usuario.setNegocio(negocio);
        ReflectionTestUtils.setField(usuario, "id", id);
        return usuario;
    }

    private Categoria categoria() {
        Categoria categoria = new Categoria("Cat");
        categoria.setNegocio(negocio);
        return categoria;
    }
}
