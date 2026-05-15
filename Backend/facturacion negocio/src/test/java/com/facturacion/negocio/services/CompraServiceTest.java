package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.Categoria;
import com.facturacion.negocio.domain.Compra;
import com.facturacion.negocio.domain.CompraDetalle;
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

    private CompraService compraService;

    @BeforeEach
    void setUp() {
        compraService = new CompraService(
                compraRepository,
                proveedorRepository,
                productoRepository,
                movimientoInventarioRepository,
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
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedorValido()));
        when(compraRepository.existsByProveedorIdAndNumeroFactura(1L, "FAC-001")).thenReturn(false);
        when(productoRepository.findById(10L)).thenReturn(Optional.of(producto));
        if (incluirSave) {
            when(compraRepository.save(any(Compra.class))).thenAnswer(invocation -> invocation.getArgument(0));
        }
    }

    private Proveedor proveedorValido() {
        return new Proveedor(1L, "Proveedor Test", "3001234567", "proveedor@test.com", TipoDocumento.NIT);
    }

    private Producto productoInventariable(Long id) {
        Categoria categoria = new Categoria("Categoria Test");
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
        producto.setCodigo(id);
        return producto;
    }
}
