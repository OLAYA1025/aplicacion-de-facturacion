package com.facturacion.negocio.domain;

import com.facturacion.negocio.enumerated.TipoGestionProducto;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.Set;

@Entity
public class Producto {

    private static final BigDecimal IVA_CERO = BigDecimal.ZERO;
    private static final BigDecimal IVA_DIECINUEVE = BigDecimal.valueOf(19);
    private static final Set<Integer> IVA_PERMITIDOS = Set.of(0, 5, 19);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long codigo;


    @Column(nullable = false, unique = true)

    private String nombre;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(nullable = false)
    private Boolean activo;

    @Enumerated(EnumType.STRING)
    @Column
    private TipoGestionProducto tipoGestion;

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal ivaPorcentaje = IVA_DIECINUEVE;

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
        this(nombre, precio, descripcion, categoria, TipoGestionProducto.PREPARACION, null, null, null);
    }

    public Producto(String nombre,
                    BigDecimal precio,
                    String descripcion,
                    Categoria categoria,
                    TipoGestionProducto tipoGestion,
                    BigDecimal ivaPorcentaje,
                    Integer stockInicial,
                    Integer stockMinimo) {
        this.nombre = nombre;
        this.precio = precio;
        this.activo = true;
        this.descripcion = descripcion;
        this.categoria = Objects.requireNonNull(categoria, "categoria requerida");
        this.tipoGestion = tipoGestion != null ? tipoGestion : TipoGestionProducto.PREPARACION;
        aplicarReglasIva(ivaPorcentaje);
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
        actualizarProducto(nombre, precio, descripcion, categoria, this.tipoGestion, this.ivaPorcentaje, this.stockMinimo);
    }

    public void actualizarProducto(String nombre,
                                   BigDecimal precio,
                                   String descripcion,
                                   Categoria categoria,
                                   TipoGestionProducto tipoGestion,
                                   BigDecimal ivaPorcentaje,
                                   Integer stockMinimo) {
        TipoGestionProducto gestionNueva = tipoGestion != null ? tipoGestion : this.tipoGestion;
        this.nombre = nombre;
        this.precio = precio;
        this.descripcion = descripcion;
        this.categoria = Objects.requireNonNull(categoria, "categoria requerida");
        this.tipoGestion = Objects.requireNonNull(gestionNueva, "tipoGestion requerido");
        aplicarReglasIva(ivaPorcentaje);
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

    public void cambiarNombre(String nombre) {
        this.nombre = nombre;
    }

    public BigDecimal getPrecio() {
        return precio;
    }

    public Long getCodigo() {
        return codigo;
    }

    public void setCodigo(Long codigo) {
        this.codigo = codigo;
    }

    public TipoGestionProducto getTipoGestion() {
        return tipoGestion;
    }

    public BigDecimal getIvaPorcentaje() {
        return ivaPorcentaje;
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

    @PrePersist
    @PreUpdate
    private void sincronizarDatosTributarios() {
        if (this.tipoGestion == null) {
            this.tipoGestion = TipoGestionProducto.PREPARACION;
        }
        aplicarReglasIva(this.ivaPorcentaje);
    }

    private void aplicarReglasIva(BigDecimal ivaSolicitado) {
        if (this.tipoGestion == TipoGestionProducto.PREPARACION) {
            this.ivaPorcentaje = IVA_DIECINUEVE;
            this.isExcluded = false;
            return;
        }

        BigDecimal ivaBase = ivaSolicitado != null
                ? ivaSolicitado
                : (this.ivaPorcentaje != null ? this.ivaPorcentaje : IVA_DIECINUEVE);
        BigDecimal ivaNormalizado = validarIvaPermitido(ivaBase);
        this.ivaPorcentaje = ivaNormalizado;
        this.isExcluded = ivaNormalizado.compareTo(IVA_CERO) == 0;
    }

    private BigDecimal validarIvaPermitido(BigDecimal ivaSolicitado) {
        if (ivaSolicitado == null) {
            throw new IllegalArgumentException("ivaPorcentaje no puede ser null");
        }

        int ivaEntero;
        try {
            ivaEntero = ivaSolicitado.stripTrailingZeros().intValueExact();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException("ivaPorcentaje invalido. Valores permitidos: 0, 5, 19");
        }

        if (!IVA_PERMITIDOS.contains(ivaEntero)) {
            throw new IllegalArgumentException("ivaPorcentaje invalido. Valores permitidos: 0, 5, 19");
        }

        return BigDecimal.valueOf(ivaEntero);
    }

}
