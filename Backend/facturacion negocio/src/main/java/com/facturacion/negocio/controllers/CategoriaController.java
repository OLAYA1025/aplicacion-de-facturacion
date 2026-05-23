package com.facturacion.negocio.controllers;

import com.facturacion.negocio.domain.Categoria;
import com.facturacion.negocio.dto.request.CrearCategoriaRequestDTO;
import com.facturacion.negocio.dto.response.CategoriaResponseDTO;
import com.facturacion.negocio.services.CategoriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public ResponseEntity<List<CategoriaResponseDTO>> obtenerCategorias() {
        List<CategoriaResponseDTO> categorias = categoriaService.obtenerCategorias()
                .stream()
                .map(CategoriaResponseDTO::from)
                .toList();
        return ResponseEntity.ok(categorias);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping
    public ResponseEntity<CategoriaResponseDTO> crearCategoria(@RequestBody CrearCategoriaRequestDTO request) {
        Categoria categoria = categoriaService.crearCategoria(request.getNombre());
        URI location = URI.create("/categoria/" + categoria.getId());
        return ResponseEntity.created(location).body(CategoriaResponseDTO.from(categoria));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> actualizarCategoria(@PathVariable Long id,
                                                                    @RequestBody CrearCategoriaRequestDTO request) {
        Categoria categoria = categoriaService.actualizarCategoria(id, request.getNombre());
        return ResponseEntity.ok(CategoriaResponseDTO.from(categoria));
    }
}
