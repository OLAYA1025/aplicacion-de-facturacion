package com.facturacion.negocio.controllers;

import com.facturacion.negocio.domain.Proveedor;
import com.facturacion.negocio.dto.request.ActualizarProveedorRequestDTO;
import com.facturacion.negocio.dto.request.CrearProveedorRequestDTO;
import com.facturacion.negocio.dto.response.ProveedorResponseDTO;
import com.facturacion.negocio.services.ProveedorService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequestMapping("/proveedor")
public class ProveedorController {

    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    @PostMapping
    public ResponseEntity<ProveedorResponseDTO> crearProveedor(@RequestBody CrearProveedorRequestDTO request) {
        Proveedor proveedor = proveedorService.crearProveedor(
                request.getId(),
                request.getNombre(),
                request.getTelefono(),
                request.getCorreo(),
                request.getTipoDocumento()
        );
        URI location = URI.create("/proveedor/" + proveedor.getId());
        return ResponseEntity.created(location).body(ProveedorResponseDTO.from(proveedor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProveedorResponseDTO> editarProveedor(@PathVariable Long id,
                                                                @RequestBody ActualizarProveedorRequestDTO request) {
        Proveedor proveedor = proveedorService.editarProveedor(
                id,
                request.getNombre(),
                request.getTelefono(),
                request.getCorreo(),
                request.getTipoDocumento()
        );
        return ResponseEntity.ok(ProveedorResponseDTO.from(proveedor));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProveedorResponseDTO> obtenerProveedor(@PathVariable Long id) {
        Proveedor proveedor = proveedorService.obtenerProveedor(id);
        return ResponseEntity.ok(ProveedorResponseDTO.from(proveedor));
    }

    @GetMapping
    public ResponseEntity<List<ProveedorResponseDTO>> listarProveedores() {
        List<Proveedor> proveedores = proveedorService.listarProveedores();
        List<ProveedorResponseDTO> response = new ArrayList<>();
        for (Proveedor proveedor : proveedores) {
            response.add(ProveedorResponseDTO.from(proveedor));
        }
        return ResponseEntity.ok(response);
    }
}
