package com.facturacion.negocio.services;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.domain.Usuario;
import com.facturacion.negocio.repository.NegocioRepository;

@Service
public class PlatformNegocioService {

    private final NegocioRepository negocioRepository;
    private final UsuarioService usuarioService;

    public PlatformNegocioService(NegocioRepository negocioRepository, UsuarioService usuarioService) {
        this.negocioRepository = negocioRepository;
        this.usuarioService = usuarioService;
    }

    @Transactional
    public NegocioConAdministrador crearNegocioConAdministrador(String nombre,
                                                                String nit,
                                                                String direccion,
                                                                String telefono,
                                                                String adminNombre,
                                                                String adminUsername,
                                                                String adminPassword,
                                                                String adminEmail) {
        Negocio negocio = new Negocio(
                normalizarRequerido(nombre, "nombre negocio invalido"),
                normalizarOpcional(nit),
                normalizarOpcional(direccion),
                normalizarOpcional(telefono),
                true
        );
        Negocio negocioCreado = negocioRepository.save(negocio);
        Usuario administrador = usuarioService.crearAdministradorInicialNegocio(
                negocioCreado,
                adminNombre,
                adminUsername,
                adminPassword,
                adminEmail
        );
        return new NegocioConAdministrador(negocioCreado, administrador);
    }

    @Transactional(readOnly = true)
    public List<Negocio> listarNegocios() {
        return negocioRepository.findAllByOrderByIdAsc();
    }

    @Transactional
    public Negocio activarNegocio(Long id) {
        Negocio negocio = obtenerNegocio(id);
        negocio.activar();
        return negocioRepository.save(negocio);
    }

    @Transactional
    public Negocio desactivarNegocio(Long id) {
        Negocio negocio = obtenerNegocio(id);
        negocio.desactivar();
        return negocioRepository.save(negocio);
    }

    private Negocio obtenerNegocio(Long id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "negocioId invalido");
        }
        return negocioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "negocio no encontrado"));
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

    public record NegocioConAdministrador(Negocio negocio, Usuario administrador) {
    }
}
