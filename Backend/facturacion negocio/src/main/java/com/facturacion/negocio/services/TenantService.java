package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.BaseTenantEntity;
import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.repository.NegocioRepository;
import com.facturacion.negocio.security.TenantContext;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class TenantService {

    private final NegocioRepository negocioRepository;

    public TenantService(NegocioRepository negocioRepository) {
        this.negocioRepository = negocioRepository;
    }

    public Optional<Long> getNegocioIdActualOptional() {
        return Optional.ofNullable(TenantContext.getNegocioId());
    }

    public Long getNegocioIdActual() {
        return getNegocioIdActualOptional()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "tenant no resuelto"));
    }

    @Transactional(readOnly = true)
    public Negocio getNegocioActual() {
        Long negocioId = getNegocioIdActual();
        return negocioRepository.findById(negocioId)
                .filter(Negocio::isActivo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "negocio no valido"));
    }

    @Transactional(readOnly = true)
    public Negocio getNegocioPorDefecto() {
        return negocioRepository.findFirstByActivoTrueOrderByIdAsc()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        "no existe negocio por defecto"
                ));
    }

    public <T extends BaseTenantEntity> T asignarNegocioActual(T entity) {
        if (entity == null) {
            return null;
        }
        Negocio negocioActual = getNegocioActual();
        if (entity.getNegocio() == null) {
            entity.setNegocio(negocioActual);
            return entity;
        }
        validarMismoNegocio(entity, negocioActual.getId());
        return entity;
    }

    public void validarMismoNegocio(BaseTenantEntity entity) {
        if (entity == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "registro no encontrado");
        }
        validarMismoNegocio(entity, getNegocioIdActual());
    }

    private void validarMismoNegocio(BaseTenantEntity entity, Long negocioIdEsperado) {
        if (entity.getNegocio() == null || entity.getNegocio().getId() == null) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "registro sin negocio asociado");
        }
        if (!entity.getNegocio().getId().equals(negocioIdEsperado)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "registro no encontrado");
        }
    }
}
