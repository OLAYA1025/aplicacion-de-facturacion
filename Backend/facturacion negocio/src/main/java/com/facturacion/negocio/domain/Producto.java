package com.facturacion.negocio.domain;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

import com.facturacion.negocio.enumerated.TipoGestionProducto;
import com.facturacion.negocio.enumerated.TipoImpuesto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_producto_negocio_nombre", columnNames = {"negocio_id", "nombre"})
        }
)
public class Producto extends BaseTenantEntity {

    private static final BigDecimal IVA_CERO = BigDecimal.ZERO;
    private static final BigDecimal IVA_DIECINUEVE = BigDecimal.valueOf(19);
    private static final Set<Integer> IVA_PERMITIDOS = Set.of(0, 5, 19);
    private static final BigDecimal INC_OCHO = BigDecimal.valueOf(8);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 100)
    private String codigo;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private Boolean activo;

    @Enumerated(EnumType.STRING)
    @Column
    private TipoGestionProducto tipoGestion;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_impuesto", nullable = false, length = 10)
    private TipoImpuesto tipoImpuesto;

    @Column(name = "impuesto_porcentaje", nullable = false, precision = 5, scale = 2)
    private BigDecimal impuestoPorcentaje;

    @Column(nullable = false)
    private Boolean isExcluded = false;

    private Integer stockActual;

    private Integer stockMinimo;

    private Integer stockReservado;

    @Column(length = 255)
    private String descripcion;

    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    protected Producto() {
    }

    public Producto(String nombre, BigDecimal precio, String descripcion, Categoria categoria) {
        this(nombre, precio, descripcion, categoria, TipoGestionProducto.PREPARACION, null, null, null, null);
    }

    public Producto(String nombre,
                    BigDecimal precio,
                    String descripcion,
                    Categoria categoria,
                    TipoGestionProducto tipoGestion,
                    TipoImpuesto tipoImpuesto,
                    BigDecimal impuestoPorcentaje,
                    Integer stockInicial,
                    Integer stockMinimo) {
        this.nombre = nombre;
        this.precio = precio;
        this.activo = true;
        this.descripcion = descripcion;
        this.categoria = Objects.requireNonNull(categoria, "categoria requerida");
        heredarNegocio(categoria);
        this.tipoGestion = tipoGestion != null ? tipoGestion : TipoGestionProducto.PREPARACION;
        aplicarReglasImpuesto(tipoImpuesto, impuestoPorcentaje);
        this.stockActual = null;
        this.stockMinimo = null;
        this.stockReservado = null;
        if (this.tipoGestion == TipoGestionProducto.INVENTARIABLE) {
            this.stockActual = validarStockNoNegativo(stockInicial, "stockInicial");
            this.stockMinimo = validarStockNoNegativo(stockMinimo, "stockMinimo");
            this.stockReservado = 0;
        }
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = normalizarCodigo(codigo);
    }

    public Boolean isActivo() {
        return activo;
    }

    public void activar() {
        this.activo = true;
    }

    public void desactivar() {
        this.activo = false;
    }

    public String getNombre() {
        return nombre;
    }

    public void cambiarPrecio(BigDecimal nuevoPrecio) {
        if (nuevoPrecio.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio debe ser mayor a cero");
        }
        this.precio = nuevoPrecio;
    }

    public void actualizarProducto(String nombre, BigDecimal precio, String descripcion, Categoria categoria) {
        actualizarProducto(
                nombre,
                precio,
                descripcion,
                categoria,
                this.tipoGestion,
                this.tipoImpuesto,
                this.impuestoPorcentaje,
                this.stockMinimo
        );
    }

    public void actualizarProducto(String nombre,
                                   BigDecimal precio,
                                   String descripcion,
                                   Categoria categoria,
                                   TipoGestionProducto tipoGestion,
                                   TipoImpuesto tipoImpuesto,
                                   BigDecimal impuestoPorcentaje,
                                   Integer stockMinimo) {
        TipoGestionProducto gestionNueva = tipoGestion != null ? tipoGestion : this.tipoGestion;
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.categoria = Objects.requireNonNull(categoria, "categoria requerida");
        heredarNegocio(categoria);
        this.tipoGestion = Objects.requireNonNull(gestionNueva, "tipoGestion requerido");
        aplicarReglasImpuesto(tipoImpuesto, impuestoPorcentaje);
        if (this.tipoGestion == TipoGestionProducto.INVENTARIABLE) {
            if (this.stockActual == null) {
                this.stockActual = 0;
            }
            if (this.stockReservado == null) {
                this.stockReservado = 0;
            }
            this.stockMinimo = validarStockNoNegativo(
                    stockMinimo != null ? stockMinimo : this.stockMinimo,
                    "stockMinimo"
            );
            if (this.stockMinimo == null) {
                this.stockMinimo = 0;
            }
            return;
        }
        this.stockActual = null;
        this.stockMinimo = null;
        this.stockReservado = null;
    }

    public void actualizarCodigo(String codigo) {
        this.codigo = normalizarCodigo(codigo);
    }

    public void cambiarNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }



    public TipoGestionProducto getTipoGestion() {
        return tipoGestion;
    }

    public TipoImpuesto getTipoImpuesto() {
        return tipoImpuesto;
    }

    public BigDecimal getImpuestoPorcentaje() {
        return impuestoPorcentaje;
    }

    @Deprecated
    public BigDecimal getIvaPorcentaje() {
        return impuestoPorcentaje;
    }

    public Boolean getIsExcluded() {
        return isExcluded;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public Integer getStockActual() {
        return stockActual;
    }

    public Integer getStockMinimo() {
        return stockMinimo;
    }

    public Integer getStockReservado() {
        return stockReservado;
    }

    public Integer getStockDisponible() {
        if (!esInventariable()) {
            return null;
        }
        int actual = this.stockActual != null ? this.stockActual : 0;
        int reservado = this.stockReservado != null ? this.stockReservado : 0;
        return actual - reservado;
    }

    public boolean esInventariable() {
        return this.tipoGestion == TipoGestionProducto.INVENTARIABLE;
    }

    public void aumentarStock(int cantidad) {
        if (!esInventariable()) {
            throw new IllegalStateException("El producto no maneja inventario");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("cantidad invalida");
        }
        if (this.stockActual == null) {
            this.stockActual = 0;
        }
        if (this.stockReservado == null) {
            this.stockReservado = 0;
        }
        this.stockActual += cantidad;
    }

    public void disminuirStock(int cantidad) {
        if (!esInventariable()) {
            throw new IllegalStateException("El producto no maneja inventario");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("cantidad invalida");
        }
        int actual = this.stockActual != null ? this.stockActual : 0;
        int reservado = this.stockReservado != null ? this.stockReservado : 0;
        int disponible = actual - reservado;
        if (disponible < cantidad) {
            throw new IllegalStateException("Stock insuficiente para el producto " + this.nombre);
        }
        this.stockActual = actual - cantidad;
    }

    public void reservarStock(int cantidad) {
        if (!esInventariable()) {
            throw new IllegalStateException("El producto no maneja inventario");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("cantidad invalida");
        }
        int actual = this.stockActual != null ? this.stockActual : 0;
        int reservado = this.stockReservado != null ? this.stockReservado : 0;
        int disponible = actual - reservado;
        if (disponible < cantidad) {
            throw new IllegalStateException("Stock disponible insuficiente para el producto " + this.nombre);
        }
        this.stockReservado = reservado + cantidad;
    }

    public void liberarStockReservado(int cantidad) {
        if (!esInventariable()) {
            throw new IllegalStateException("El producto no maneja inventario");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("cantidad invalida");
        }
        int reservado = this.stockReservado != null ? this.stockReservado : 0;
        if (reservado < cantidad) {
            throw new IllegalStateException("No hay stock reservado suficiente para el producto " + this.nombre);
        }
        this.stockReservado = reservado - cantidad;
    }

    public void consumirStockReservado(int cantidad) {
        if (!esInventariable()) {
            throw new IllegalStateException("El producto no maneja inventario");
        }
        if (cantidad <= 0) {
            throw new IllegalArgumentException("cantidad invalida");
        }
        int actual = this.stockActual != null ? this.stockActual : 0;
        int reservado = this.stockReservado != null ? this.stockReservado : 0;
        if (reservado < cantidad) {
            throw new IllegalStateException("No hay stock reservado suficiente para el producto " + this.nombre);
        }
        if (actual < cantidad) {
            throw new IllegalStateException("Stock insuficiente para el producto " + this.nombre);
        }
        this.stockActual = actual - cantidad;
        this.stockReservado = reservado - cantidad;
    }

    private Integer validarStockNoNegativo(Integer valor, String campo) {
        if (valor == null) {
            return 0;
        }
        if (valor < 0) {
            throw new IllegalArgumentException(campo + " no puede ser negativo");
        }
        return valor;
    }

    private String normalizarCodigo(String codigo) {
        if (codigo == null) {
            return null;
        }

        String codigoNormalizado = codigo.trim();
        return codigoNormalizado.isEmpty() ? null : codigoNormalizado;
    }

    @PrePersist
    @PreUpdate
    private void sincronizarDatosTributarios() {
        this.codigo = normalizarCodigo(this.codigo);
        if (this.tipoGestion == null) {
            this.tipoGestion = TipoGestionProducto.PREPARACION;
        }
        aplicarReglasImpuesto(this.tipoImpuesto, this.impuestoPorcentaje);
    }

    private void aplicarReglasImpuesto(TipoImpuesto tipoImpuestoSolicitado, BigDecimal impuestoPorcentajeSolicitado) {
        if (this.tipoGestion == TipoGestionProducto.PREPARACION) {
            this.tipoImpuesto = TipoImpuesto.INC;
            this.impuestoPorcentaje = INC_OCHO;
            this.isExcluded = false;
            return;
        }

        TipoImpuesto tipoNormalizado = tipoImpuestoSolicitado != null
                ? tipoImpuestoSolicitado
                : (this.tipoImpuesto != null ? this.tipoImpuesto : TipoImpuesto.IVA);

        if (tipoNormalizado == TipoImpuesto.INC) {
            this.tipoImpuesto = TipoImpuesto.INC;
            this.impuestoPorcentaje = validarIncPermitido(impuestoPorcentajeSolicitado);
            this.isExcluded = false;
            return;
        }

        BigDecimal porcentajeBase = impuestoPorcentajeSolicitado != null
                ? impuestoPorcentajeSolicitado
                : (this.tipoImpuesto == TipoImpuesto.IVA && this.impuestoPorcentaje != null
                ? this.impuestoPorcentaje
                : IVA_DIECINUEVE);

        BigDecimal ivaNormalizado = validarIvaPermitido(porcentajeBase);
        this.tipoImpuesto = TipoImpuesto.IVA;
        this.impuestoPorcentaje = ivaNormalizado;
        this.isExcluded = ivaNormalizado.compareTo(IVA_CERO) == 0;
    }

    private BigDecimal validarIncPermitido(BigDecimal porcentajeSolicitado) {
        if (porcentajeSolicitado == null) {
            return INC_OCHO;
        }
        int porcentajeEntero;
        try {
            porcentajeEntero = porcentajeSolicitado.stripTrailingZeros().intValueExact();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("impuestoPorcentaje invalido. Para INC el valor permitido es 8");
        }
        if (porcentajeEntero != 8) {
            throw new IllegalArgumentException("impuestoPorcentaje invalido. Para INC el valor permitido es 8");
        }
        return INC_OCHO;
    }

    private BigDecimal validarIvaPermitido(BigDecimal porcentajeSolicitado) {
        if (porcentajeSolicitado == null) {
            throw new IllegalArgumentException("impuestoPorcentaje no puede ser null para IVA");
        }

        int porcentajeEntero;
        try {
            porcentajeEntero = porcentajeSolicitado.stripTrailingZeros().intValueExact();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("impuestoPorcentaje invalido. Para IVA los valores permitidos son: 0, 5, 19");
        }

        if (!IVA_PERMITIDOS.contains(porcentajeEntero)) {
            throw new IllegalArgumentException("impuestoPorcentaje invalido. Para IVA los valores permitidos son: 0, 5, 19");
        }

        return BigDecimal.valueOf(porcentajeEntero);
    }
}
