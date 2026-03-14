package com.facturacion.negocio.dto.response;

import com.facturacion.negocio.domain.MovimientoCaja;
import com.facturacion.negocio.domain.Usuario;
import com.facturacion.negocio.enumerated.TipoMovimiento;

import java.math.BigDecimal;
import java.time.Instant;

public class MovimientoCajaResponseDTO {
    private Long id;
    private BigDecimal monto;
    private TipoMovimiento tipo;
    private String username;
    private String motivo;
    private Instant fecha;

    public MovimientoCajaResponseDTO() {
    }

    public MovimientoCajaResponseDTO(Long id,
                                     BigDecimal monto,
                                     TipoMovimiento tipo,
                                     String username,
                                     String motivo,
                                     Instant fecha) {
        this.id = id;
        this.monto = monto;
        this.tipo = tipo;
        this.username = username;
        this.motivo = motivo;
        this.fecha = fecha;
    }

    public static MovimientoCajaResponseDTO from(MovimientoCaja movimientoCaja) {
        MovimientoCajaResponseDTO movimientoCajaResponseDTO = new MovimientoCajaResponseDTO();
        movimientoCajaResponseDTO.setId(movimientoCaja.getId());
        movimientoCajaResponseDTO.setMonto(movimientoCaja.getMonto());
        movimientoCajaResponseDTO.setTipo(movimientoCaja.getTipo());
        movimientoCajaResponseDTO.setUsername(movimientoCaja.getUsuario().getUsername());
        movimientoCajaResponseDTO.setMotivo(movimientoCaja.getMotivo());
        movimientoCajaResponseDTO.setFecha(movimientoCaja.getFecha());
        return movimientoCajaResponseDTO;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getMonto() {
        return monto;
    }

    public void setMonto(BigDecimal monto) {
        this.monto = monto;
    }

    public TipoMovimiento getTipo() {
        return tipo;
    }

    public void setTipo(TipoMovimiento tipo) {
        this.tipo = tipo;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Instant getFecha() {
        return fecha;
    }

    public void setFecha(Instant fecha) {
        this.fecha = fecha;
    }

    public String getMotivo() {
        return motivo;
    }
    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
