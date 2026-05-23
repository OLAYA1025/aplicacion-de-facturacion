package com.facturacion.negocio.domain;

import com.facturacion.negocio.enumerated.TipoDocumento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.util.Objects;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_proveedor_negocio_correo", columnNames = {"negocio_id", "correo"})
        }
)
public class Proveedor extends BaseTenantEntity {

    @Id
    @Column(nullable = false)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 30)
    private String telefono;

    @Column(length = 150)
    private String correo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_documento", nullable = false)
    private TipoDocumento tipoDocumento;

    protected Proveedor() {
    }

    public Proveedor(Long id, String nombre, String telefono, String correo, TipoDocumento tipoDocumento) {
        this.id = Objects.requireNonNull(id, "id requerido");
        this.nombre = Objects.requireNonNull(nombre, "nombre requerido");
        this.telefono = telefono;
        this.correo = correo;
        this.tipoDocumento = Objects.requireNonNull(tipoDocumento, "tipoDocumento requerido");
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = Objects.requireNonNull(id, "id requerido");
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = Objects.requireNonNull(nombre, "nombre requerido");
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public TipoDocumento getTipoDocumento() {
        return tipoDocumento;
    }

    public void setTipoDocumento(TipoDocumento tipoDocumento) {
        this.tipoDocumento = Objects.requireNonNull(tipoDocumento, "tipoDocumento requerido");
    }

    public void actualizar(String nombre, String telefono, String correo, TipoDocumento tipoDocumento) {
        this.nombre = Objects.requireNonNull(nombre, "nombre requerido");
        this.telefono = telefono;
        this.correo = correo;
        this.tipoDocumento = Objects.requireNonNull(tipoDocumento, "tipoDocumento requerido");
    }
}
