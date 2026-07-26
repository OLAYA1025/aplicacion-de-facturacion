package com.facturacion.negocio.dto.response;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.facturacion.negocio.domain.DetallePedido;
import com.facturacion.negocio.domain.DetallePedidoNota;
import com.facturacion.negocio.domain.Producto;
import com.facturacion.negocio.enumerated.TipoImpuesto;

public class DetallePedidoResponseDTO {
    private Long id;
    private Long productoId;
    private String productoNombre;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private TipoImpuesto tipoImpuesto;
    private BigDecimal impuestoPorcentaje;
    private BigDecimal subtotal;
    private List<DetallePedidoNotaResponseDTO> notas = new ArrayList<>();

    public static DetallePedidoResponseDTO from(DetallePedido detalle) {
        DetallePedidoResponseDTO dto = new DetallePedidoResponseDTO();
        Producto producto = detalle.getProducto();
        if (producto != null) {
            dto.setProductoId(producto.getId());
            dto.setProductoNombre(producto.getNombre());
        }
        dto.setId(detalle.getId());
        dto.setCantidad(detalle.getCantidad());
        dto.setPrecioUnitario(detalle.getPrecioUnitario());
        dto.setTipoImpuesto(detalle.getTipoImpuesto());
        dto.setImpuestoPorcentaje(detalle.getImpuestoPorcentaje());
        dto.setSubtotal(detalle.calcularSubtotal());
        List<DetallePedidoNota> detalleNotas = new ArrayList<>(detalle.getDetallePedidoNotas());
        detalleNotas.sort(Comparator.comparing(
                (DetallePedidoNota nota) -> {
                    if (nota.getNotaPredefinida() == null || nota.getNotaPredefinida().getGrupo() == null) {
                        return "";
                    }
                    return nota.getNotaPredefinida().getGrupo().getNombre();
                },
                String.CASE_INSENSITIVE_ORDER
        ).thenComparing(
                (DetallePedidoNota nota) -> nota.getNotaPredefinida() != null ? nota.getNotaPredefinida().getDescripcion() : "",
                String.CASE_INSENSITIVE_ORDER
        ));

        List<DetallePedidoNotaResponseDTO> notasDto = new ArrayList<>(detalleNotas.size());
        for (DetallePedidoNota detalleNota : detalleNotas) {
            notasDto.add(DetallePedidoNotaResponseDTO.from(detalleNota));
        }
        dto.setNotas(notasDto);
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductoId() {
        return productoId;
    }

    public void setProductoId(Long productoId) {
        this.productoId = productoId;
    }

    public String getProductoNombre() {
        return productoNombre;
    }

    public void setProductoNombre(String productoNombre) {
        this.productoNombre = productoNombre;
    }

    public Integer getCantidad() {
        return cantidad;
    }

    public void setCantidad(Integer cantidad) {
        this.cantidad = cantidad;
    }

    public BigDecimal getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(BigDecimal precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public TipoImpuesto getTipoImpuesto() {
        return tipoImpuesto;
    }

    public void setTipoImpuesto(TipoImpuesto tipoImpuesto) {
        this.tipoImpuesto = tipoImpuesto;
    }

    public BigDecimal getImpuestoPorcentaje() {
        return impuestoPorcentaje;
    }

    public void setImpuestoPorcentaje(BigDecimal impuestoPorcentaje) {
        this.impuestoPorcentaje = impuestoPorcentaje;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public List<DetallePedidoNotaResponseDTO> getNotas() {
        return notas;
    }

    public void setNotas(List<DetallePedidoNotaResponseDTO> notas) {
        this.notas = notas;
    }
}
