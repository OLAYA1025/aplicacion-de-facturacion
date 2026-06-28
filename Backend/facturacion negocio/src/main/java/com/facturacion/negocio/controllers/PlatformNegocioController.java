package com.facturacion.negocio.controllers;

import java.net.URI;
import java.util.List;

import com.facturacion.negocio.dto.request.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.facturacion.negocio.dto.response.NegocioPlataformaResponseDTO;
import com.facturacion.negocio.services.PlatformNegocioService;

@RestController
@RequestMapping("/platform/negocios")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class PlatformNegocioController {

    private final PlatformNegocioService platformNegocioService;

    public PlatformNegocioController(PlatformNegocioService platformNegocioService) {
        this.platformNegocioService = platformNegocioService;
    }

    @PostMapping
    public ResponseEntity<NegocioPlataformaResponseDTO> crearNegocio(
            @RequestBody CrearNegocioPlataformaRequestDTO request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "body invalido");
        }
        PlatformNegocioService.NegocioConAdministrador creado = platformNegocioService.crearNegocioConAdministrador(
                request.getNombre(),
                request.getNit(),
                request.getDireccion(),
                request.getTelefono(),
                request.getAdminNombre(),
                request.getAdminUsername(),
                request.getAdminPassword(),
                request.getAdminEmail()
        );
        URI location = URI.create("/platform/negocios/" + creado.negocio().getId());
        return ResponseEntity.created(location).body(NegocioPlataformaResponseDTO.from(creado.negocio(), creado.administrador()));
    }

    @GetMapping
    public ResponseEntity<List<NegocioPlataformaResponseDTO>> listarNegocios() {
        List<NegocioPlataformaResponseDTO> negocios = platformNegocioService.listarNegocios()
                .stream()
                .map(NegocioPlataformaResponseDTO::from)
                .toList();
        return ResponseEntity.ok(negocios);
    }

    @PutMapping("/{id}/activar")
    public ResponseEntity<NegocioPlataformaResponseDTO> activarNegocio(@PathVariable Long id) {
        return ResponseEntity.ok(NegocioPlataformaResponseDTO.from(platformNegocioService.activarNegocio(id)));
    }

    @PutMapping("/{id}/desactivar")
    public ResponseEntity<NegocioPlataformaResponseDTO> desactivarNegocio(@PathVariable Long id) {
        return ResponseEntity.ok(NegocioPlataformaResponseDTO.from(platformNegocioService.desactivarNegocio(id)));
    }

    @PostMapping("/facturacion-electronica")
    public ResponseEntity<Long>
    crearConfiguracionFacturacionElectronica(@RequestBody CrearConfiguracionFeRequest request) {

        Long id =
                platformNegocioService
                        .crearConfiguracion(request.negocioId(),
                                request.proveedor(),
                                request.baseUrl(),
                                request.username(),
                                request.password(),
                                request.clientId(),
                                request.clientSecret(),
                                request.prefijoCode()
                        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(id);
    }

    @PutMapping(
            "/facturacion-electronica/{configuracionId}"
    )
    public ResponseEntity<Void>
    editarConfiguracionFacturacionElectronica(
            @PathVariable Long configuracionId,
            @RequestBody EditarConfiguracionFeRequest request
    ) {

        platformNegocioService.editarConfiguracionFacturacionElectronica(
                configuracionId,
                request
        );

        return ResponseEntity.noContent()
                .build();
    }

    @PostMapping("/numbering-ranges")
    public ResponseEntity<Long>
    crearNumberingRange(@RequestBody CrearNumberingRangeRequest request) {

        Long id =
                platformNegocioService
                        .crearNumberingRange(
                                request
                        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(id);
    }

    @PutMapping(
            "/numbering-ranges/{rangeId}"
    )
    public ResponseEntity<Void>
    editarNumberingRange(
            @PathVariable Long rangeId,
            @RequestBody EditarNumberingRangeRequest request
    ) {

        platformNegocioService.editarNumberingRange(
                rangeId,
                request
        );

        return ResponseEntity.noContent()
                .build();
    }


}
