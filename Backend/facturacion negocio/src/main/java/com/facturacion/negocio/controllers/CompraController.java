package com.facturacion.negocio.controllers;

import com.facturacion.negocio.domain.Compra;
import com.facturacion.negocio.dto.request.CrearCompraRequestDTO;
import com.facturacion.negocio.dto.response.CompraResponseDTO;
import com.facturacion.negocio.dto.response.CompraResumenResponseDTO;
import com.facturacion.negocio.enumerated.EstadoCompra;
import com.facturacion.negocio.services.CompraService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.time.LocalDate;
@RestController
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequestMapping("/compra")
public class CompraController {

    private final CompraService compraService;

    public CompraController(CompraService compraService) {
        this.compraService = compraService;
    }

    @PostMapping
    public ResponseEntity<CompraResponseDTO> crearCompra(@RequestBody CrearCompraRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "body invalido");
        }
        Compra compra = compraService.crearCompra(
                request.getProveedorId(),
                request.getNumeroFactura(),
                request.getObservacion(),
                request.getDetalles()
        );
        Compra compraCreada = compraService.obtenerCompra(compra.getId());
        URI location = URI.create("/compra/" + compra.getId());
        return ResponseEntity.created(location).body(CompraResponseDTO.from(compraCreada));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompraResponseDTO> obtenerCompra(@PathVariable Long id) {
        Compra compra = compraService.obtenerCompra(id);
        return ResponseEntity.ok(CompraResponseDTO.from(compra));
    }

    @GetMapping
    public ResponseEntity<Page<CompraResumenResponseDTO>> listarCompras(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String ordenarPor,
            @RequestParam(defaultValue = "desc") String direccion,
            @RequestParam(required = false) EstadoCompra estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta) {
        return ResponseEntity.ok(compraService.listarCompras(
                page,
                size,
                ordenarPor,
                direccion,
                desde,
                hasta,
                estado
        ));
    }

    @PostMapping("/{id}/confirmar")
    public ResponseEntity<CompraResponseDTO> confirmarCompra(@PathVariable Long id) {
        Compra compra = compraService.confirmarCompra(id);
        return ResponseEntity.ok(CompraResponseDTO.from(compra));
    }

    @PostMapping("/{id}/anular")
    public ResponseEntity<CompraResponseDTO> anularCompra(@PathVariable Long id) {
        Compra compra = compraService.anularCompra(id);
        return ResponseEntity.ok(CompraResponseDTO.from(compra));
    }
}
