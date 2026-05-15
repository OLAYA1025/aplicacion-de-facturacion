package com.facturacion.negocio.domain;

import jakarta.persistence.*;

@Entity
@Table(
        name = "nota_predefinida",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_nota_predefinida_descripcion", columnNames = {"grupo_id", "descripcion"})
        }
)
public class NotaPredefinida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 180)
    private String descripcion;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_id", nullable = false)
    private GrupoNotaCategoria grupo;

    protected NotaPredefinida() {
    }

    public NotaPredefinida(String descripcion, GrupoNotaCategoria grupo) {
        setDescripcion(descripcion);
        setGrupo(grupo);
    }

    public Long getId() {
        return id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public GrupoNotaCategoria getGrupo() {
        return grupo;
    }

    public void setDescripcion(String descripcion) {
        if (descripcion == null || descripcion.isBlank()) {
            throw new IllegalArgumentException("descripcion de nota invalida");
        }
        this.descripcion = descripcion.trim();
    }

    public void setGrupo(GrupoNotaCategoria grupo) {
        this.grupo = grupo;
    }
}
