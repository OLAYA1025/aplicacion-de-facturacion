package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.domain.MovimientoInventario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MovimientoInventarioRepository extends JpaRepository<MovimientoInventario, Long> {

    List<MovimientoInventario> findByProductoIdAndNegocioOrderByFechaDesc(Long productoId, Negocio negocio);


}
