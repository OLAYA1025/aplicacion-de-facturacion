package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.Proveedor;
import com.facturacion.negocio.repository.ProveedorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public ProveedorService(ProveedorRepository proveedorRepository) {
        this.proveedorRepository = proveedorRepository;
    }

    public Proveedor crearProveedor(Long id, String nombre, String telefono, String correo) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id proveedor requerido");
        }
        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "nombre proveedor invalido");
        }
        if (proveedorRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ya existe un proveedor con ese id");
        }
        if (correo != null && !correo.isBlank() && proveedorRepository.existsByCorreoIgnoreCase(correo.trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "correo de proveedor ya registrado");
        }
        Proveedor proveedor = new Proveedor(id, nombre.trim(), telefono, normalizarCorreo(correo));
        return proveedorRepository.save(proveedor);
    }

    public Proveedor obtenerProveedor(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));
    }

    public List<Proveedor> listarProveedores() {
        return proveedorRepository.findAll();
    }

    private String normalizarCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            return null;
        }
        return correo.trim();
    }
}
