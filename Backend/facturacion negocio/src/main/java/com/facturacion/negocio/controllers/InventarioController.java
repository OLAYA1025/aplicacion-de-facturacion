package com.facturacion.negocio.controllers;

import com.facturacion.negocio.domain.MovimientoInventario;
import com.facturacion.negocio.dto.response.MovimientoInventarioResponseDTO;
import com.facturacion.negocio.services.InventarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/inventario")
public class InventarioController {

    private final InventarioService inventarioService;

    public InventarioController(InventarioService inventarioService) {
        this.inventarioService = inventarioService;
    }

    @GetMapping("/movimientos/producto/{productoId}")
    public ResponseEntity<List<MovimientoInventarioResponseDTO>> obtenerMovimientosPorProducto(
            @PathVariable Long productoId) {
        List<MovimientoInventario> movimientos = inventarioService.obtenerMovimientosPorProducto(productoId);
        List<MovimientoInventarioResponseDTO> response = new ArrayList<>();
        for (MovimientoInventario movimiento : movimientos) {
            response.add(MovimientoInventarioResponseDTO.from(movimiento));
        }
        return ResponseEntity.ok(response);
    }
}
