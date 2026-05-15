package com.facturacion.negocio.controllers;

import com.facturacion.negocio.domain.Caja;
import com.facturacion.negocio.dto.request.CajaAperturaRequestDTO;
import com.facturacion.negocio.dto.request.CajaCierreRequestDTO;
import com.facturacion.negocio.dto.response.CajaResponseDTO;
import com.facturacion.negocio.dto.response.CajaResumenResponseDTO;
import com.facturacion.negocio.services.CajaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/caja")
public class CajaController {


    private final CajaService cajaService;

    public CajaController(CajaService cajaService) {
        this.cajaService = cajaService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CajaResponseDTO> getCaja(@PathVariable Long id) {
        Caja caja = cajaService.obtenerCaja(id);
        return ResponseEntity.ok().body(CajaResponseDTO.from(caja));
    }

    @GetMapping("/resumen")
    public ResponseEntity<CajaResumenResponseDTO>  getCajaResumen() {
        return ResponseEntity.ok().body(cajaService.obtenerCajaResumen());
    }
    @GetMapping("/abierta")
    public ResponseEntity<CajaResponseDTO> getAllCajas() {
        Caja caja = cajaService.obtenerCajaAbierta();
        return ResponseEntity.ok().body(CajaResponseDTO.from(caja));
    }

    @PreAuthorize("hasAnyRole('CAJERO', 'ADMINISTRADOR')")
    @PostMapping("/abrir")
    public ResponseEntity<CajaResponseDTO> abrirCaja(@RequestBody CajaAperturaRequestDTO cajaAperturaRequestDTO) {
        Caja caja = cajaService.abrirCaja(cajaAperturaRequestDTO.getUsuarioAperturaId());
        URI location = URI.create("/caja/" + caja.getId());

        return ResponseEntity.created(location).body(CajaResponseDTO.from(caja));
    }



    @PreAuthorize("hasAnyRole('CAJERO', 'ADMINISTRADOR')")
    @PostMapping("/cerrar")
    public ResponseEntity<CajaResponseDTO> cerrarCaja(@RequestBody CajaCierreRequestDTO cajaCierreRequestDTO) {
        Caja caja = cajaService.cerrarCaja(
                cajaCierreRequestDTO.getIdUsuarioCierre(),
                cajaCierreRequestDTO.getRetiro());

        return ResponseEntity.ok(CajaResponseDTO.from(caja));
    }


}
