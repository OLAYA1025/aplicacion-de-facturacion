package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.DocumentoFiscal;
import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.dto.factura.Establecimiento;
import org.springframework.stereotype.Component;

@Component
public class EstablecimientoResolver {

    private final Establecimiento establecimientoBase;

    public EstablecimientoResolver(Establecimiento establecimientoBase) {
        this.establecimientoBase = establecimientoBase;
    }

    public Establecimiento resolverPara(DocumentoFiscal doc) {
        Establecimiento result = copiarBase();
        if (doc == null) {
            return result;
        }

        Negocio negocio = doc.getNegocio();
        if (negocio == null) {
            return result;
        }

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
        if (hasText(negocio.getLogoArchivo())) {
            result.setLogoFileName(negocio.getLogoArchivo());
        }

        return result;
    }

    private Establecimiento copiarBase() {
        Establecimiento result = new Establecimiento();
        result.setName(establecimientoBase.getName());
        result.setNit(establecimientoBase.getNit());
        result.setDv(establecimientoBase.getDv());
        result.setAddress(establecimientoBase.getAddress());
        result.setPhone_number(establecimientoBase.getPhone_number());
        result.setEmail(establecimientoBase.getEmail());
        result.setLogoFileName(establecimientoBase.getLogoFileName());
        result.setMunicipality_id(establecimientoBase.getMunicipality_id());
        result.setMunicipality_code_dian(establecimientoBase.getMunicipality_code_dian());
        return result;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
