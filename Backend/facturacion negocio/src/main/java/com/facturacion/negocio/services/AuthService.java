package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.Usuario;
import com.facturacion.negocio.dto.response.LoginResponseDTO;
import com.facturacion.negocio.dto.response.UsuarioResponseDTO;
import com.facturacion.negocio.repository.UsuarioRepository;
import com.facturacion.negocio.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public LoginResponseDTO login(String username, String password) {
        if (username == null || username.isBlank() || password == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "credenciales invalidas");
        }

        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "credenciales invalidas"));

        if (!usuario.isActivo()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "usuario desactivado");
        }

        if (!passwordMatches(usuario, password)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "credenciales invalidas");
        }

        String token = jwtService.generarToken(usuario);
        long expiracion = jwtService.calcularExpiracionEpochMillis();
        return LoginResponseDTO.of(token, expiracion, UsuarioResponseDTO.from(usuario));
    }

    private boolean passwordMatches(Usuario usuario, String rawPassword) {
        String stored = usuario.getPassword();
        if (stored == null) {
            return false;
        }

        if (isBcryptHash(stored)) {
            return passwordEncoder.matches(rawPassword, stored);
        }

        if (stored.equals(rawPassword)) {
            usuario.setPassword(passwordEncoder.encode(rawPassword));
            usuarioRepository.save(usuario);
            return true;
        }

        return false;
    }

    private boolean isBcryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
}
