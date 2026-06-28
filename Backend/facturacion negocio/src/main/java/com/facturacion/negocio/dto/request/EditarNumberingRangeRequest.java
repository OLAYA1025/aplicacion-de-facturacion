package com.facturacion.negocio.dto.request;

public record EditarNumberingRangeRequest(
        Integer providerRangeId,
        String prefix,
        Long fromNumber,
        Long toNumber,
        Long currentNumber,
        String resolutionNumber,
        String technicalKey,
        Boolean active
) {
}