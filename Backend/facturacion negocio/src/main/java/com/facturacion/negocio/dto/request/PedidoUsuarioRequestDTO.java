package com.facturacion.negocio.dto.request;

import com.facturacion.negocio.enumerated.MetodoPago;
import com.facturacion.negocio.enumerated.TipoDocumentoFiscal;

public class PedidoUsuarioRequestDTO {
    private Long usuarioId;
    private MetodoPago metodoPago;
    private TipoDocumentoFiscal tipoDocumentoFiscal; // DEE_POS / FEV / FEV_CONS_FINAL
    private ClienteFiscalDTO cliente;


    public Long getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(Long usuarioId) {
        this.usuarioId = usuarioId;
    }

    public MetodoPago getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(MetodoPago metodoPago) {
        this.metodoPago = metodoPago;
    }

    public TipoDocumentoFiscal getTipoDocumentoFiscal() {
        return tipoDocumentoFiscal;
    }

    public void setTipoDocumentoFiscal(TipoDocumentoFiscal tipoDocumentoFiscal) {
        this.tipoDocumentoFiscal = tipoDocumentoFiscal;
    }

    public ClienteFiscalDTO getCliente() {
        return cliente;
    }

    public void setCliente(ClienteFiscalDTO cliente) {
        this.cliente = cliente;
    }
}
