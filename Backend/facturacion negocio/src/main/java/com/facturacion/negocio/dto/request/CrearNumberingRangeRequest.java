package com.facturacion.negocio.dto.request;

public record CrearNumberingRangeRequest(
        Long configuracionId,
        Integer providerRangeId,
        String prefix,
        Long fromNumber,
        Long toNumber,
        Long currentNumber,
        String resolutionNumber,
        String technicalKey
) {
}