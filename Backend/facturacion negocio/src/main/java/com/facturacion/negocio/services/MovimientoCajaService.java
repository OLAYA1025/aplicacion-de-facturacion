package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.Caja;
import com.facturacion.negocio.domain.MovimientoCaja;
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

    public MovimientoCajaService(MovimientoCajaRepository movimientoCajaRepository,
                                 CajaRepository cajaRepository, UsuarioRepository usuarioRepository) {
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.cajaRepository = cajaRepository;
        this.usuarioRepository = usuarioRepository;
    }


    public MovimientoCaja obtenerMovimiento(Long id) {
        if(id == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id invalido");
        }
        MovimientoCaja movimientoCaja = this.movimientoCajaRepository.findById(id)
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
        Caja caja = cajaRepository.obtenerCajaAbierta().orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "No hay cajas abiertas")
        );
        return movimientoCajaRepository.findResumenByCaja(caja);
    }

    public List<MovimientoCaja> obtenerMovimientosByCaja(Long cajaId) {
        Caja caja = cajaRepository.findById(cajaId).orElseThrow(() ->
                new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "caja no encontrada")
        );
        return movimientoCajaRepository.findByCaja(caja);

    }

    public BigDecimal calcularTotalIngresos(){
        BigDecimal totalIngresos = BigDecimal.ZERO;
        List<MovimientoCaja> movimientoCajas = movimientoCajaRepository
                .findByTipoMovimiento(TipoMovimiento.INGRESO);
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

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Usuario no encontrado")
                );

        Caja caja = cajaRepository.obtenerCajaAbierta().orElseThrow(()-> new ResponseStatusException(
                HttpStatus.CONFLICT,
                "La caja debe estar abierta para realizar un egreso"));

        BigDecimal saldo = cajaRepository.calcularSaldoPorCaja(caja.getId()).add(caja.getMontoInicial());
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
