package com.facturacion.negocio.controllers;

import com.facturacion.negocio.domain.MovimientoCaja;
import com.facturacion.negocio.dto.request.RealizarEgresoRequest;
import com.facturacion.negocio.dto.response.MovimientoCajaResponseDTO;
import com.facturacion.negocio.services.MovimientoCajaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/movimientos")
public class MovimientoCajaController {

    private final MovimientoCajaService movimientoCajaService;

    public MovimientoCajaController( MovimientoCajaService movimientoCajaService) {
        this.movimientoCajaService = movimientoCajaService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<MovimientoCajaResponseDTO> obtenerMovimientoCaja(@PathVariable Long id) {
        return ResponseEntity.ok().body(MovimientoCajaResponseDTO.from(movimientoCajaService.obtenerMovimiento(id)));
    }

    @GetMapping("/abierta")
    public ResponseEntity<List<MovimientoCajaResponseDTO>> obtenerByCajaAbierta() {
        return ResponseEntity.ok().body(movimientoCajaService.obtenerMovimientosByCajaAbierta());
    }

    @PostMapping("/egreso")
    public ResponseEntity<MovimientoCajaResponseDTO> realizarEgreso(@Valid @RequestBody RealizarEgresoRequest request){
         MovimientoCaja movimiento = movimientoCajaService.realizarEgreso(request.getMonto(), request.getMotivo(), request.getUsuarioId());
         URI location = URI.create("/movimientos/"+ movimiento.getId());

        return ResponseEntity.created(location).body(MovimientoCajaResponseDTO.from(movimiento));

    }

}
