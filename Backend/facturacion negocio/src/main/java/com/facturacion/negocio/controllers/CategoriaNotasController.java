package com.facturacion.negocio.controllers;

import com.facturacion.negocio.domain.GrupoNotaCategoria;
import com.facturacion.negocio.domain.NotaPredefinida;
import com.facturacion.negocio.dto.request.GrupoNotaCategoriaRequestDTO;
import com.facturacion.negocio.dto.request.NotaPredefinidaRequestDTO;
import com.facturacion.negocio.dto.response.GrupoNotaCategoriaResponseDTO;
import com.facturacion.negocio.dto.response.NotaPredefinidaResponseDTO;
import com.facturacion.negocio.services.NotasPredefinidasService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/categoria/{categoriaId}/grupos-notas")
public class CategoriaNotasController {

    private final NotasPredefinidasService notasPredefinidasService;

    public CategoriaNotasController(NotasPredefinidasService notasPredefinidasService) {
        this.notasPredefinidasService = notasPredefinidasService;
    }

    @GetMapping
    public ResponseEntity<List<GrupoNotaCategoriaResponseDTO>> obtenerGruposConNotas(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(notasPredefinidasService.obtenerGruposConNotasPorCategoria(categoriaId));
    }

    @GetMapping("/detalle")
    public ResponseEntity<List<GrupoNotaCategoriaResponseDTO>> obtenerDetalleGruposConNotas(@PathVariable Long categoriaId) {
        return ResponseEntity.ok(notasPredefinidasService.obtenerGruposConNotasPorCategoria(categoriaId));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping
    public ResponseEntity<GrupoNotaCategoriaResponseDTO> crearGrupo(@PathVariable Long categoriaId,
                                                                     @RequestBody GrupoNotaCategoriaRequestDTO request) {
        GrupoNotaCategoria grupo = notasPredefinidasService.crearGrupo(
                categoriaId,
                request.getNombre(),
                request.getObligatorio(),
                request.getTipoSeleccion()
        );
        URI location = URI.create("/categoria/" + categoriaId + "/grupos-notas/" + grupo.getId());
        return ResponseEntity.created(location).body(GrupoNotaCategoriaResponseDTO.from(grupo));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/{grupoId}")
    public ResponseEntity<GrupoNotaCategoriaResponseDTO> actualizarGrupo(@PathVariable Long categoriaId,
                                                                          @PathVariable Long grupoId,
                                                                          @RequestBody GrupoNotaCategoriaRequestDTO request) {
        GrupoNotaCategoria grupo = notasPredefinidasService.actualizarGrupo(
                categoriaId,
                grupoId,
                request.getNombre(),
                request.getObligatorio(),
                request.getTipoSeleccion()
        );
        return ResponseEntity.ok(GrupoNotaCategoriaResponseDTO.from(grupo));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/{grupoId}")
    public ResponseEntity<Void> eliminarGrupo(@PathVariable Long categoriaId, @PathVariable Long grupoId) {
        notasPredefinidasService.eliminarGrupo(categoriaId, grupoId);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PostMapping("/{grupoId}/notas")
    public ResponseEntity<NotaPredefinidaResponseDTO> crearNota(@PathVariable Long categoriaId,
                                                                 @PathVariable Long grupoId,
                                                                 @RequestBody NotaPredefinidaRequestDTO request) {
        NotaPredefinida nota = notasPredefinidasService.crearNota(categoriaId, grupoId, request.getDescripcion());
        URI location = URI.create("/categoria/" + categoriaId + "/grupos-notas/" + grupoId + "/notas/" + nota.getId());
        return ResponseEntity.created(location).body(NotaPredefinidaResponseDTO.from(nota));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @PutMapping("/{grupoId}/notas/{notaId}")
    public ResponseEntity<NotaPredefinidaResponseDTO> actualizarNota(@PathVariable Long categoriaId,
                                                                      @PathVariable Long grupoId,
                                                                      @PathVariable Long notaId,
                                                                      @RequestBody NotaPredefinidaRequestDTO request) {
        NotaPredefinida nota = notasPredefinidasService.actualizarNota(categoriaId, grupoId, notaId, request.getDescripcion());
        return ResponseEntity.ok(NotaPredefinidaResponseDTO.from(nota));
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    @DeleteMapping("/{grupoId}/notas/{notaId}")
    public ResponseEntity<Void> eliminarNota(@PathVariable Long categoriaId,
                                             @PathVariable Long grupoId,
                                             @PathVariable Long notaId) {
        notasPredefinidasService.eliminarNota(categoriaId, grupoId, notaId);
        return ResponseEntity.noContent().build();
    }
}
