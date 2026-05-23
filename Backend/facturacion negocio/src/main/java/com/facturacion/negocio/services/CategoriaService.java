package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.Categoria;
import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.repository.CategoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final TenantService tenantService;

    public CategoriaService(CategoriaRepository categoriaRepository, TenantService tenantService) {
        this.categoriaRepository = categoriaRepository;
        this.tenantService = tenantService;
    }

    public Categoria crearCategoria(String nombre) {
        Negocio negocio = tenantService.getNegocioActual();
        String nombreNormalizado = validarNombre(nombre);
        if (categoriaRepository.existsByNombreIgnoreCaseAndNegocio(nombreNormalizado, negocio)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "la categoria ya existe");
        }
        Categoria categoria = new Categoria(nombreNormalizado);
        categoria.setNegocio(negocio);
        return categoriaRepository.save(categoria);
    }

    public List<Categoria> obtenerCategorias() {
        return categoriaRepository.findAllByNegocioOrderByNombreAsc(tenantService.getNegocioActual());
    }

    public Categoria obtenerCategoria(Long id) {
        return categoriaRepository.findByIdAndNegocio(id, tenantService.getNegocioActual())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "categoria no encontrada"));
    }

    public Categoria actualizarCategoria(Long id, String nombre) {
        if (id == null || id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "categoriaId invalido");
        }
        Categoria categoria = obtenerCategoria(id);
        String nombreNormalizado = validarNombre(nombre);

        if (categoriaRepository.existsByNombreIgnoreCaseAndIdNotAndNegocio(
                nombreNormalizado,
                id,
                categoria.getNegocio()
        )) {
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
