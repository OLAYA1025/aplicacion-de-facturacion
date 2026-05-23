package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.Cliente;
import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.enumerated.TipoDocumento;
import com.facturacion.negocio.repository.ClienteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final TenantService tenantService;

    public ClienteService(ClienteRepository clienteRepository, TenantService tenantService) {
        this.clienteRepository = clienteRepository;
        this.tenantService = tenantService;
    }

    @Transactional(readOnly = true)
    public Cliente obtenerPorTipoYNumero(String tipoIdentificacion, String numeroIdentificacion) {
        TipoDocumento tipo = normalizarTipoIdentificacion(tipoIdentificacion);
        String numero = normalizarNumeroIdentificacion(numeroIdentificacion);
        Negocio negocio = tenantService.getNegocioActual();

        return clienteRepository.findByTipoIdentificacionAndNumeroIdentificacionAndNegocio(tipo, numero, negocio)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado"));
    }

    @Transactional
    public Cliente editarPorTipoYNumero(String tipoIdentificacion,
                                        String numeroIdentificacion,
                                        String nombre,
                                        String email) {
        Cliente cliente = obtenerPorTipoYNumero(tipoIdentificacion, numeroIdentificacion);

        cliente.setNombre(normalizarNombre(nombre));
        cliente.setEmail(normalizarEmail(email));

        return clienteRepository.save(cliente);
    }

    private TipoDocumento normalizarTipoIdentificacion(String tipoIdentificacion) {
        if (tipoIdentificacion == null || tipoIdentificacion.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tipoIdentificacion invalido");
        }

        try {
            return TipoDocumento.valueOf(tipoIdentificacion.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "tipoIdentificacion invalido");
        }
    }

    private String normalizarNumeroIdentificacion(String numeroIdentificacion) {
        if (numeroIdentificacion == null || numeroIdentificacion.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "numeroIdentificacion invalido");
        }
        return numeroIdentificacion.trim();
    }

    private String normalizarNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "nombre cliente invalido");
        }
        return nombre.trim();
    }

    private String normalizarEmail(String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        return email.trim();
    }
}
