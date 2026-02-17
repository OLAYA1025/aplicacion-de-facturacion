package com.facturacion.negocio.services;

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

    public InventarioService(MovimientoInventarioRepository movimientoInventarioRepository,
                             ProductoRepository productoRepository) {
        this.movimientoInventarioRepository = movimientoInventarioRepository;
        this.productoRepository = productoRepository;
    }

    public List<MovimientoInventario> obtenerMovimientosPorProducto(Long productoId) {
        if (productoId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "productoId invalido");
        }
        if (!productoRepository.existsById(productoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado");
        }
        return movimientoInventarioRepository.findByProductoCodigoOrderByFechaDesc(productoId);
    }
}
