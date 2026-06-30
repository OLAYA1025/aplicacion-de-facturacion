package com.facturacion.negocio.dto.response;

import com.facturacion.negocio.domain.NumberingRange;

public class NumberingRangeResponseDTO {

    private Long id;
    private Long configuracionId;
    private Integer providerRangeId;
    private String prefix;
    private Long fromNumber;
    private Long toNumber;
    private Long currentNumber;
    private String resolutionNumber;
    private String technicalKey;
    private Boolean active;

    public static NumberingRangeResponseDTO from(NumberingRange range) {
        NumberingRangeResponseDTO dto = new NumberingRangeResponseDTO();
        dto.id = range.getId();
        dto.configuracionId = range.getConfiguracion().getId();
        dto.providerRangeId = range.getProviderRangeId();
        dto.prefix = range.getPrefix();
        dto.fromNumber = range.getFromNumber();
        dto.toNumber = range.getToNumber();
        dto.currentNumber = range.getCurrentNumber();
        dto.resolutionNumber = range.getResolutionNumber();
        dto.technicalKey = range.getTechnicalKey();
        dto.active = range.getActive();
        return dto;
    }

    public Long getId() {
        return id;
    }

    public Long getConfiguracionId() {
        return configuracionId;
    }

    public Integer getProviderRangeId() {
        return providerRangeId;
    }

    public String getPrefix() {
        return prefix;
    }

    public Long getFromNumber() {
        return fromNumber;
    }

    public Long getToNumber() {
        return toNumber;
    }

    public Long getCurrentNumber() {
        return currentNumber;
    }

    public String getResolutionNumber() {
        return resolutionNumber;
    }

    public String getTechnicalKey() {
        return technicalKey;
    }

    public Boolean getActive() {
        return active;
    }
}
