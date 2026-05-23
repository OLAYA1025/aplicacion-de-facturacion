package com.facturacion.negocio.domain;

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseTenantEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "negocio_id", nullable = false)
    private Negocio negocio;

    public Negocio getNegocio() {
        return negocio;
    }

    public void setNegocio(Negocio negocio) {
        this.negocio = negocio;
    }

    protected void heredarNegocio(BaseTenantEntity entidadRelacionada) {
        if (entidadRelacionada == null) {
            return;
        }
        heredarNegocio(entidadRelacionada.getNegocio());
    }

    protected void heredarNegocio(Usuario usuario) {
        if (usuario == null) {
            return;
        }
        heredarNegocio(usuario.getNegocio());
    }

    protected void heredarNegocio(Negocio negocio) {
        if (negocio == null) {
            return;
        }
        if (this.negocio == null) {
            this.negocio = negocio;
            return;
        }

        Long negocioActualId = this.negocio.getId();
        Long negocioNuevoId = negocio.getId();
        if (negocioActualId != null && negocioNuevoId != null && !negocioActualId.equals(negocioNuevoId)) {
            throw new IllegalArgumentException("la relacion pertenece a otro negocio");
        }
    }
}
