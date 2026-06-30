package com.facturacion.negocio.services.tirillaFactura;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
public class FacturaLogoService {

    private static final Logger log = LoggerFactory.getLogger(FacturaLogoService.class);
    private static final List<String> EXTENSIONES_PERMITIDAS = List.of("png", "jpg", "jpeg", "webp", "gif");

    private final Path logoDirectory;

    public FacturaLogoService(
            @Value("${tirilla.factura.logo-directory:${app.logos.directory:./logos}}") String logoDirectory
    ) {
        this.logoDirectory = Path.of(logoDirectory).toAbsolutePath().normalize();
    }

    public Optional<String> logoToBase64(Factura factura) {
        return resolverLogo(factura).flatMap(this::toDataUri);
    }

    private Optional<Path> resolverLogo(Factura factura) {
        if (factura == null) {
            return Optional.empty();
        }

        Optional<Path> logoConfigurado = resolverPorNombreArchivo(factura.getLogoArchivo());
        if (logoConfigurado.isPresent()) {
            return logoConfigurado;
        }

        Long negocioId = factura.getNegocioId();
        if (negocioId == null) {
            return Optional.empty();
        }

        for (String extension : EXTENSIONES_PERMITIDAS) {
            Path candidato = logoDirectory.resolve("negocio-" + negocioId + "." + extension).normalize();
            if (esArchivoDentroDelDirectorio(candidato)) {
                return Optional.of(candidato);
            }
        }
        return Optional.empty();
    }

    private Optional<Path> resolverPorNombreArchivo(String logoArchivo) {
        if (logoArchivo == null || logoArchivo.isBlank()) {
            return Optional.empty();
        }

        Path candidato = logoDirectory.resolve(Path.of(logoArchivo).getFileName()).normalize();
        if (!esArchivoDentroDelDirectorio(candidato)) {
            return Optional.empty();
        }
        return Optional.of(candidato);
    }

    private boolean esArchivoDentroDelDirectorio(Path path) {
        return path.startsWith(logoDirectory) && Files.isRegularFile(path);
    }

    private Optional<String> toDataUri(Path logoPath) {
        try {
            String contentType = Files.probeContentType(logoPath);
            if (contentType == null || !contentType.startsWith("image/")) {
                contentType = contentTypeSegunExtension(logoPath);
            }

            byte[] bytes = Files.readAllBytes(logoPath);
            return Optional.of("data:" + contentType + ";base64," + Base64.getEncoder().encodeToString(bytes));
        } catch (IOException e) {
            log.warn("No se pudo leer el logo de factura {}", logoPath, e);
            return Optional.empty();
        }
    }

    private String contentTypeSegunExtension(Path logoPath) {
        String nombre = logoPath.getFileName().toString().toLowerCase();
        if (nombre.endsWith(".jpg") || nombre.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (nombre.endsWith(".webp")) {
            return "image/webp";
        }
        if (nombre.endsWith(".gif")) {
            return "image/gif";
        }
        return "image/png";
    }
}
