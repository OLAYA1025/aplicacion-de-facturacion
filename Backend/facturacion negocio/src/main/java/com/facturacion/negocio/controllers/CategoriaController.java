package com.facturacion.negocio.controllers;

import com.facturacion.negocio.domain.Categoria;
import com.facturacion.negocio.dto.request.CrearCategoriaRequestDTO;
import com.facturacion.negocio.services.CategoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/categoria")
public class CategoriaController {

    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @GetMapping
    public ResponseEntity<List<Categoria>> obtenerCategorias() {
        return ResponseEntity.ok(categoriaService.obtenerCategorias());
    }

    @PostMapping
    public ResponseEntity<Categoria> crearCategoria(@RequestBody CrearCategoriaRequestDTO request) {
        Categoria categoria = categoriaService.crearCategoria(request.getNombre());
        URI location = URI.create("/categoria/" + categoria.getId());
        return ResponseEntity.created(location).body(categoria);
    }
}
