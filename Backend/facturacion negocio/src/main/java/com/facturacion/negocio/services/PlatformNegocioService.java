package com.facturacion.negocio.services;

import java.util.List;

import com.facturacion.negocio.domain.ConfiguracionFacturacionElectronica;
import com.facturacion.negocio.domain.NumberingRange;
import com.facturacion.negocio.dto.request.CrearConfiguracionFeRequest;
import com.facturacion.negocio.dto.request.CrearNumberingRangeRequest;
import com.facturacion.negocio.dto.request.EditarConfiguracionFeRequest;
import com.facturacion.negocio.dto.request.EditarNumberingRangeRequest;
import com.facturacion.negocio.dto.response.ConfiguracionFacturacionElectronicaResponseDTO;
import com.facturacion.negocio.enumerated.ProveedorFacturacion;
import com.facturacion.negocio.repository.ConfiguracionFacturacionElectronicaRepository;
import com.facturacion.negocio.repository.NumberingRangeRepository;
import com.facturacion.negocio.security.EncryptionService;
import com.facturacion.negocio.utils.Normalizador;
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
    private final ConfiguracionFacturacionElectronicaRepository configuracionRepository;
    private final NumberingRangeRepository numberingRangeRepository;
    private final EncryptionService encryptionService;

    public PlatformNegocioService(NegocioRepository negocioRepository,
                                  UsuarioService usuarioService,
                                  ConfiguracionFacturacionElectronicaRepository configuracionRepository,
                                  NumberingRangeRepository numberingRangeRepository,
                                  EncryptionService encryptionService) {
        this.negocioRepository = negocioRepository;
        this.usuarioService = usuarioService;
        this.configuracionRepository = configuracionRepository;
        this.numberingRangeRepository = numberingRangeRepository;
        this.encryptionService = encryptionService;

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

    @Transactional(readOnly = true)
    public ConfiguracionFacturacionElectronicaResponseDTO obtenerConfiguracionFacturacionElectronica(
            Long negocioId
    ) {
        obtenerNegocio(negocioId);

        return configuracionRepository.findByNegocioId(negocioId)
                .map(configuracion -> ConfiguracionFacturacionElectronicaResponseDTO.from(
                        configuracion,
                        numberingRangeRepository.findByConfiguracionIdOrderByIdAsc(configuracion.getId()),
                        encryptionService
                ))
                .orElse(null);
    }

    @Transactional
    public Long crearConfiguracion(
            Long negocioId,
            ProveedorFacturacion proveedor,
            String baseUrl,
            String username,
            String password,
            String clientId,
            String clientSecret,
            String prefijoCode
    ) {

        Negocio negocio = negocioRepository.findById(negocioId)
                .orElseThrow(() -> new IllegalArgumentException("Negocio no existe"));





        if (configuracionRepository.findByNegocioId(negocioId).isPresent()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El negocio ya tiene configuracion de facturacion electronica"
            );
        }

        ConfiguracionFacturacionElectronica config = new ConfiguracionFacturacionElectronica(
                proveedor,
                baseUrl,
                username,
                password,
                clientId,
                clientSecret,
                prefijoCode
        );

        config.cifrarCredenciales(encryptionService);
        config.setNegocio(negocio);
        negocio.habilitarFacturacion();

        configuracionRepository.save(config);

        return config.getId();
    }

    @Transactional
    public void editarConfiguracionFacturacionElectronica(
            Long configuracionId,
            EditarConfiguracionFeRequest request
    ) {

        ConfiguracionFacturacionElectronica configuracion =
                configuracionRepository
                        .findById(configuracionId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "La configuracion no existe"
                                )
                        );

        configuracion.actualizar(
                encryptionService,
                request.proveedor(),
                request.baseUrl(),
                request.username(),
                request.password(),
                request.clientId(),
                request.clientSecret(),
                request.prefijoCode()
        );
    }

    @Transactional
    public Long crearNumberingRange(
            CrearNumberingRangeRequest request
    ) {

        ConfiguracionFacturacionElectronica configuracion =
                configuracionRepository.findById(
                        request.configuracionId()
                ).orElseThrow(() ->
                        new IllegalArgumentException(
                                "La configuración no existe"
                        )
                );

        NumberingRange range =
                new NumberingRange(
                        request.providerRangeId(),
                        request.prefix(),
                        request.fromNumber(),
                        request.toNumber(),
                        request.currentNumber(),
                        request.resolutionNumber(),
                        request.technicalKey(),
                        configuracion
                );

        configuracion.addRange(range);

        numberingRangeRepository.save(range);

        return range.getId();
    }

    @Transactional
    public void editarNumberingRange(
            Long rangeId,
            EditarNumberingRangeRequest request
    ) {

        NumberingRange range =
                numberingRangeRepository
                        .findById(rangeId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "El rango no existe"
                                )
                        );

        range.actualizar(
                request.providerRangeId(),
                request.prefix(),
                request.fromNumber(),
                request.toNumber(),
                request.currentNumber(),
                request.resolutionNumber(),
                request.technicalKey(),
                request.active()
        );
    }


    public Negocio configurarLogo(Long negocioId, String logoArchivo) {
        if (negocioId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "negocioId inválido");
        }

        if (logoArchivo == null || logoArchivo.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nombre de archivo inválido");
        }

        Negocio negocio = negocioRepository.findById(negocioId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Negocio no encontrado"));

        negocio.setLogoArchivo(Normalizador.normalizar(logoArchivo));

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
