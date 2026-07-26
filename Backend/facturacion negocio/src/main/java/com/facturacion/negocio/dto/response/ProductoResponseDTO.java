package com.facturacion.negocio.dto.response;

import com.facturacion.negocio.domain.Categoria;
import com.facturacion.negocio.domain.Producto;
import com.facturacion.negocio.enumerated.TipoGestionProducto;
import com.facturacion.negocio.enumerated.TipoImpuesto;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public record ProductoResponseDTO(
        Long id,
        String codigo,
        String nombre,
        BigDecimal precio,
        BigDecimal costo,
        boolean activo,
        TipoGestionProducto tipoGestion,
        TipoGestionProducto tipoProducto,
        Integer stockActual,
        Integer stockMinimo,
        Integer stockReservado,
        Integer stockDisponible,
        String descripcion,
        TipoImpuesto tipoImpuesto,
        BigDecimal impuestoPorcentaje,
        boolean isExcluded,
        CategoriaProductoDTO categoria
) {

    public static ProductoResponseDTO fromEntity(Producto producto) {
        return new ProductoResponseDTO(
                producto.getId(),
                producto.getCodigo(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getCosto(),
                Boolean.TRUE.equals(producto.isActivo()),
                producto.getTipoGestion(),
                producto.getTipoGestion(),
                producto.getStockActual(),
                producto.getStockMinimo(),
                producto.getStockReservado(),
                producto.getStockDisponible(),
                producto.getDescripcion(),
                producto.getTipoImpuesto(),
                producto.getImpuestoPorcentaje(),
                Boolean.TRUE.equals(producto.getIsExcluded()),
                CategoriaProductoDTO.fromEntity(producto.getCategoria())
        );
    }

    @JsonProperty("ivaPorcentaje")
    public BigDecimal ivaPorcentajeLegacy() {
        return impuestoPorcentaje;
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
