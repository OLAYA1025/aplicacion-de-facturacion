package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.domain.Producto;
import com.facturacion.negocio.domain.MovimientoInventario;
import com.facturacion.negocio.repository.MovimientoInventarioRepository;
import com.facturacion.negocio.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class InventarioService {

    private final MovimientoInventarioRepository movimientoInventarioRepository;
    private final ProductoRepository productoRepository;
    private final TenantService tenantService;

    public InventarioService(MovimientoInventarioRepository movimientoInventarioRepository,
                             ProductoRepository productoRepository,
                             TenantService tenantService) {
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.productoRepository = productoRepository;
        this.tenantService = tenantService;
    }

    public List<MovimientoInventario> obtenerMovimientosPorProducto(Long productoId) {
        if (productoId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productoId invalido");
        }
        Negocio negocio = tenantService.getNegocioActual();
        Producto producto = productoRepository.findByIdAndNegocio(productoId, negocio)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
        return movimientoInventarioRepository.findByProductoIdAndNegocioOrderByFechaDesc(producto.getId(), negocio);
    }
}
