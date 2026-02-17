package com.facturacion.negocio.controllers;

import com.facturacion.negocio.domain.Usuario;
import com.facturacion.negocio.dto.request.CrearUsuarioRequestDTO;
import com.facturacion.negocio.dto.response.PedidoResponseDTO;
import com.facturacion.negocio.dto.response.UsuarioResponseDTO;
import com.facturacion.negocio.services.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
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

    @PostMapping
    public ResponseEntity<UsuarioResponseDTO> crearUsuario(@RequestBody CrearUsuarioRequestDTO request){
        Usuario usuario = usuarioService.crearUsuario(request.getNombre(), request.getUsername(), request.getPassword(), request.getEmail(), request.getRol());
        URI location = URI.create("/usuario/" + usuario.getId());
        return ResponseEntity.created(location).body(UsuarioResponseDTO.from(usuario));

    }


}
