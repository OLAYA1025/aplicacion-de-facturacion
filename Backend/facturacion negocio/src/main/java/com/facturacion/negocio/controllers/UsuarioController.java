package com.facturacion.negocio.controllers;

import com.facturacion.negocio.domain.Usuario;
import com.facturacion.negocio.dto.request.ActualizarUsuarioRequestDTO;
import com.facturacion.negocio.dto.request.CambiarPasswordUsuarioRequestDTO;
import com.facturacion.negocio.dto.request.CrearUsuarioRequestDTO;
import com.facturacion.negocio.dto.response.UsuarioResponseDTO;
import com.facturacion.negocio.services.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@PreAuthorize("hasRole('ADMINISTRADOR')")
@RequestMapping("usuario")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> obtenerUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioService.obtenerUsuario(id);
        return ResponseEntity.ok(UsuarioResponseDTO.from(usuario));
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponseDTO>> listarUsuarios() {
        List<UsuarioResponseDTO> usuarios = usuarioService.listarUsuarios()
                .stream()
                .map(UsuarioResponseDTO::from)
                .toList();
        return ResponseEntity.ok(usuarios);
    }

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> crearUsuario(@RequestBody CrearUsuarioRequestDTO request){
        Usuario usuario = usuarioService.crearUsuario(request.getNombre(), request.getUsername(), request.getPassword(), request.getEmail(), request.getRol());
        URI location = URI.create("/usuario/" + usuario.getId());
        return ResponseEntity.created(location).body(UsuarioResponseDTO.from(usuario));

    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponseDTO> actualizarUsuario(@PathVariable Long id,
                                                                @RequestBody ActualizarUsuarioRequestDTO request) {
        Usuario usuario = usuarioService.actualizarUsuario(
                id,
                request.getNombre(),
                request.getUsername(),
                request.getEmail(),
                request.getRol()
        );
        return ResponseEntity.ok(UsuarioResponseDTO.from(usuario));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<UsuarioResponseDTO> cambiarPassword(@PathVariable Long id,
                                                              @RequestBody CambiarPasswordUsuarioRequestDTO request) {
        Usuario usuario = usuarioService.cambiarPassword(id, request.getNuevaPassword());
        return ResponseEntity.ok(UsuarioResponseDTO.from(usuario));
    }

    @PostMapping("/{id}/activar")
    public ResponseEntity<UsuarioResponseDTO> activarUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioService.activarUsuario(id);
        return ResponseEntity.ok(UsuarioResponseDTO.from(usuario));
    }

    @PostMapping("/{id}/desactivar")
    public ResponseEntity<UsuarioResponseDTO> desactivarUsuario(@PathVariable Long id) {
        Usuario usuario = usuarioService.desactivarUsuario(id);
        return ResponseEntity.ok(UsuarioResponseDTO.from(usuario));
    }


}
