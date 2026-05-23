package com.facturacion.negocio.services;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.facturacion.negocio.domain.Usuario;
import com.facturacion.negocio.dto.response.LoginResponseDTO;
import com.facturacion.negocio.dto.response.UsuarioResponseDTO;
import com.facturacion.negocio.enumerated.Rol;
import com.facturacion.negocio.repository.UsuarioRepository;
import com.facturacion.negocio.security.JwtService;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    // Genera TU propio hash dummy (BCrypt) y pégalo aquí.
    // Puedes generarlo una vez con passwordEncoder.encode("cualquier-cosa") en un test.
    private static final String DUMMY_BCRYPT_HASH =
            "$2a$10$u1hVwJw6mQb2m4g1h1qg2e3m9eQfD0n8rQe8m1m8Qw4o9n9q9p9qK";

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

        Usuario usuario = resolverUsuarioParaLogin(username.trim(), password);
        if (usuario == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "credenciales invalidas");
        }

        String token = jwtService.generarToken(usuario);
        long expiracion = jwtService.calcularExpiracionEpochMillis();
        return LoginResponseDTO.of(token, expiracion, UsuarioResponseDTO.from(usuario));
    }

    private boolean isBcryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }

    private Usuario resolverUsuarioParaLogin(String username, String password) {
        List<Usuario> usuarios = usuarioRepository.findAllWithNegocioByUsername(username);
        if (usuarios.isEmpty()) {
            passwordEncoder.matches(password, DUMMY_BCRYPT_HASH);
            return null;
        }

        List<Usuario> coincidenciasActivas = new ArrayList<>();
        Usuario coincidenciaParaMigracion = null;

        for (Usuario usuario : usuarios) {
            String stored = usuario.getPassword();
            if (stored == null) {
                continue;
            }

            boolean passwordOk = false;
            boolean passwordEnTextoPlano = false;
            if (isBcryptHash(stored)) {
                passwordOk = passwordEncoder.matches(password, stored);
            } else if (stored.equals(password)) {
                passwordOk = true;
                passwordEnTextoPlano = true;
            }

            if (!passwordOk) {
                continue;
            }

            if (passwordEnTextoPlano) {
                coincidenciaParaMigracion = usuario;
            }

            if (!usuario.isActivo()) {
                continue;
            }
            if (usuario.getRol() == Rol.SUPER_ADMIN) {
                if (usuario.getNegocio() == null) {
                    coincidenciasActivas.add(usuario);
                }
                continue;
            }
            if (usuario.getNegocio() != null && usuario.getNegocio().isActivo()) {
                coincidenciasActivas.add(usuario);
            }
        }

        if (coincidenciasActivas.isEmpty()) {
            return null;
        }
        if (coincidenciasActivas.size() > 1) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "username ambiguo; debes diferenciar el acceso por negocio"
            );
        }

        Usuario usuario = coincidenciasActivas.get(0);
        if (coincidenciaParaMigracion != null && esMismoUsuario(coincidenciaParaMigracion, usuario)) {
            usuario.setPassword(passwordEncoder.encode(password));
            usuarioRepository.save(usuario);
        }
        return usuario;
    }

    private boolean esMismoUsuario(Usuario esperado, Usuario actual) {
        if (esperado == actual) {
            return true;
        }
        if (esperado == null || actual == null) {
            return false;
        }
        if (esperado.getId() != null && actual.getId() != null) {
            return esperado.getId().equals(actual.getId());
        }
        return false;
    }
}
