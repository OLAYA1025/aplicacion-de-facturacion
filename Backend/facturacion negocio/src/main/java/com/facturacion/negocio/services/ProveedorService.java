package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.Proveedor;
import com.facturacion.negocio.enumerated.TipoDocumento;
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

    public Proveedor crearProveedor(Long id, String nombre, String telefono, String correo, TipoDocumento tipoDocumento) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id proveedor requerido");
        }
        if (proveedorRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "ya existe un proveedor con ese id");
        }
        String nombreNormalizado = normalizarNombre(nombre);
        String correoNormalizado = normalizarCorreo(correo);
        if (correoNormalizado != null && proveedorRepository.existsByCorreoIgnoreCase(correoNormalizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "correo de proveedor ya registrado");
        }
        TipoDocumento tipoDocumentoNormalizado = normalizarTipoDocumento(tipoDocumento);
        Proveedor proveedor = new Proveedor(
                id,
                nombreNormalizado,
                normalizarTelefono(telefono),
                correoNormalizado,
                tipoDocumentoNormalizado
        );
        return proveedorRepository.save(proveedor);
    }

    public Proveedor editarProveedor(Long id, String nombre, String telefono, String correo, TipoDocumento tipoDocumento) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id proveedor requerido");
        }
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));

        String nombreNormalizado = normalizarNombre(nombre);
        String correoNormalizado = normalizarCorreo(correo);
        if (correoNormalizado != null && proveedorRepository.existsByCorreoIgnoreCaseAndIdNot(correoNormalizado, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "correo de proveedor ya registrado");
        }
        TipoDocumento tipoDocumentoNormalizado = normalizarTipoDocumento(tipoDocumento);

        proveedor.actualizar(
                nombreNormalizado,
                normalizarTelefono(telefono),
                correoNormalizado,
                tipoDocumentoNormalizado
        );
        return proveedorRepository.save(proveedor);
    }

    public Proveedor obtenerProveedor(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));
    }

    public List<Proveedor> listarProveedores() {
        return proveedorRepository.findAll();
    }

    private String normalizarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "nombre proveedor invalido");
        }
        return nombre.trim();
    }

    private String normalizarTelefono(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            return null;
        }
        return telefono.trim();
    }

    private String normalizarCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            return null;
        }
        return correo.trim();
    }

    private TipoDocumento normalizarTipoDocumento(TipoDocumento tipoDocumento) {
        if (tipoDocumento == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tipoDocumento invalido");
        }
        return tipoDocumento;
    }
}
