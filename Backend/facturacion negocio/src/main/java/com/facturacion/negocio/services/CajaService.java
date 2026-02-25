package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.Caja;
import com.facturacion.negocio.domain.MovimientoCaja;
import com.facturacion.negocio.domain.Pedido;
import com.facturacion.negocio.domain.Usuario;
import com.facturacion.negocio.dto.response.CajaResumenResponseDTO;
import com.facturacion.negocio.enumerated.EstadoPedido;
import com.facturacion.negocio.enumerated.MetodoPago;
import com.facturacion.negocio.enumerated.TipoMovimiento;
import com.facturacion.negocio.repository.*;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class CajaService {

    private final CajaRepository cajaRepository;
    private final MovimientoCajaRepository movimientoCajaRepository;
    private final UsuarioRepository usuarioRepository;
    private final PedidoRepository pedidoRepository;

    public CajaService(CajaRepository cajaRepository,
                       MovimientoCajaRepository movimientoCajaRepository,
                       UsuarioRepository usuarioRepository,
                       PedidoRepository pedidoRepository) {
        this.cajaRepository = cajaRepository;
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.usuarioRepository = usuarioRepository;
        this.pedidoRepository = pedidoRepository;
    }

    public Caja obtenerCaja(Long id) {
        return cajaRepository.findById(id)
                .orElseThrow(() -> new
                        ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                        "Caja no encontrada")
                );
    }

    public CajaResumenResponseDTO  obtenerCajaResumen() {
        Caja cajaAbierta = obtenerCajaAbierta();
        BigDecimal montoInicial = valorSeguro(cajaAbierta.getMontoInicial());
        List<Pedido> pedidosCaja = pedidoRepository.findPedidosByCaja(cajaAbierta.getId());

        BigDecimal ingresosEfectivo = calcularIngresosPorMetodo(pedidosCaja, MetodoPago.EFECTIVO);
        BigDecimal ingresosTransferencia = calcularIngresosPorMetodo(pedidosCaja, MetodoPago.TRANSFERENCIA);
        BigDecimal ingresosTarjeta = calcularIngresosPorMetodo(pedidosCaja, MetodoPago.TARJETA);
        BigDecimal ingresos = ingresosEfectivo.add(ingresosTransferencia).add(ingresosTarjeta);

        BigDecimal egresos = valorSeguro(movimientoCajaRepository.calcularTotalesByTipoAndCaja(TipoMovimiento.EGRESO,cajaAbierta));
        BigDecimal retiros = valorSeguro(movimientoCajaRepository.calcularTotalesByTipoAndCaja(TipoMovimiento.RETIRO,cajaAbierta));

        return new CajaResumenResponseDTO(
                montoInicial,
                ingresos,
                ingresosEfectivo,
                ingresosTransferencia,
                ingresosTarjeta,
                egresos,
                retiros
        );

    }

    public Caja obtenerCajaAbierta(){
        return cajaRepository.obtenerCajaAbierta()
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "no hay cajas abiertas")
                );
    }



    @Transactional
    public Caja abrirCaja(Long idUsuario) {
        if(idUsuario == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "idUsuario invalido"
            );
        }
        Usuario usuario = usuarioRepository.findById(idUsuario)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario no encontrado")
                );

        if (!usuario.isActivo()){
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario debe estar activo para abrir la caja"
            );
        }

        if (cajaRepository.existeCajaAbierta()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya hay una caja abierta"
            );
        }

        Optional<Caja> ultimaCajaOpt = cajaRepository.obtenerUltimaCajaCerrada();

        BigDecimal montoInicial;

        if (ultimaCajaOpt.isPresent()) {
            Caja cajaAnterior = ultimaCajaOpt.get();
            montoInicial = cajaAnterior.getMontoFinal();
        } else {
            montoInicial = BigDecimal.ZERO;
        }

        Caja nuevaCaja = new Caja(montoInicial, usuario);

        return cajaRepository.save(nuevaCaja);
    }

    @Transactional
    public Caja cerrarCaja(Long idUsuarioCierre, BigDecimal retiro) {

        if (idUsuarioCierre == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "idUsuarioCierre invalido"
            );
        }

        if (retiro == null || retiro.compareTo(BigDecimal.ZERO) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "retiro invalido"
            );
        }

        Caja caja = obtenerCajaAbierta();

        Usuario usuarioCierre = usuarioRepository.findById(idUsuarioCierre)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Usuario de cierre no encontrado")
                );

        List<Pedido> pedidos = pedidoRepository.findPedidosByCaja(caja.getId());
        for(Pedido pedido : pedidos) {
            if(pedido.isPendiente()){
                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "no se puede cerrar caja con pedidos pendientes");
            }
        }
        if (!usuarioCierre.isActivo()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "El usuario de cierre debe estar activo"
            );
        }

        if (!caja.isAbierta()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "La caja ya esta cerrada"
            );
        }

        // saldo de movimientos (sin monto inicial)
        BigDecimal saldoMovimientos = cajaRepository.calcularSaldoPorCaja(caja.getId());
        if (saldoMovimientos == null) {
            saldoMovimientos = BigDecimal.ZERO;
        }
        BigDecimal montoInicial = caja.getMontoInicial();
        if (montoInicial == null) {
            montoInicial = BigDecimal.ZERO;
        }

        // saldo total antes del retiro
        BigDecimal saldoActual = montoInicial.add(saldoMovimientos);

        // utilidad real del turno (solo movimientos)
        BigDecimal utilidad = saldoActual.subtract(montoInicial);

        // validar que el retiro no supere el saldo
        if (saldoActual.compareTo(retiro) < 0) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Saldo insuficiente para realizar el retiro"
            );
        }

        // registrar el retiro
        if (retiro.compareTo(BigDecimal.ZERO) > 0) {
            MovimientoCaja movimiento = new MovimientoCaja(
                    retiro,
                    TipoMovimiento.RETIRO,
                    "Retiro al cerrar caja #" + caja.getId(),
                    usuarioCierre,
                    caja
            );
            movimientoCajaRepository.save(movimiento);
        }

        //monto final después del retiro
        BigDecimal montoFinal = saldoActual.subtract(retiro);

        // cerrar caja
        caja.cerrarCaja(usuarioCierre, utilidad, montoFinal);

        return cajaRepository.save(caja);
    }




    private BigDecimal calcularIngresosPorMetodo(List<Pedido> pedidos, MetodoPago metodoPago) {
        BigDecimal total = BigDecimal.ZERO;
        for (Pedido pedido : pedidos) {
            if (pedido.getEstado() == EstadoPedido.PAGO && pedido.getMetodoPago() == metodoPago) {
                total = total.add(valorSeguro(pedido.calcularTotal()));
            }
        }
        return total;
    }

    private BigDecimal valorSeguro(BigDecimal valor) {
        return valor == null ? BigDecimal.ZERO : valor;
    }

}
