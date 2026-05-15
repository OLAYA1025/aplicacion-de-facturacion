package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.Usuario;
import com.facturacion.negocio.enumerated.Rol;
import com.facturacion.negocio.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
        if (usuarioId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Envia un ID valido");
        }
        return usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    public List<Usuario> listarUsuarios() {
        Usuario usuarioAutenticado = obtenerUsuarioAutenticado();
        validarAdministrador(usuarioAutenticado);
        return usuarioRepository.findAll();
    }

    public Usuario crearUsuario(String nombre, String username, String password, String email, Rol rol) {
        String nombreNormalizado = normalizarRequerido(nombre, "nombre invalido");
        String usernameNormalizado = normalizarRequerido(username, "username invalido");
        String passwordNormalizada = normalizarRequerido(password, "password invalido");
        if (rol == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rol invalido");
        }

        if (usuarioRepository.existsByUsername(usernameNormalizado)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "usuario existente");
        }

        String encodedPassword = passwordEncoder.encode(passwordNormalizada);
        Usuario usuario = new Usuario(
                nombreNormalizado,
                usernameNormalizado,
                encodedPassword,
                normalizarOpcional(email),
                rol
        );
        return usuarioRepository.save(usuario);
    }

    public Usuario actualizarUsuario(Long id, String nombre, String username, String email, Rol rol) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id de usuario invalido");
        }

        Usuario usuarioAutenticado = obtenerUsuarioAutenticado();
        validarAdministrador(usuarioAutenticado);

        Usuario usuarioObjetivo = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        String nombreNormalizado = normalizarRequerido(nombre, "nombre invalido");
        String usernameNormalizado = normalizarRequerido(username, "username invalido");
        if (rol == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rol invalido");
        }

        if (usuarioRepository.existsByUsernameAndIdNot(usernameNormalizado, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "usuario existente");
        }

        usuarioObjetivo.setNombre(nombreNormalizado);
        usuarioObjetivo.setUsername(usernameNormalizado);
        usuarioObjetivo.setEmail(normalizarOpcional(email));
        usuarioObjetivo.setRol(rol);

        return usuarioRepository.save(usuarioObjetivo);
    }

    public Usuario cambiarPassword(Long id, String nuevaPassword) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id de usuario invalido");
        }

        Usuario usuarioAutenticado = obtenerUsuarioAutenticado();
        validarAdministrador(usuarioAutenticado);

        Usuario usuarioObjetivo = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        boolean objetivoEsAdmin = usuarioObjetivo.getRol() == Rol.ADMINISTRADOR;
        boolean editandoPropioUsuario = usuarioAutenticado.getId().equals(usuarioObjetivo.getId());
        if (objetivoEsAdmin && !editandoPropioUsuario) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Un administrador solo puede cambiar su propia password de administrador");
        }

        String nuevaPasswordNormalizada = normalizarRequerido(nuevaPassword, "password invalido");
        usuarioObjetivo.setPassword(passwordEncoder.encode(nuevaPasswordNormalizada));
        return usuarioRepository.save(usuarioObjetivo);
    }

    public Usuario activarUsuario(Long id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id de usuario invalido");
        }

        Usuario usuarioAutenticado = obtenerUsuarioAutenticado();
        validarAdministrador(usuarioAutenticado);

        Usuario usuarioObjetivo = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        usuarioObjetivo.activar();
        return usuarioRepository.save(usuarioObjetivo);
    }

    public Usuario desactivarUsuario(Long id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id de usuario invalido");
        }

        Usuario usuarioAutenticado = obtenerUsuarioAutenticado();
        validarAdministrador(usuarioAutenticado);

        Usuario usuarioObjetivo = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));

        if (usuarioObjetivo.getRol() == Rol.ADMINISTRADOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo se puede desactivar usuarios que no sean administradores");
        }

        usuarioObjetivo.desactivar();
        return usuarioRepository.save(usuarioObjetivo);
    }

    private String normalizarRequerido(String valor, String mensajeError) {
        if (valor == null || valor.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, mensajeError);
        }
        return valor.trim();
    }

    private String normalizarOpcional(String valor) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        return valor.trim();
    }

    private Usuario obtenerUsuarioAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        String username = auth.getName();
        if (username == null || username.isBlank() || "anonymousUser".equalsIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }

        return usuarioRepository.findByUsername(username.trim())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado"));
    }

    private void validarAdministrador(Usuario usuarioAutenticado) {
        if (usuarioAutenticado.getRol() != Rol.ADMINISTRADOR) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Solo un administrador puede editar usuarios");
        }
    }
}
