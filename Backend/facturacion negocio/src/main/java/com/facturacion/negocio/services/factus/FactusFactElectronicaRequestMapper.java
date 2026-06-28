package com.facturacion.negocio.services.factus;

import com.facturacion.negocio.domain.DetallePedido;
import com.facturacion.negocio.domain.DocumentoFiscal;
import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.domain.NumberingRange;
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
        dto.setNumbering_range_id(resolveNumberingRangeId(doc));

        if (doc.getTipo() == TipoDocumentoFiscal.FEV && doc.getCliente().getEmail() != null) {
            dto.setSend_email(true);
        }

        dto.setEstablecimiento(resolveEstablecimiento(doc));

        return dto;
    }

    private Integer resolveNumberingRangeId(DocumentoFiscal doc) {
        NumberingRange range = doc.getNumberingRange();
        if (range != null && range.getProviderRangeId() != null) {
            return range.getProviderRangeId();
        }
        return doc.getNumberingRangeId();
    }

    private Establecimiento resolveEstablecimiento(DocumentoFiscal doc) {
        Establecimiento result = new Establecimiento();
        result.setName(establecimiento.getName());
        result.setNit(establecimiento.getNit());
        result.setDv(establecimiento.getDv());
        result.setAddress(establecimiento.getAddress());
        result.setPhone_number(establecimiento.getPhone_number());
        result.setEmail(establecimiento.getEmail());
        result.setMunicipality_id(establecimiento.getMunicipality_id());
        result.setMunicipality_code_dian(establecimiento.getMunicipality_code_dian());

        Negocio negocio = doc.getNegocio();
        if (negocio != null) {
            if (hasText(negocio.getNombre())) {
                result.setName(negocio.getNombre());
            }
            if (hasText(negocio.getNit())) {
                result.setNit(negocio.getNit());
            }
            if (hasText(negocio.getDireccion())) {
                result.setAddress(negocio.getDireccion());
            }
            if (hasText(negocio.getTelefono())) {
                result.setPhone_number(negocio.getTelefono());
            }
        }

        return result;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
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
