package com.facturacion.negocio.services.dian.xml;

import com.facturacion.negocio.enumerated.TipoImpuesto;
import com.facturacion.negocio.services.dian.dto.DianCustomerData;
import com.facturacion.negocio.services.dian.dto.DianInvoiceData;
import com.facturacion.negocio.services.dian.dto.DianInvoiceLineData;
import com.facturacion.negocio.services.dian.dto.DianSupplierData;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

@Component
public class DianInvoiceXmlBuilder {

    private final ZoneId zoneId;

    private static final String CBC_NS =
            "urn:oasis:names:specification:ubl:schema:xsd:CommonBasicComponents-2";

    private static final String CAC_NS =
            "urn:oasis:names:specification:ubl:schema:xsd:CommonAggregateComponents-2";

    private static final String EXT_NS =
            "urn:oasis:names:specification:ubl:schema:xsd:CommonExtensionComponents-2";

    public DianInvoiceXmlBuilder(@Value("${app.time-zone:America/Bogota}") String zoneId) {
        this.zoneId = ZoneId.of(zoneId);
    }

    private String formatDate(Instant instant) {
        return instant
                .atZone(zoneId)
                .toLocalDate()
                .toString();
    }

    private String formatTime(Instant instant) {
        return instant.atZone(zoneId)
                .toOffsetDateTime()
                .toOffsetTime()
                .withNano(0)
                .toString();
    }

    public String build(DianInvoiceData invoice) throws Exception {

        StringWriter writer = new StringWriter();

        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        XMLStreamWriter xml = factory.createXMLStreamWriter(writer);

        xml.writeStartDocument("UTF-8", "1.0");

        xml.writeStartElement("Invoice");

        xml.writeDefaultNamespace(
                "urn:oasis:names:specification:ubl:schema:xsd:Invoice-2"
        );

        xml.writeNamespace("cbc", CBC_NS);
        xml.writeNamespace("cac", CAC_NS);
        xml.writeNamespace("ext", EXT_NS);

        writeUblExtensions(xml);

        writeHeader(xml, invoice);

        writeSupplier(xml, invoice.getSupplier());

        writeCustomer(xml, invoice.getCustomer());

        writeTaxTotal(xml, invoice);

        writeLegalMonetaryTotal(xml, invoice);

        writeLines(xml, invoice.getLines());

        xml.writeEndElement();

        xml.writeEndDocument();

        xml.close();

        return writer.toString();
    }

    private void writeUblExtensions(XMLStreamWriter xml) throws Exception {
        xml.writeStartElement("ext", "UBLExtensions", EXT_NS);

        xml.writeStartElement("ext", "UBLExtension", EXT_NS);
        xml.writeStartElement("ext", "ExtensionContent", EXT_NS);
        xml.writeEndElement(); // ExtensionContent
        xml.writeEndElement(); // UBLExtension

        xml.writeEndElement(); // UBLExtensions
    }

    private void writeHeader(XMLStreamWriter xml, DianInvoiceData invoice) throws Exception {

        xml.writeStartElement("cbc", "UBLVersionID", CBC_NS);
        xml.writeCharacters("UBL 2.1");
        xml.writeEndElement();

        xml.writeStartElement("cbc", "CustomizationID", CBC_NS);
        xml.writeCharacters("10");
        xml.writeEndElement();

        xml.writeStartElement("cbc", "ProfileID", CBC_NS);
        xml.writeCharacters("DIAN 2.1: Factura Electrónica de Venta");
        xml.writeEndElement();

        xml.writeStartElement("cbc", "ProfileExecutionID", CBC_NS);
        xml.writeCharacters("2"); // 2 = habilitación / pruebas
        xml.writeEndElement();

        xml.writeStartElement("cbc", "ID", CBC_NS);
        xml.writeCharacters(invoice.getInvoiceNumber());
        xml.writeEndElement();

        xml.writeStartElement("cbc", "UUID", CBC_NS);
        xml.writeAttribute("schemeName", "CUFE-SHA384");
        xml.writeCharacters(invoice.getCufe());
        xml.writeEndElement();

        xml.writeStartElement("cbc", "IssueDate", CBC_NS);
        xml.writeCharacters(formatDate(invoice.getIssuedAt()));
        xml.writeEndElement();

        xml.writeStartElement("cbc", "IssueTime", CBC_NS);
        xml.writeCharacters(formatTime(invoice.getIssuedAt()));
        xml.writeEndElement();

        xml.writeStartElement("cbc", "InvoiceTypeCode", CBC_NS);
        xml.writeCharacters(invoice.getInvoiceTypeCode());
        xml.writeEndElement();

        xml.writeStartElement("cbc", "DocumentCurrencyCode", CBC_NS);
        xml.writeCharacters("COP");
        xml.writeEndElement();

        xml.writeStartElement("cbc", "LineCountNumeric", CBC_NS);
        xml.writeCharacters(String.valueOf(invoice.getLines().size()));
        xml.writeEndElement();
    }

    private void writeSupplier(XMLStreamWriter xml, DianSupplierData supplier) throws Exception {

        xml.writeStartElement("cac", "AccountingSupplierParty", CAC_NS);

        xml.writeStartElement("cbc", "AdditionalAccountID", CBC_NS);
        xml.writeCharacters(String.valueOf(supplier.getLegalOrganizationType()));
        xml.writeEndElement();

        xml.writeStartElement("cac", "Party", CAC_NS);

        xml.writeStartElement("cac", "PartyIdentification", CAC_NS);
        xml.writeStartElement("cbc", "ID", CBC_NS);
        xml.writeAttribute("schemeID", supplier.getDv());
        xml.writeCharacters(supplier.getNit());
        xml.writeEndElement();
        xml.writeEndElement();

        xml.writeStartElement("cac", "PartyName", CAC_NS);
        xml.writeStartElement("cbc", "Name", CBC_NS);
        xml.writeCharacters(supplier.getName());
        xml.writeEndElement();
        xml.writeEndElement();

        xml.writeStartElement("cac", "PhysicalLocation", CAC_NS);
        xml.writeStartElement("cac", "Address", CAC_NS);

        xml.writeStartElement("cbc", "ID", CBC_NS);
        xml.writeCharacters(supplier.getMunicipalityCode());
        xml.writeEndElement();

        xml.writeStartElement("cbc", "CityName", CBC_NS);
        xml.writeCharacters(supplier.getMunicipalityName());
        xml.writeEndElement();

        xml.writeStartElement("cbc", "Line", CBC_NS);
        xml.writeCharacters(supplier.getAddress());
        xml.writeEndElement();

        xml.writeEndElement();
        xml.writeEndElement();

        xml.writeStartElement("cac", "PartyTaxScheme", CAC_NS);

        xml.writeStartElement("cbc", "RegistrationName", CBC_NS);
        xml.writeCharacters(supplier.getName());
        xml.writeEndElement();

        xml.writeStartElement("cbc", "CompanyID", CBC_NS);
        xml.writeAttribute("schemeID", supplier.getDv());
        xml.writeCharacters(supplier.getNit());
        xml.writeEndElement();

        xml.writeEndElement();

        xml.writeStartElement("cac", "PartyLegalEntity", CAC_NS);

        xml.writeStartElement("cbc", "RegistrationName", CBC_NS);
        xml.writeCharacters(supplier.getName());
        xml.writeEndElement();

        xml.writeStartElement("cbc", "CompanyID", CBC_NS);
        xml.writeAttribute("schemeID", supplier.getDv());
        xml.writeCharacters(supplier.getNit());
        xml.writeEndElement();

        xml.writeEndElement();

        writeContactBlockIfPresent(xml, supplier);

        xml.writeEndElement(); // Party
        xml.writeEndElement(); // AccountingSupplierParty
    }

    private void writeContactBlockIfPresent(XMLStreamWriter xml, DianSupplierData supplier) throws Exception {
        if (supplier.getPhone() == null && supplier.getEmail() == null) {
            return;
        }

        xml.writeStartElement("cac", "Contact", CAC_NS);

        if (supplier.getPhone() != null && !supplier.getPhone().isBlank()) {
            xml.writeStartElement("cbc", "Telephone", CBC_NS);
            xml.writeCharacters(supplier.getPhone());
            xml.writeEndElement();
        }

        if (supplier.getEmail() != null && !supplier.getEmail().isBlank()) {
            xml.writeStartElement("cbc", "ElectronicMail", CBC_NS);
            xml.writeCharacters(supplier.getEmail());
            xml.writeEndElement();
        }

        xml.writeEndElement();
    }

    private void writeCustomer(XMLStreamWriter xml, DianCustomerData customer) throws Exception {

        xml.writeStartElement("cac", "AccountingCustomerParty", CAC_NS);

        xml.writeStartElement("cbc", "AdditionalAccountID", CBC_NS);
        xml.writeCharacters(String.valueOf(customer.getLegalOrganizationType()));
        xml.writeEndElement();

        xml.writeStartElement("cac", "Party", CAC_NS);

        xml.writeStartElement("cac", "PartyIdentification", CAC_NS);
        xml.writeStartElement("cbc", "ID", CBC_NS);
        xml.writeAttribute("schemeID", customer.getIdentificationType());
        xml.writeCharacters(customer.getIdentificationNumber());
        xml.writeEndElement();
        xml.writeEndElement();

        xml.writeStartElement("cac", "PartyName", CAC_NS);
        xml.writeStartElement("cbc", "Name", CBC_NS);
        xml.writeCharacters(customer.getName());
        xml.writeEndElement();
        xml.writeEndElement();

        xml.writeStartElement("cac", "PartyLegalEntity", CAC_NS);
        xml.writeStartElement("cbc", "RegistrationName", CBC_NS);
        xml.writeCharacters(customer.getName());
        xml.writeEndElement();
        xml.writeEndElement();

        if (customer.getEmail() != null && !customer.getEmail().isBlank()) {
            xml.writeStartElement("cac", "Contact", CAC_NS);
            xml.writeStartElement("cbc", "ElectronicMail", CBC_NS);
            xml.writeCharacters(customer.getEmail());
            xml.writeEndElement();
            xml.writeEndElement();
        }

        xml.writeEndElement(); // Party
        xml.writeEndElement(); // AccountingCustomerParty
    }

    private void writeTaxTotal(XMLStreamWriter xml, DianInvoiceData invoice) throws Exception {
        xml.writeStartElement("cac", "TaxTotal", CAC_NS);

        xml.writeStartElement("cbc", "TaxAmount", CBC_NS);
        xml.writeCharacters(invoice.getTaxTotal().setScale(2, RoundingMode.HALF_UP).toPlainString());
        xml.writeEndElement();

        if (invoice.getIvaTotal() != null && invoice.getIvaTotal().compareTo(BigDecimal.ZERO) > 0) {
            writeTaxSubtotal(xml, invoice.getIvaTotal(), TipoImpuesto.IVA, BigDecimal.valueOf(19));
        }

        if (invoice.getIncTotal() != null && invoice.getIncTotal().compareTo(BigDecimal.ZERO) > 0) {
            writeTaxSubtotal(xml, invoice.getIncTotal(), TipoImpuesto.INC, BigDecimal.valueOf(8));
        }

        xml.writeEndElement(); // TaxTotal
    }

    private void writeTaxSubtotal(
            XMLStreamWriter xml,
            BigDecimal taxAmount,
            TipoImpuesto tipoImpuesto,
            BigDecimal taxPercent
    ) throws Exception {

        xml.writeStartElement("cac", "TaxSubtotal", CAC_NS);

        xml.writeStartElement("cbc", "TaxAmount", CBC_NS);
        xml.writeCharacters(taxAmount.setScale(2, RoundingMode.HALF_UP).toPlainString());
        xml.writeEndElement();

        xml.writeStartElement("cac", "TaxCategory", CAC_NS);

        xml.writeStartElement("cbc", "Percent", CBC_NS);
        xml.writeCharacters(taxPercent.setScale(2, RoundingMode.HALF_UP).toPlainString());
        xml.writeEndElement();

        xml.writeStartElement("cac", "TaxScheme", CAC_NS);

        xml.writeStartElement("cbc", "ID", CBC_NS);
        xml.writeCharacters(tipoImpuesto.getIdDian());
        xml.writeEndElement();

        xml.writeEndElement(); // TaxScheme
        xml.writeEndElement(); // TaxCategory
        xml.writeEndElement(); // TaxSubtotal
    }

    private void writeLegalMonetaryTotal(XMLStreamWriter xml, DianInvoiceData invoice) throws Exception {
        xml.writeStartElement("cac", "LegalMonetaryTotal", CAC_NS);

        xml.writeStartElement("cbc", "LineExtensionAmount", CBC_NS);
        xml.writeCharacters(invoice.getSubtotal().setScale(2, RoundingMode.HALF_UP).toPlainString());
        xml.writeEndElement();

        xml.writeStartElement("cbc", "TaxExclusiveAmount", CBC_NS);
        xml.writeCharacters(invoice.getSubtotal().setScale(2, RoundingMode.HALF_UP).toPlainString());
        xml.writeEndElement();

        xml.writeStartElement("cbc", "TaxInclusiveAmount", CBC_NS);
        xml.writeCharacters(invoice.getTotal().setScale(2, RoundingMode.HALF_UP).toPlainString());
        xml.writeEndElement();

        xml.writeStartElement("cbc", "PayableAmount", CBC_NS);
        xml.writeCharacters(invoice.getTotal().setScale(2, RoundingMode.HALF_UP).toPlainString());
        xml.writeEndElement();

        xml.writeEndElement(); // LegalMonetaryTotal
    }


    private void writeLines(XMLStreamWriter xml, List<DianInvoiceLineData> lines) throws Exception {

        int lineId = 1;

        for (DianInvoiceLineData line : lines) {

            xml.writeStartElement("cac", "InvoiceLine", CAC_NS);

            xml.writeStartElement("cbc", "ID", CBC_NS);
            xml.writeCharacters(String.valueOf(lineId++));
            xml.writeEndElement();

            xml.writeStartElement("cbc", "InvoicedQuantity", CBC_NS);
            xml.writeCharacters(line.getQuantity().toString());
            xml.writeEndElement();

            xml.writeStartElement("cbc", "LineExtensionAmount", CBC_NS);
            xml.writeCharacters(line.getLineExtensionAmount().setScale(2, RoundingMode.HALF_UP).toPlainString());
            xml.writeEndElement();

            writeLineTax(xml, line);
            writeItem(xml, line);
            writePrice(xml, line);

            xml.writeEndElement(); // InvoiceLine
        }
    }

    private void writeLineTax(XMLStreamWriter xml, DianInvoiceLineData line) throws Exception {

        xml.writeStartElement("cac", "TaxTotal", CAC_NS);

        xml.writeStartElement("cbc", "TaxAmount", CBC_NS);
        xml.writeCharacters(line.getTaxAmount().setScale(2, RoundingMode.HALF_UP).toPlainString());
        xml.writeEndElement();

        xml.writeStartElement("cac", "TaxSubtotal", CAC_NS);

        xml.writeStartElement("cbc", "TaxAmount", CBC_NS);
        xml.writeCharacters(line.getTaxAmount().setScale(2, RoundingMode.HALF_UP).toPlainString());
        xml.writeEndElement();

        xml.writeStartElement("cac", "TaxCategory", CAC_NS);

        xml.writeStartElement("cbc", "Percent", CBC_NS);
        xml.writeCharacters(line.getTaxPercent().setScale(2, RoundingMode.HALF_UP).toPlainString());
        xml.writeEndElement();

        xml.writeStartElement("cac", "TaxScheme", CAC_NS);

        xml.writeStartElement("cbc", "ID", CBC_NS);
        xml.writeCharacters(line.getTaxCode());
        xml.writeEndElement();

        xml.writeEndElement(); // TaxScheme
        xml.writeEndElement(); // TaxCategory
        xml.writeEndElement(); // TaxSubtotal
        xml.writeEndElement(); // TaxTotal
    }

    private void writeItem(XMLStreamWriter xml, DianInvoiceLineData line) throws Exception {

        xml.writeStartElement("cac", "Item", CAC_NS);

        xml.writeStartElement("cbc", "Description", CBC_NS);
        xml.writeCharacters(line.getDescription());
        xml.writeEndElement();

        xml.writeEndElement(); // Item
    }

    private void writePrice(XMLStreamWriter xml, DianInvoiceLineData line) throws Exception {

        xml.writeStartElement("cac", "Price", CAC_NS);

        xml.writeStartElement("cbc", "PriceAmount", CBC_NS);
        xml.writeCharacters(line.getUnitPrice().setScale(2, RoundingMode.HALF_UP).toPlainString());
        xml.writeEndElement();

        xml.writeEndElement(); // Price
    }
}