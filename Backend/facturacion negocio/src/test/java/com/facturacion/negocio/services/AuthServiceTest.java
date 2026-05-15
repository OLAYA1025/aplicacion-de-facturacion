package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.Usuario;
import com.facturacion.negocio.dto.response.LoginResponseDTO;
import com.facturacion.negocio.enumerated.Rol;
import com.facturacion.negocio.repository.UsuarioRepository;
import com.facturacion.negocio.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(usuarioRepository, passwordEncoder, jwtService);
    }

    @Nested
    class Login {

        @Test
        void debeRetornarBadRequestCuandoUsernameEsInvalido() {
            assertThatThrownBy(() -> authService.login(" ", "clave"))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException response = (ResponseStatusException) ex;
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                        assertThat(response.getReason()).isEqualTo("credenciales invalidas");
                    });
        }

        @Test
        void debeAutenticarUsuarioConPasswordBcrypt() {
            String hashBcrypt = "$2a$10$7EqJtq98hPqEX7fNZaFWoOeR7Yz7jH9MuNoMNFkQJUO2cRPn1ytGa";
            Usuario usuario = usuarioActivo("admin", hashBcrypt);

            when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches("clave-segura", hashBcrypt)).thenReturn(true);
            when(jwtService.generarToken(usuario)).thenReturn("jwt-token");
            when(jwtService.calcularExpiracionEpochMillis()).thenReturn(1_700_000_000_000L);

            LoginResponseDTO response = authService.login("admin", "clave-segura");

            assertThat(response.getToken()).isEqualTo("jwt-token");
            assertThat(response.getTipo()).isEqualTo("Bearer");
            assertThat(response.getExpiracionEpochMillis()).isEqualTo(1_700_000_000_000L);
            assertThat(response.getUsuario().getUsername()).isEqualTo("admin");
            verify(usuarioRepository, never()).save(any(Usuario.class));
        }

        @Test
        void debeMigrarPasswordPlanoCuandoCoincide() {
            Usuario usuario = usuarioActivo("cajero", "1234");

            when(usuarioRepository.findByUsername("cajero")).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches(eq("1234"), anyString())).thenReturn(false);
            when(passwordEncoder.encode("1234")).thenReturn("hash-nuevo");
            when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> invocation.getArgument(0));
            when(jwtService.generarToken(usuario)).thenReturn("jwt-migrado");
            when(jwtService.calcularExpiracionEpochMillis()).thenReturn(1_800_000_000_000L);

            LoginResponseDTO response = authService.login("cajero", "1234");

            assertThat(response.getToken()).isEqualTo("jwt-migrado");
            ArgumentCaptor<Usuario> usuarioCaptor = ArgumentCaptor.forClass(Usuario.class);
            verify(usuarioRepository).save(usuarioCaptor.capture());
            assertThat(usuarioCaptor.getValue().getPassword()).isEqualTo("hash-nuevo");
        }

        @Test
        void debeRetornarUnauthorizedCuandoUsuarioInactivo() {
            String hashBcrypt = "$2a$10$7EqJtq98hPqEX7fNZaFWoOeR7Yz7jH9MuNoMNFkQJUO2cRPn1ytGa";
            Usuario usuario = usuarioActivo("inactivo", hashBcrypt);
            usuario.desactivar();

            when(usuarioRepository.findByUsername("inactivo")).thenReturn(Optional.of(usuario));
            when(passwordEncoder.matches("clave", hashBcrypt)).thenReturn(true);

            assertThatThrownBy(() -> authService.login("inactivo", "clave"))
                    .isInstanceOf(ResponseStatusException.class)
                    .satisfies(ex -> {
                        ResponseStatusException response = (ResponseStatusException) ex;
                        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
                        assertThat(response.getReason()).isEqualTo("credenciales invalidas");
                    });
        }
    }

    private Usuario usuarioActivo(String username, String password) {
        return new Usuario("Usuario Test", username, password, username + "@mail.test", Rol.ADMINISTRADOR);
    }
}
