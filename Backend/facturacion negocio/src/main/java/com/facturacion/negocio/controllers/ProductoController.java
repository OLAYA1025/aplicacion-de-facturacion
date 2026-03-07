package com.facturacion.negocio.controllers;

import com.facturacion.negocio.domain.Producto;
import com.facturacion.negocio.dto.request.CrearProductoRequestDTO;
import com.facturacion.negocio.dto.request.ProductoUpdateRequestDTO;
import com.facturacion.negocio.dto.response.ProductoResponseDTO;
import com.facturacion.negocio.services.ProductoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/producto")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerProducto(@PathVariable Long id) {
        return ResponseEntity.ok(ProductoResponseDTO.fromEntity(productoService.obtenerProducto(id)));
    }

    @GetMapping
    public ResponseEntity<List<ProductoResponseDTO>> obtenerProductos() {
        return ResponseEntity.ok(productoService.obtenerProductos().stream()
                .map(ProductoResponseDTO::fromEntity)
                .toList());
    }

    @GetMapping("/inventariable")
    public ResponseEntity<List<ProductoResponseDTO>> obtenerProductosInventario() {
        return ResponseEntity.ok(productoService.obtenerProductosInventariables().stream()
                .map(ProductoResponseDTO::fromEntity)
                .toList());
    }

    @GetMapping("/preparacion")
    public ResponseEntity<List<ProductoResponseDTO>> obtenerProductosPreparacion() {
        return ResponseEntity.ok(productoService.obtenerProductosPreparacion().stream()
                .map(ProductoResponseDTO::fromEntity)
                .toList());
    }

    @GetMapping("/categoria/{id}")
    public ResponseEntity<List<ProductoResponseDTO>> obtenerProductosPorCategoria(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerProductosPorCategoria(id).stream()
                .map(ProductoResponseDTO::fromEntity)
                .toList());
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping
    public ResponseEntity<ProductoResponseDTO> crearProducto(@RequestBody CrearProductoRequestDTO request) {
        Producto producto = productoService.crearProducto(request.getNombre(),
                request.getPrecio(),
                request.getDescripcion(),
                request.getCategoriaId(),
                request.resolverTipoProducto(),
                request.getTipoImpuesto(),
                request.getImpuestoPorcentaje(),
                request.getStockInicial(),
                request.getStockMinimo()
        );

        URI location = URI.create("/producto/"+ producto.getCodigo());

        return ResponseEntity.created(location).body(ProductoResponseDTO.fromEntity(producto));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> actualizarProducto( @PathVariable Long id,
                                                        @RequestBody ProductoUpdateRequestDTO request) {
        Producto producto = productoService.editarProducto(id,
                request.getNombre(),
                request.getPrecio(),
                request.getDescripcion(),
                request.getCategoriaId(),
                request.resolverTipoProducto(),
                request.getTipoImpuesto(),
                request.getImpuestoPorcentaje(),
                request.getStockMinimo());

        return ResponseEntity.ok(ProductoResponseDTO.fromEntity(producto));
    }

    @PostMapping("/{id}/desactivar")
    public ResponseEntity<ProductoResponseDTO> desactivarProducto(@PathVariable Long id) {
        return ResponseEntity.ok(ProductoResponseDTO.fromEntity(productoService.desactivarProducto(id)));
    }

    @PostMapping("/{id}/activar")
    public ResponseEntity<ProductoResponseDTO> activarProducto(@PathVariable Long id) {
        return ResponseEntity.ok(ProductoResponseDTO.fromEntity(productoService.activarProducto(id)));
    }

}
