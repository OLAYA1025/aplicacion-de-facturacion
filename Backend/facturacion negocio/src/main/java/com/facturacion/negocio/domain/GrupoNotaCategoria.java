package com.facturacion.negocio.domain;

import com.facturacion.negocio.enumerated.TipoSeleccionNota;
import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Entity
@Table(
        name = "grupo_nota_categoria",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_grupo_nota_categoria_nombre", columnNames = {"categoria_id", "nombre"})
        }
)
public class GrupoNotaCategoria extends BaseTenantEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false)
    private Boolean obligatorio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TipoSeleccionNota tipoSeleccion;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<NotaPredefinida> notas = new ArrayList<>();

    protected GrupoNotaCategoria() {
    }

    public GrupoNotaCategoria(String nombre,
                              Boolean obligatorio,
                              TipoSeleccionNota tipoSeleccion,
                              Categoria categoria) {
        setNombre(nombre);
        setObligatorio(obligatorio);
        setTipoSeleccion(tipoSeleccion);
        setCategoria(categoria);
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public Boolean getObligatorio() {
        return obligatorio;
    }

    public TipoSeleccionNota getTipoSeleccion() {
        return tipoSeleccion;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public List<NotaPredefinida> getNotas() {
        return Collections.unmodifiableList(notas);
    }

    public void actualizar(String nombre, Boolean obligatorio, TipoSeleccionNota tipoSeleccion) {
        setNombre(nombre);
        setObligatorio(obligatorio);
        setTipoSeleccion(tipoSeleccion);
    }

    public void setNombre(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("nombre del grupo invalido");
        }
        this.nombre = nombre.trim();
    }

    public void setObligatorio(Boolean obligatorio) {
        if (obligatorio == null) {
            throw new IllegalArgumentException("obligatorio es requerido");
        }
        this.obligatorio = obligatorio;
    }

    public void setTipoSeleccion(TipoSeleccionNota tipoSeleccion) {
        this.tipoSeleccion = Objects.requireNonNull(tipoSeleccion, "tipoSeleccion requerido");
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = Objects.requireNonNull(categoria, "categoria requerida");
        heredarNegocio(categoria);
    }

    public void agregarNota(NotaPredefinida nota) {
        Objects.requireNonNull(nota, "nota requerida");
        nota.setGrupo(this);
        this.notas.add(nota);
    }

    public void removerNota(NotaPredefinida nota) {
        if (nota == null) {
            return;
        }
        if (this.notas.remove(nota)) {
            nota.setGrupo(null);
        }
    }
}
