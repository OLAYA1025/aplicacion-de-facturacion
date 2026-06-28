package com.facturacion.negocio.domain;

import jakarta.persistence.*;
import com.facturacion.negocio.utils.Normalizador;


import java.util.Objects;

@Entity
@Table(name = "numbering_ranges")
public class NumberingRange extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    /*
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProveedorFacturacion provider;
     */
    // Para Factus, por ejemplo 8
    @Column(name = "provider_range_id")
    private Integer providerRangeId;

    @Column(nullable = false, length = 20)
    private String prefix;

    @Column(name = "from_number", nullable = false)
    private Long fromNumber;

    @Column(name = "to_number", nullable = false)
    private Long toNumber;

    /*
    este hace de cuenta al proximo numero a usar,
    si anteriormente en una factura se uso el 10,
    el currentNumber seria igual a 11
    */
    @Column(name = "current_number", nullable = false)
    private Long currentNumber;

    @Column(name = "resolution_number", length = 50)
    private String resolutionNumber;

    @Column(name = "technical_key", length = 255)
    private String technicalKey;

    @Column(nullable = false)
    private Boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "configuracion_fe_id")
    private ConfiguracionFacturacionElectronica configuracion;

    protected NumberingRange() {
    }

    public NumberingRange(Integer providerRangeId,
                          String prefix,
                          Long fromNumber,
                          Long toNumber,
                          Long currentNumber,
                          String resolutionNumber,
                          String technicalKey,
                          ConfiguracionFacturacionElectronica configuracion){
        if((fromNumber == null || toNumber==null || currentNumber==null) ||
                (fromNumber<0 || toNumber<0 || currentNumber<0)){
            throw new IllegalArgumentException("ingrese rangos de numeracion validos");
        }

        if( currentNumber < fromNumber
                || currentNumber > toNumber){
            throw new IllegalArgumentException("el current number no esta entre el rango de numeracion");
        }

        this.providerRangeId = providerRangeId;
        this.prefix = Normalizador.normalizar(prefix);
        this.fromNumber = fromNumber;
        this.toNumber = toNumber;
        this.currentNumber = currentNumber;
        this.resolutionNumber = Normalizador.normalizar(resolutionNumber);
        this.technicalKey = Normalizador.normalizar(technicalKey);
        this.configuracion = Objects.requireNonNull(
                configuracion,
                "la configuracion es obligatoria"
        );;
        heredarNegocio(configuracion);
    }

    public void setConfiguracion(ConfiguracionFacturacionElectronica configuracion) {
        this.configuracion = configuracion;
        heredarNegocio(configuracion);
    }


    public Long takeNextNumber() {
        long current = this.currentNumber;
        if (currentNumber > this.toNumber) {
            throw new IllegalStateException("Rango de numeracion agotado");
        }

        this.currentNumber = this.currentNumber + 1;
        return current;
    }

    public String buildDocumentNumber(Long consecutive) {
        return this.prefix + consecutive;
    }

    public void actualizar(
            Integer providerRangeId,
            String prefix,
            Long fromNumber,
            Long toNumber,
            Long currentNumber,
            String resolutionNumber,
            String technicalKey,
            Boolean active
    ) {

        if (fromNumber == null
                || toNumber == null
                || currentNumber == null) {

            throw new IllegalArgumentException(
                    "ingrese rangos de numeracion validos"
            );
        }

        if (fromNumber < 0
                || toNumber < 0
                || currentNumber < 0) {

            throw new IllegalArgumentException(
                    "los numeros no pueden ser negativos"
            );
        }

        if (fromNumber > toNumber) {
            throw new IllegalArgumentException(
                    "el rango de numeracion es invalido"
            );
        }

        if (currentNumber < fromNumber
                || currentNumber > toNumber) {

            throw new IllegalArgumentException(
                    "current number fuera del rango"
            );
        }

        if (currentNumber < this.currentNumber) {
            throw new IllegalArgumentException(
                    "No se puede retroceder la numeracion"
            );
        }

        this.providerRangeId = providerRangeId;
        this.prefix = Normalizador.normalizar(prefix);
        this.fromNumber = fromNumber;
        this.toNumber = toNumber;
        this.currentNumber = currentNumber;
        this.resolutionNumber =
                Normalizador.normalizar(
                        resolutionNumber
                );
        this.technicalKey =
                Normalizador.normalizar(
                        technicalKey
                );

        if (active != null) {
            this.active = active;
        }
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    /*
    public ProveedorFacturacion getProvider() {
        return provider;
    }

    public void setProvider(ProveedorFacturacion provider) {
        this.provider = provider;
    }

     */

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

    public ConfiguracionFacturacionElectronica getConfiguracion() {
        return configuracion;
    }
}
