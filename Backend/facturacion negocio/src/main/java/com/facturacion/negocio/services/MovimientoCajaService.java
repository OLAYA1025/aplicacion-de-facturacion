package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.Caja;
import com.facturacion.negocio.domain.MovimientoCaja;
import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.domain.Usuario;
import com.facturacion.negocio.dto.response.MovimientoCajaResponseDTO;
import com.facturacion.negocio.enumerated.TipoMovimiento;
import com.facturacion.negocio.repository.CajaRepository;
import com.facturacion.negocio.repository.MovimientoCajaRepository;
import com.facturacion.negocio.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class MovimientoCajaService{


    private final MovimientoCajaRepository movimientoCajaRepository;
    private final CajaRepository cajaRepository;
    private final UsuarioRepository usuarioRepository;
    private final TenantService tenantService;

    public MovimientoCajaService(MovimientoCajaRepository movimientoCajaRepository,
                                 CajaRepository cajaRepository,
                                 UsuarioRepository usuarioRepository,
                                 TenantService tenantService) {
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.cajaRepository = cajaRepository;
        this.usuarioRepository = usuarioRepository;
        this.tenantService = tenantService;
    }


    public MovimientoCaja obtenerMovimiento(Long id) {
        if(id == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id invalido");
        }
        MovimientoCaja movimientoCaja = this.movimientoCajaRepository.findByIdAndNegocio(id, tenantService.getNegocioActual())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Movimiento caja no encontrada")
                );
        return movimientoCaja;
    }


    public MovimientoCaja addProduct(MovimientoCaja movimientoCaja) {

        if (movimientoCaja.getId() != null) {
            throw new IllegalArgumentException("no puedes agregar un movimiento de caja con un id exixtente");
        }

        ;
        return movimientoCajaRepository.save(movimientoCaja);
    }


    public List<MovimientoCajaResponseDTO> obtenerMovimientosByCajaAbierta(){
        Negocio negocio = tenantService.getNegocioActual();
        Caja caja = cajaRepository.obtenerCajaAbierta(negocio).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No hay cajas abiertas")
        );
        return movimientoCajaRepository.findResumenByCaja(caja, negocio);
    }

    public List<MovimientoCaja> obtenerMovimientosByCaja(Long cajaId) {
        Negocio negocio = tenantService.getNegocioActual();
        Caja caja = cajaRepository.findByIdAndNegocio(cajaId, negocio).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "caja no encontrada")
        );
        return movimientoCajaRepository.findByCaja(caja, negocio);

    }

    public BigDecimal calcularTotalIngresos(){
        BigDecimal totalIngresos = BigDecimal.ZERO;
        Negocio negocio = tenantService.getNegocioActual();
        List<MovimientoCaja> movimientoCajas = movimientoCajaRepository
                .findByTipoMovimiento(TipoMovimiento.INGRESO, negocio);
        for (MovimientoCaja movimientoCaja : movimientoCajas) {
            totalIngresos = totalIngresos.add(movimientoCaja.getMonto());
        }
        return totalIngresos;

    }

    public MovimientoCaja realizarEgreso(BigDecimal monto, String motivo, Long usuarioId){
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Monto invalido");
        }

        if (motivo == null || motivo.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Motivo invalido");
        }

        if (usuarioId == null || usuarioId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "usuarioId invalido");
        }

        Negocio negocio = tenantService.getNegocioActual();
        Usuario usuario = usuarioRepository.findByIdAndNegocio(usuarioId, negocio)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Usuario no encontrado")
                );

        Caja caja = cajaRepository.obtenerCajaAbierta(negocio).orElseThrow(()-> new ResponseStatusException(
                HttpStatus.CONFLICT,
                "La caja debe estar abierta para realizar un egreso"));

        BigDecimal saldo = cajaRepository.calcularSaldoPorCaja(caja.getId(), negocio).add(caja.getMontoInicial());
        if (saldo == null) {
            saldo = BigDecimal.ZERO;
        }

        if(saldo.compareTo(monto)<0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Saldo insuficiente");
        }

        MovimientoCaja movimiento = new MovimientoCaja(monto, TipoMovimiento.EGRESO, motivo.trim(), usuario, caja );
        return movimientoCajaRepository.save(movimiento);
    }





}
