package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.Usuario;
import com.facturacion.negocio.enumerated.Rol;
import com.facturacion.negocio.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Usuario obtenerUsuario(Long usuarioId) {
        if(usuarioId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envia un ID valido");
        }
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Usuario no encontrado")
                );

    }


    public Usuario crearUsuario(String nombre, String username, String password, String email, Rol rol) {

        if(nombre == null || username == null || password == null || rol == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "datos invalidos");
        }

        if(usuarioRepository.existsByUsername(username)){
           throw new ResponseStatusException(HttpStatus.CONFLICT, "usuario existente");
        }
        String encodedPassword = passwordEncoder.encode(password);
        Usuario usuario = new Usuario(nombre, username, encodedPassword, email, rol);
        return usuarioRepository.save(usuario);
    }



}
