package com.facturacion.negocio.dto.response;

import com.facturacion.negocio.domain.Categoria;
import com.facturacion.negocio.domain.Producto;
import com.facturacion.negocio.enumerated.TipoGestionProducto;

import java.math.BigDecimal;

public record ProductoResponseDTO(
        Long codigo,
        String nombre,
        BigDecimal precio,
        boolean activo,
        TipoGestionProducto tipoGestion,
        TipoGestionProducto tipoProducto,
        Integer stockActual,
        Integer stockMinimo,
        Integer stockReservado,
        Integer stockDisponible,
        String descripcion,
        BigDecimal ivaPorcentaje,
        boolean isExcluded,
        CategoriaProductoDTO categoria
) {

    public static ProductoResponseDTO fromEntity(Producto producto) {
        return new ProductoResponseDTO(
                producto.getCodigo(),
                producto.getNombre(),
                producto.getPrecio(),
                Boolean.TRUE.equals(producto.isActivo()),
                producto.getTipoGestion(),
                producto.getTipoGestion(),
                producto.getStockActual(),
                producto.getStockMinimo(),
                producto.getStockReservado(),
                producto.getStockDisponible(),
                producto.getDescripcion(),
                producto.getIvaPorcentaje(),
                Boolean.TRUE.equals(producto.getIsExcluded()),
                CategoriaProductoDTO.fromEntity(producto.getCategoria())
        );
    }

    public record CategoriaProductoDTO(Long id, String nombre) {
        public static CategoriaProductoDTO fromEntity(Categoria categoria) {
            if (categoria == null) {
                return null;
            }
            return new CategoriaProductoDTO(categoria.getId(), categoria.getNombre());
        }
    }
}
