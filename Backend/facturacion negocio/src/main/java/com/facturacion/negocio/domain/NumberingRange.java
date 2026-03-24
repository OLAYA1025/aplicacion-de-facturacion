package com.facturacion.negocio.domain;

import com.facturacion.negocio.enumerated.ProveedorFacturacion;
import jakarta.persistence.*;


import java.time.Instant;

@Entity
@Table(name = "numbering_ranges")
public class NumberingRange {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProveedorFacturacion provider;

    // Para Factus, por ejemplo 8
    @Column(name = "provider_range_id")
    private Integer providerRangeId;

    @Column(nullable = false, length = 20)
    private String prefix;

    @Column(name = "from_number", nullable = false)
    private Long fromNumber;

    @Column(name = "to_number", nullable = false)
    private Long toNumber;

    // Ultimo usado
    @Column(name = "current_number", nullable = false)
    private Long currentNumber;

    @Column(name = "resolution_number", length = 50)
    private String resolutionNumber;

    @Column(name = "technical_key", length = 255)
    private String technicalKey;

    @Column(nullable = false)
    private Boolean active = true;

    protected NumberingRange() {
    }

    public Long takeNextNumber() {
        long next = this.currentNumber + 1;
        if (next > this.toNumber) {
            throw new IllegalStateException("Rango de numeracion agotado");
        }
        this.currentNumber = next;
        return next;
    }

    public String buildDocumentNumber(Long consecutive) {
        return this.prefix + consecutive;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ProveedorFacturacion getProvider() {
        return provider;
    }

    public void setProvider(ProveedorFacturacion provider) {
        this.provider = provider;
    }

    public Integer getProviderRangeId() {
        return providerRangeId;
    }

    public void setProviderRangeId(Integer providerRangeId) {
        this.providerRangeId = providerRangeId;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Long getFromNumber() {
        return fromNumber;
    }

    public void setFromNumber(Long fromNumber) {
        this.fromNumber = fromNumber;
    }

    public Long getToNumber() {
        return toNumber;
    }

    public void setToNumber(Long toNumber) {
        this.toNumber = toNumber;
    }

    public Long getCurrentNumber() {
        return currentNumber;
    }

    public void setCurrentNumber(Long currentNumber) {
        this.currentNumber = currentNumber;
    }

    public String getResolutionNumber() {
        return resolutionNumber;
    }

    public void setResolutionNumber(String resolutionNumber) {
        this.resolutionNumber = resolutionNumber;
    }

    public String getTechnicalKey() {
        return technicalKey;
    }

    public void setTechnicalKey(String technicalKey) {
        this.technicalKey = technicalKey;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}