package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.Categoria;
import com.facturacion.negocio.domain.Compra;
import com.facturacion.negocio.domain.CompraDetalle;
import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.domain.Producto;
import com.facturacion.negocio.domain.Proveedor;
import com.facturacion.negocio.dto.request.DetalleCompraRequestDTO;
import com.facturacion.negocio.enumerated.TipoDocumento;
import com.facturacion.negocio.enumerated.TipoGestionProducto;
import com.facturacion.negocio.enumerated.TipoImpuesto;
import com.facturacion.negocio.repository.CompraRepository;
import com.facturacion.negocio.repository.MovimientoInventarioRepository;
import com.facturacion.negocio.repository.ProductoRepository;
import com.facturacion.negocio.repository.ProveedorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompraServiceTest {

    @Mock
    private CompraRepository compraRepository;
    @Mock
    private ProveedorRepository proveedorRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private MovimientoInventarioRepository movimientoInventarioRepository;
    @Mock
    private TenantService tenantService;

    private CompraService compraService;
    private Negocio negocio;

    @BeforeEach
    void setUp() {
        negocio = new Negocio("Negocio Test", null, null, null, true);
        when(tenantService.getNegocioActual()).thenReturn(negocio);
        compraService = new CompraService(
                compraRepository,
                proveedorRepository,
                productoRepository,
                movimientoInventarioRepository,
                tenantService,
                "America/Bogota"
        );
    }

    @Test
    void debeCalcularCostoUnitarioDesdeSubtotalCuandoNoLlegaCostoUnitario() {
        Producto producto = productoInventariable(10L);
        prepararStubsBase(producto, true);

        DetalleCompraRequestDTO detalle = new DetalleCompraRequestDTO();
        detalle.setProductoId(10L);
        detalle.setCantidad(4);
        detalle.setSubtotal(new BigDecimal("1000.00"));

        Compra compra = compraService.crearCompra(
                1L,
                "FAC-001",
                "Compra de prueba",
                List.of(detalle)
        );

        CompraDetalle detalleCreado = compra.getDetalles().get(0);
        assertThat(detalleCreado.getCostoUnitario()).isEqualByComparingTo("250.00");
        assertThat(detalleCreado.calcularSubtotal()).isEqualByComparingTo("1000.00");
    }

    @Test
    void debeRetornarErrorCuandoNoLlegaSubtotal() {
        Producto producto = productoInventariable(10L);
        prepararStubsBase(producto, false);

        DetalleCompraRequestDTO detalle = new DetalleCompraRequestDTO();
        detalle.setProductoId(10L);
        detalle.setCantidad(4);

        assertThatThrownBy(() -> compraService.crearCompra(
                1L,
                "FAC-001",
                "Compra de prueba",
                List.of(detalle)
        ))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException response = (ResponseStatusException) ex;
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(response.getReason()).isEqualTo("subtotal requerido");
                });
    }

    @Test
    void debeRetornarErrorCuandoSubtotalNoEsMayorACero() {
        Producto producto = productoInventariable(10L);
        prepararStubsBase(producto, false);

        DetalleCompraRequestDTO detalle = new DetalleCompraRequestDTO();
        detalle.setProductoId(10L);
        detalle.setCantidad(4);
        detalle.setSubtotal(BigDecimal.ZERO);

        assertThatThrownBy(() -> compraService.crearCompra(
                1L,
                "FAC-001",
                "Compra de prueba",
                List.of(detalle)
        ))
                .isInstanceOf(ResponseStatusException.class)
                .satisfies(ex -> {
                    ResponseStatusException response = (ResponseStatusException) ex;
                    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
                    assertThat(response.getReason()).isEqualTo("subtotal invalido");
                });
    }

    private void prepararStubsBase(Producto producto, boolean incluirSave) {
        when(proveedorRepository.findByIdAndNegocio(1L, negocio)).thenReturn(Optional.of(proveedorValido()));
        when(compraRepository.existsByProveedorIdAndNumeroFacturaAndNegocio(1L, "FAC-001", negocio)).thenReturn(false);
        when(productoRepository.findByIdAndNegocio(10L, negocio)).thenReturn(Optional.of(producto));
        if (incluirSave) {
            when(compraRepository.save(any(Compra.class))).thenAnswer(invocation -> invocation.getArgument(0));
        }
    }

    private Proveedor proveedorValido() {
        Proveedor proveedor = new Proveedor(1L, "Proveedor Test", "3001234567", "proveedor@test.com", TipoDocumento.NIT);
        proveedor.setNegocio(negocio);
        return proveedor;
    }

    private Producto productoInventariable(Long id) {
        Categoria categoria = new Categoria("Categoria Test");
        categoria.setNegocio(negocio);
        Producto producto = new Producto(
                "Producto Test",
                new BigDecimal("5000.00"),
                "Producto para test",
                categoria,
                TipoGestionProducto.INVENTARIABLE,
                TipoImpuesto.IVA,
                BigDecimal.valueOf(19),
                0,
                0
        );
        producto.setId(id);
        return producto;
    }
}
