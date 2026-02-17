package com.facturacion.negocio.controllers;

import com.facturacion.negocio.domain.Producto;
import com.facturacion.negocio.dto.request.CrearProductoRequestDTO;
import com.facturacion.negocio.dto.request.ProductoUpdateRequestDTO;
import com.facturacion.negocio.services.ProductoService;
import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Producto> obtenerProducto(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerProducto(id));
    }

    @GetMapping
    public ResponseEntity<List<Producto>> obtenerProductos() {
        return ResponseEntity.ok(productoService.obtenerProductos());
    }

    @GetMapping("/inventariable")
    public ResponseEntity<List<Producto>> obtenerProductosInventario() {
        return ResponseEntity.ok(productoService.obtenerProductosInventariables());
    }

    @GetMapping("/preparacion")
    public ResponseEntity<List<Producto>> obtenerProductosPreparacion() {
        return ResponseEntity.ok(productoService.obtenerProductosPreparacion());
    }

    @GetMapping("/categoria/{id}")
    public ResponseEntity<List<Producto>> obtenerProductosPorCategoria(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerProductosPorCategoria(id));
    }

    @PostMapping
    public ResponseEntity<Producto> crearProducto(@RequestBody CrearProductoRequestDTO request) {
        Producto producto = productoService.crearProducto(request.getNombre(),
                request.getPrecio(),
                request.getDescripcion(),
                request.getCategoriaId(),
                request.getTipoGestion(),
                request.getStockInicial(),
                request.getStockMinimo()
        );

        URI location = URI.create("/producto/"+ producto.getCodigo());

        return ResponseEntity.created(location).body(producto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizarProducto( @PathVariable Long id,
                                                        @RequestBody ProductoUpdateRequestDTO request) {
        Producto producto = productoService.editarProducto(id,
                request.getNombre(),
                request.getPrecio(),
                request.getDescripcion(),
                request.getCategoriaId(),
                request.getTipoGestion(),
                request.getStockMinimo());

        return ResponseEntity.ok(producto);
    }

    @PostMapping("/{id}/desactivar")
    public ResponseEntity<Producto> desactivarProducto(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.desactivarProducto(id));
    }

    @PostMapping("/{id}/activar")
    public ResponseEntity<Producto> activarProducto(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.activarProducto(id));
    }

}
