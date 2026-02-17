package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.Compra;
import com.facturacion.negocio.domain.CompraDetalle;
import com.facturacion.negocio.domain.MovimientoInventario;
import com.facturacion.negocio.domain.Producto;
import com.facturacion.negocio.domain.Proveedor;
import com.facturacion.negocio.dto.request.DetalleCompraRequestDTO;
import com.facturacion.negocio.enumerated.TipoMovimientoInventario;
import com.facturacion.negocio.repository.CompraRepository;
import com.facturacion.negocio.repository.MovimientoInventarioRepository;
import com.facturacion.negocio.repository.ProductoRepository;
import com.facturacion.negocio.repository.ProveedorRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class CompraService {

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
    public List<Compra> listarCompras() {
        return compraRepository.findAllConDetalles();
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

        Compra compraConDetalles = compraRepository.findByIdConDetalles(compra.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compra no encontrada"));

        if (compraConDetalles.isConfirmada()) {
            for (CompraDetalle detalle : compraConDetalles.getDetalles()) {
                Producto producto = detalle.getProducto();
                producto.disminuirStock(detalle.getCantidad());
                movimientoInventarioRepository.save(new MovimientoInventario(
                        producto,
                        TipoMovimientoInventario.ANULACION_COMPRA,
                        -detalle.getCantidad(),
                        "COMPRA",
                        compraConDetalles.getId(),
                        "Anulacion compra factura " + compraConDetalles.getNumeroFactura()
                ));
            }
        }

        compraConDetalles.anular();
        return compraRepository.save(compraConDetalles);
    }

    private String normalizarFactura(String numeroFactura) {
        if (numeroFactura == null || numeroFactura.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "numeroFactura invalido");
        }
        return numeroFactura.trim();
    }
}
