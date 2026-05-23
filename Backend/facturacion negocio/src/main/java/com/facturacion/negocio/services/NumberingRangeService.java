package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.NumberingRange;
import com.facturacion.negocio.dto.factura.NumberAllocationResult;
import com.facturacion.negocio.repository.NumberingRangeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NumberingRangeService {

    private final NumberingRangeRepository numberingRangeRepository;
    private final TenantService tenantService;

    public NumberingRangeService(NumberingRangeRepository numberingRangeRepository,
                                 TenantService tenantService) {
        this.numberingRangeRepository = numberingRangeRepository;
        this.tenantService = tenantService;
    }

    @Transactional
    public NumberAllocationResult allocateNext(Long rangeId) {
        NumberingRange range = tenantService.getNegocioIdActualOptional()
                .map(negocioId -> numberingRangeRepository.findByIdForUpdateAndNegocioId(rangeId, negocioId)
                        .orElseThrow(() -> new IllegalArgumentException("Rango no encontrado: " + rangeId)))
                .orElseGet(() -> numberingRangeRepository.findByIdForUpdate(rangeId)
                        .orElseThrow(() -> new IllegalArgumentException("Rango no encontrado: " + rangeId)));

        if (!Boolean.TRUE.equals(range.getActive())) {
            throw new IllegalStateException("El rango no esta activo");
        }

        Long consecutive = range.takeNextNumber();
        String documentNumber = range.buildDocumentNumber(consecutive);

        return new NumberAllocationResult(
                range.getId(),
                range.getPrefix(),
                consecutive,
                documentNumber,
                range.getResolutionNumber(),
                range.getTechnicalKey()
        );
    }

    /*
    public NumberingRange crear(){
      //TODO
      implementar despues
    }

     */
}
