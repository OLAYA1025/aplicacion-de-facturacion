package com.facturacion.negocio.controllers;

import com.facturacion.negocio.dto.response.MovimientoCajaResponseDTO;
import com.facturacion.negocio.services.MovimientoCajaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/movimientos")
public class MovimientoCajaController {

    private final MovimientoCajaService movimientoCajaService;

    public MovimientoCajaController( MovimientoCajaService movimientoCajaService) {
        this.movimientoCajaService = movimientoCajaService;
    }

    @GetMapping("/abierta")
    public ResponseEntity<List<MovimientoCajaResponseDTO>> obtenerByCajaAbierta() {
        return ResponseEntity.ok().body(movimientoCajaService.obtenerMovimientosByCajaAbierta());
    }



}
