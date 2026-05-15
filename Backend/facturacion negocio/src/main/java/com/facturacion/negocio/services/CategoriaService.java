package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.Categoria;
import com.facturacion.negocio.repository.CategoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public CategoriaService(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    public Categoria crearCategoria(String nombre) {
        String nombreNormalizado = validarNombre(nombre);
        if (categoriaRepository.existsByNombreIgnoreCase(nombreNormalizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "la categoria ya existe");
        }
        Categoria categoria = new Categoria(nombreNormalizado);
        return categoriaRepository.save(categoria);
    }

    public List<Categoria> obtenerCategorias() {
        return categoriaRepository.findAll();
    }

    public Categoria obtenerCategoria(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "categoria no encontrada"));
    }

    public Categoria actualizarCategoria(Long id, String nombre) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "categoriaId invalido");
        }
        Categoria categoria = obtenerCategoria(id);
        String nombreNormalizado = validarNombre(nombre);

        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNot(nombreNormalizado, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "la categoria ya existe");
        }

        categoria.setNombre(nombreNormalizado);
        return categoriaRepository.save(categoria);
    }

    private String validarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "nombre categoria invalido");
        }
        String nombreNormalizado = nombre.trim();
        if (nombreNormalizado.length() > 100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "nombre categoria invalido");
        }
        return nombreNormalizado;
    }
}
