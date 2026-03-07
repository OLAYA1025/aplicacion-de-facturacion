package com.facturacion.negocio.domain;

import com.facturacion.negocio.enumerated.TipoDocumento;
import jakarta.persistence.*;

@Entity
@Table(
        name = "clientes",
        indexes = {
                @Index(name = "idx_cliente_numero_id", columnList = "numero_identificacion")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_cliente_tipo_numero",
                        columnNames = {"tipo_identificacion", "numero_identificacion"}
                )
        }
)
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name="tipo_identificacion", nullable = false, length = 10)
    private TipoDocumento tipoIdentificacion; // "CC", "NIT", etc.

    @Column(name="numero_identificacion", nullable = false, length = 30)
    private String numeroIdentificacion; // NIT/CC

    @Column(name="nombre", nullable = false, length = 200)
    private String nombre; // Razón social o nombre

    @Column(name="email", length = 150)
    private String email;



    protected Cliente() {
    }
    public Cliente(TipoDocumento tipoIdentificacion, String numeroIdentificacion, String nombre, String email) {
        this.tipoIdentificacion = tipoIdentificacion;
        this.numeroIdentificacion = numeroIdentificacion;
        this.nombre = nombre;
        this.email = email;
    }

    // getters/setters
    public Long getId() { return id; }
    public TipoDocumento getTipoIdentificacion() { return tipoIdentificacion; }
    public void setTipoIdentificacion(TipoDocumento tipoIdentificacion) { this.tipoIdentificacion = tipoIdentificacion; }
    public String getNumeroIdentificacion() { return numeroIdentificacion; }
    public void setNumeroIdentificacion(String numeroIdentificacion) { this.numeroIdentificacion = numeroIdentificacion; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }


}