package com.facturacion.negocio.dto.factura;

public record NumberAllocationResult(
        Long rangeId,
        String prefix,
        Long consecutive,
        String documentNumber,
        String resolutionNumber,
        String technicalKey
) {
}