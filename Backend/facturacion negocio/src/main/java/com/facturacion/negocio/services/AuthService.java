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

        Usuario usuario = usuarioRepository.findByUsername(username).orElse(null);

        // Siempre hacemos una verificación BCrypt (real o dummy)
        String stored = (usuario != null) ? usuario.getPassword() : null;

        boolean isBcrypt = stored != null && isBcryptHash(stored);
        String hashToCheck = isBcrypt ? stored : DUMMY_BCRYPT_HASH;

        // Esto “igualará” el costo en ambos casos (exista o no exista)
        boolean bcryptMatches = passwordEncoder.matches(password, hashToCheck);

        // Manejo de legado en texto plano (si todavía tienes)
        boolean passwordOk = false;
        if (usuario != null && stored != null) {
            if (isBcrypt) {
                passwordOk = bcryptMatches;
            } else {
                // Comparación texto plano (legado) + migración
                if (stored.equals(password)) {
                    usuario.setPassword(passwordEncoder.encode(password));
                    usuarioRepository.save(usuario);
                    passwordOk = true;
                } else {
                    passwordOk = false;
                }
            }
        } else {
            // Usuario no existe: solo usamos el bcrypt dummy y fallamos
            passwordOk = false;
        }

        // No filtres estado (evita enumeración). Respuesta genérica.
        if (usuario == null || !passwordOk || !usuario.isActivo()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "credenciales invalidas");
        }

        String token = jwtService.generarToken(usuario);
        long expiracion = jwtService.calcularExpiracionEpochMillis();
        return LoginResponseDTO.of(token, expiracion, UsuarioResponseDTO.from(usuario));
    }

    private boolean isBcryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
}