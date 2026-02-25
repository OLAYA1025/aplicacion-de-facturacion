package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.Compra;
import com.facturacion.negocio.domain.CompraDetalle;
import com.facturacion.negocio.domain.MovimientoInventario;
import com.facturacion.negocio.domain.Producto;
import com.facturacion.negocio.domain.Proveedor;
import com.facturacion.negocio.dto.request.DetalleCompraRequestDTO;
import com.facturacion.negocio.dto.response.CompraResumenResponseDTO;
import com.facturacion.negocio.enumerated.EstadoCompra;
import com.facturacion.negocio.enumerated.TipoMovimientoInventario;
import com.facturacion.negocio.repository.CompraRepository;
import com.facturacion.negocio.repository.MovimientoInventarioRepository;
import com.facturacion.negocio.repository.ProductoRepository;
import com.facturacion.negocio.repository.ProveedorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CompraService {

    private static final Set<String> CAMPOS_ORDEN_PERMITIDOS =
            Set.of("id", "fechaCreacion", "total", "estado", "numeroFactura");

    private final CompraRepository compraRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoInventarioRepository movimientoInventarioRepository;

    public CompraService(CompraRepository compraRepository,
                        ProveedorRepository proveedorRepository,
                        ProductoRepository productoRepository,
                        MovimientoInventarioRepository movimientoInventarioRepository) {
        this.compraRepository = compraRepository;
        this.proveedorRepository = proveedorRepository;
        this.productoRepository = productoRepository;
        this.movimientoInventarioRepository = movimientoInventarioRepository;
    }

    @Transactional
    public Compra crearCompra(Long proveedorId,
                              String numeroFactura,
                              String observacion,
                              List<DetalleCompraRequestDTO> detallesRequest) {
        if (proveedorId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "proveedorId invalido");
        }
        String facturaNormalizada = normalizarFactura(numeroFactura);
        Proveedor proveedor = proveedorRepository.findById(proveedorId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Proveedor no encontrado"));

        if (compraRepository.existsByProveedorIdAndNumeroFactura(proveedorId, facturaNormalizada)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "numero de factura duplicado para el proveedor");
        }
        if (detallesRequest == null || detallesRequest.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "la compra debe tener detalles");
        }

        Compra compra = new Compra(proveedor, facturaNormalizada, observacion);
        Set<Long> productoIds = new HashSet<>();
        for (DetalleCompraRequestDTO detalleRequest : detallesRequest) {
            if (detalleRequest == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "detalle invalido");
            }
            Long productoId = detalleRequest.getProductoId();
            if (productoId == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productoId invalido");
            }
            if (!productoIds.add(productoId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productoId repetido en la compra");
            }
            Producto producto = productoRepository.findById(productoId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
            if (!Boolean.TRUE.equals(producto.isActivo())) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Producto inactivo");
            }
            if (!producto.esInventariable()) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "El producto " + producto.getNombre() + " es PREPARACION y no se compra por inventario");
            }
            CompraDetalle detalle = new CompraDetalle(
                    producto,
                    detalleRequest.getCantidad(),
                    detalleRequest.getCostoUnitario()
            );
            compra.agregarDetalle(detalle);
        }

        return compraRepository.save(compra);
    }

    @Transactional(readOnly = true)
    public Compra obtenerCompra(Long id) {
        if (id == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id compra invalido");
        }
        return compraRepository.findByIdConDetalles(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compra no encontrada"));
    }

    @Transactional(readOnly = true)
    public Page<CompraResumenResponseDTO> listarCompras(int page,
                                                        int size,
                                                        String ordenarPor,
                                                        String direccion,
                                                        LocalDate desde,
                                                        LocalDate hasta,
                                                        EstadoCompra estado) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "page invalido");
        }
        if (size <= 0 || size > 200) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "size invalido");
        }
        if (ordenarPor == null || ordenarPor.isBlank() || !CAMPOS_ORDEN_PERMITIDOS.contains(ordenarPor)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "ordenarPor invalido");
        }
        String direccionNormalizada = direccion == null ? null : direccion.trim().toLowerCase();
        if ("des".equals(direccionNormalizada)) {
            direccionNormalizada = "desc";
        }
        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(direccionNormalizada)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "direccion invalida"));
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, ordenarPor));

        if (desde != null && hasta != null && desde.isAfter(hasta)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "rango de fechas invalido");
        }
        LocalDateTime desdeInclusive = desde != null ? desde.atStartOfDay() : null;
        LocalDateTime hastaExclusiva = hasta != null ? hasta.plusDays(1).atStartOfDay() : null;

        if (estado != null && desdeInclusive != null && hastaExclusiva != null) {
            return compraRepository
                    .findByEstadoAndFechaCreacionGreaterThanEqualAndFechaCreacionLessThan(
                            estado, desdeInclusive, hastaExclusiva, pageable)
                    .map(CompraResumenResponseDTO::from);
        }
        if (estado != null && desdeInclusive != null) {
            return compraRepository
                    .findByEstadoAndFechaCreacionGreaterThanEqual(estado, desdeInclusive, pageable)
                    .map(CompraResumenResponseDTO::from);
        }
        if (estado != null && hastaExclusiva != null) {
            return compraRepository
                    .findByEstadoAndFechaCreacionLessThan(estado, hastaExclusiva, pageable)
                    .map(CompraResumenResponseDTO::from);
        }
        if (estado != null) {
            return compraRepository.findByEstado(estado, pageable).map(CompraResumenResponseDTO::from);
        }
        if (desdeInclusive != null && hastaExclusiva != null) {
            return compraRepository
                    .findByFechaCreacionGreaterThanEqualAndFechaCreacionLessThan(desdeInclusive, hastaExclusiva, pageable)
                    .map(CompraResumenResponseDTO::from);
        }
        if (desdeInclusive != null) {
            return compraRepository
                    .findByFechaCreacionGreaterThanEqual(desdeInclusive, pageable)
                    .map(CompraResumenResponseDTO::from);
        }
        if (hastaExclusiva != null) {
            return compraRepository
                    .findByFechaCreacionLessThan(hastaExclusiva, pageable)
                    .map(CompraResumenResponseDTO::from);
        }
        return compraRepository.findAll(pageable).map(CompraResumenResponseDTO::from);
    }

    @Transactional
    public Compra confirmarCompra(Long compraId) {
        if (compraId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "compraId invalido");
        }
        Compra compra = compraRepository.findByIdForUpdate(compraId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compra no encontrada"));
        if (!compra.isBorrador()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La compra debe estar en BORRADOR");
        }

        // Carga detalles/productos dentro de la transaccion para actualizar stock.
        Compra compraConDetalles = compraRepository.findByIdConDetalles(compra.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compra no encontrada"));

        for (CompraDetalle detalle : compraConDetalles.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.aumentarStock(detalle.getCantidad());
            movimientoInventarioRepository.save(new MovimientoInventario(
                    producto,
                    TipoMovimientoInventario.COMPRA,
                    detalle.getCantidad(),
                    "COMPRA",
                    compraConDetalles.getId(),
                    "Compra factura " + compraConDetalles.getNumeroFactura()
            ));
        }
        compraConDetalles.confirmar();
        return compraRepository.save(compraConDetalles);
    }

    @Transactional
    public Compra anularCompra(Long compraId) {
        if (compraId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "compraId invalido");
        }
        Compra compra = compraRepository.findByIdForUpdate(compraId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compra no encontrada"));
        if (compra.isAnulada()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La compra ya esta anulada");
        }
        if (compra.isConfirmada()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "No se puede anular una compra confirmada");
        }

        compra.anular(obtenerUsernameAutenticado());
        return compraRepository.save(compra);
    }

    private String normalizarFactura(String numeroFactura) {
        if (numeroFactura == null || numeroFactura.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "numeroFactura invalido");
        }
        return numeroFactura.trim();
    }

    private String obtenerUsernameAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        String username = auth.getName();
        if (username == null || username.isBlank() || "anonymousUser".equalsIgnoreCase(username)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No autenticado");
        }
        return username.trim();
    }
}
