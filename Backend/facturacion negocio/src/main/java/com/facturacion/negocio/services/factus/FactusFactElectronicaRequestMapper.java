package com.facturacion.negocio.services.factus;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.facturacion.negocio.domain.DetallePedido;
import com.facturacion.negocio.domain.DocumentoFiscal;
import com.facturacion.negocio.domain.NumberingRange;
import com.facturacion.negocio.domain.Pedido;
import com.facturacion.negocio.dto.factura.CustomerDTO;
import com.facturacion.negocio.dto.factura.FactusFactElectronicaRequestDTO;
import com.facturacion.negocio.dto.factura.ItemDTO;
import com.facturacion.negocio.enumerated.TipoDocumentoFiscal;
import com.facturacion.negocio.services.DetallePedidoAgrupador;
import com.facturacion.negocio.services.DetallePedidoAgrupador.DetallePedidoAgrupado;
import com.facturacion.negocio.services.EstablecimientoResolver;

@Component
public class FactusFactElectronicaRequestMapper {

    private final EstablecimientoResolver establecimientoResolver;

    public FactusFactElectronicaRequestMapper(EstablecimientoResolver establecimientoResolver) {
        this.establecimientoResolver = establecimientoResolver;
    }

    public FactusFactElectronicaRequestDTO from(DocumentoFiscal doc) {
        Pedido pedido = doc.getPedido();

        FactusFactElectronicaRequestDTO dto = new FactusFactElectronicaRequestDTO();
        dto.setReference_code(doc.getReferenceCode());
        dto.setPayment_method_code(pedido.getMetodoPago().getfactusId().toString());
        dto.setCustomer(CustomerDTO.from(doc.getCliente()));
        dto.setItems(mapItems(pedido.getDetallePedidos()));
        dto.setNumbering_range_id(resolveNumberingRangeId(doc));

        if (doc.getTipo() == TipoDocumentoFiscal.FEV && doc.getCliente().getEmail() != null) {
            dto.setSend_email(true);
        }

        dto.setEstablecimiento(establecimientoResolver.resolverPara(doc));

        return dto;
    }

    private Integer resolveNumberingRangeId(DocumentoFiscal doc) {
        NumberingRange range = doc.getNumberingRange();
        if (range != null && range.getProviderRangeId() != null) {
            return range.getProviderRangeId();
        }
        return doc.getNumberingRangeId();
    }

    private List<ItemDTO> mapItems(List<DetallePedido> detalles) {
        List<ItemDTO> itemsDTO = new ArrayList<>();
        List<DetallePedidoAgrupado> detallesAgrupados = DetallePedidoAgrupador.agruparPorProducto(detalles);
        for (DetallePedidoAgrupado detalleAgrupado : detallesAgrupados) {
            itemsDTO.add(ItemDTO.from(
                    detalleAgrupado.getProducto(),
                    detalleAgrupado.getCantidad(),
                    detalleAgrupado.getPrecioUnitario(),
                    detalleAgrupado.getTipoImpuesto(),
                    detalleAgrupado.getImpuestoPorcentaje()
            ));
        }
        return itemsDTO;
    }
}
