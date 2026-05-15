package com.facturacion.negocio.services.dian.mapper;

import com.facturacion.negocio.domain.*;
import com.facturacion.negocio.dto.factura.Establecimiento;
import com.facturacion.negocio.services.dian.dto.DianCustomerData;
import com.facturacion.negocio.services.dian.dto.DianInvoiceData;
import com.facturacion.negocio.services.dian.dto.DianInvoiceLineData;
import com.facturacion.negocio.services.dian.dto.DianSupplierData;
import com.facturacion.negocio.services.DetallePedidoAgrupador;
import com.facturacion.negocio.services.DetallePedidoAgrupador.DetallePedidoAgrupado;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Component
public class DianInvoiceMapper {

    private final Establecimiento establecimiento;

    public DianInvoiceMapper(Establecimiento establecimiento) {
        this.establecimiento = establecimiento;
    }


    public DianInvoiceData from(DocumentoFiscal doc) {

        Pedido pedido = doc.getPedido();

        DianInvoiceData data = new DianInvoiceData();

        data.setInvoiceNumber(doc.getNumeroDocumento());
        data.setConsecutive(doc.getConsecutivo());
        data.setPrefix(doc.getNumberingRange().getPrefix());

        data.setIssuedAt(doc.getEnviadoAt());

        data.setCustomer(mapCustomer(doc.getCliente()));

        data.setSupplier(mapSupplier(establecimiento));

        List<DianInvoiceLineData> lines = mapLines(pedido.getDetallePedidos());

        data.setLines(lines);

        BigDecimal subtotal = calculateSubtotal(lines);
        BigDecimal ivaTotal = calcularIvaTotal(lines);
        BigDecimal incTotal = calcularIncTotal(lines);
        BigDecimal taxTotal = ivaTotal.add(incTotal);
        BigDecimal total = subtotal.add(taxTotal).setScale(2, RoundingMode.HALF_UP);

        data.setIvaTotal(ivaTotal);
        data.setIncTotal(incTotal);
        data.setSubtotal(subtotal);
        data.setTaxTotal(taxTotal);
        data.setTotal(total);

        return data;
    }

    private BigDecimal calculateSubtotal(List<DianInvoiceLineData> lines) {
        return lines.stream()
                .map(DianInvoiceLineData::getLineExtensionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateTaxTotal(List<DianInvoiceLineData> lines) {
        return lines.stream()
                .map(DianInvoiceLineData::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularIvaTotal(List<DianInvoiceLineData> lines) {
        return lines.stream()
                .filter(l -> "01".equals(l.getTaxCode()))
                .map(DianInvoiceLineData::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularIncTotal(List<DianInvoiceLineData> lines) {
        return lines.stream()
                .filter(l -> "04".equals(l.getTaxCode()))
                .map(DianInvoiceLineData::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
    }

    private DianCustomerData mapCustomer(Cliente cliente) {
        DianCustomerData customer = new DianCustomerData();

        customer.setIdentificationNumber(cliente.getNumeroIdentificacion());

        customer.setIdentificationType(
                cliente.getTipoIdentificacion().getDianCode()
        );

        customer.setName(cliente.getNombre());

        customer.setEmail(cliente.getEmail());

        customer.setLegalOrganizationType(cliente.getTipoIdentificacion().getLegalOrganizacionId());

        return customer;

    }

    private DianSupplierData mapSupplier(Establecimiento establecimiento) {

        DianSupplierData supplier = new DianSupplierData();

        System.out.println(establecimiento.toString());
        supplier.setNit(establecimiento.getNit());
        supplier.setDv(establecimiento.getDv());
        supplier.setName(establecimiento.getName());
        supplier.setAddress(establecimiento.getAddress());
        supplier.setPhone(establecimiento.getPhone_number());
        supplier.setEmail(establecimiento.getEmail());
        supplier.setMunicipalityCode(
                establecimiento.getMunicipality_code_dian()
        );

        return supplier;
    }

    private DianInvoiceLineData mapLine(Producto producto, Integer cantidad, BigDecimal precioVenta) {
        BigDecimal porcentaje = producto.getImpuestoPorcentaje();

        BigDecimal divisor = BigDecimal.ONE.add(
                porcentaje.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)
        );

        BigDecimal baseUnitPrice = precioVenta.divide(divisor, 2, RoundingMode.HALF_UP);
        BigDecimal taxUnitAmount = precioVenta.subtract(baseUnitPrice);

        BigDecimal quantity = BigDecimal.valueOf(cantidad);

        BigDecimal lineExtensionAmount = baseUnitPrice.multiply(quantity);
        BigDecimal taxAmount = taxUnitAmount.multiply(quantity);

        DianInvoiceLineData line = new DianInvoiceLineData();

        line.setCode(producto.getCodigo().toString());
        line.setDescription(producto.getNombre());
        line.setQuantity(cantidad);

        line.setUnitPrice(baseUnitPrice);

        line.setTaxPercent(porcentaje);

        line.setTaxCode(producto.getTipoImpuesto().getIdDian());

        line.setLineExtensionAmount(lineExtensionAmount);

        line.setTaxAmount(taxAmount);

        return line;
    }

    private List<DianInvoiceLineData> mapLines(List<DetallePedido> detalles) {

        List<DianInvoiceLineData> lines = new ArrayList<>();
        List<DetallePedidoAgrupado> detallesAgrupados = DetallePedidoAgrupador.agruparPorProducto(detalles);
        for (DetallePedidoAgrupado detalleAgrupado : detallesAgrupados) {
            lines.add(mapLine(
                    detalleAgrupado.getProducto(),
                    detalleAgrupado.getCantidad(),
                    detalleAgrupado.getPrecioUnitario()
            ));
        }

        return lines;
    }

}
