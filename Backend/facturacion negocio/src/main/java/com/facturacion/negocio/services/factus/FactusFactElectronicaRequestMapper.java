package com.facturacion.negocio.services.factus;

import com.facturacion.negocio.domain.DetallePedido;
import com.facturacion.negocio.domain.DocumentoFiscal;
import com.facturacion.negocio.domain.Pedido;
import com.facturacion.negocio.dto.factura.CustomerDTO;
import com.facturacion.negocio.dto.factura.Establecimiento;
import com.facturacion.negocio.dto.factura.FactusFactElectronicaRequestDTO;
import com.facturacion.negocio.dto.factura.ItemDTO;
import org.springframework.stereotype.Component;
import com.facturacion.negocio.enumerated.TipoDocumentoFiscal;
import com.facturacion.negocio.services.DetallePedidoAgrupador;
import com.facturacion.negocio.services.DetallePedidoAgrupador.DetallePedidoAgrupado;

import java.util.ArrayList;
import java.util.List;

@Component
public class FactusFactElectronicaRequestMapper {

    private final Establecimiento establecimiento; // Bean configurado

    public FactusFactElectronicaRequestMapper(Establecimiento establecimiento) {
        this.establecimiento = establecimiento;
    }

    public FactusFactElectronicaRequestDTO from(DocumentoFiscal doc) {
        Pedido pedido = doc.getPedido();

        FactusFactElectronicaRequestDTO dto = new FactusFactElectronicaRequestDTO();
        dto.setReference_code(doc.getReferenceCode());
        dto.setPayment_method_code(pedido.getMetodoPago().getfactusId().toString());
        dto.setCustomer(CustomerDTO.from(doc.getCliente()));
        dto.setItems(mapItems(pedido.getDetallePedidos()));

        if (doc.getTipo() == TipoDocumentoFiscal.FEV && doc.getCliente().getEmail() != null) {
            dto.setSend_email(true);
        }

        dto.setEstablecimiento(establecimiento);

        return dto;
    }

    private List<ItemDTO> mapItems(List<DetallePedido> detalles) {
        List<ItemDTO> itemsDTO = new ArrayList<>();
        List<DetallePedidoAgrupado> detallesAgrupados = DetallePedidoAgrupador.agruparPorProducto(detalles);
        for (DetallePedidoAgrupado detalleAgrupado : detallesAgrupados) {
            itemsDTO.add(ItemDTO.from(
                    detalleAgrupado.getProducto(),
                    detalleAgrupado.getCantidad(),
                    detalleAgrupado.getPrecioUnitario()
            ));
        }
        return itemsDTO;
    }
}
