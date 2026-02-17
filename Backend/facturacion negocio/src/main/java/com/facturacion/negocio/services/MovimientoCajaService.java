package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.Caja;
import com.facturacion.negocio.domain.MovimientoCaja;
import com.facturacion.negocio.dto.response.MovimientoCajaResponseDTO;
import com.facturacion.negocio.enumerated.TipoMovimiento;
import com.facturacion.negocio.repository.CajaRepository;
import com.facturacion.negocio.repository.MovimientoCajaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class MovimientoCajaService{


    private final MovimientoCajaRepository movimientoCajaRepository;
    private final CajaRepository cajaRepository;

    public MovimientoCajaService(MovimientoCajaRepository movimientoCajaRepository,
                                 CajaRepository cajaRepository) {
        this.movimientoCajaRepository = movimientoCajaRepository;
        this.cajaRepository = cajaRepository;
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





}
