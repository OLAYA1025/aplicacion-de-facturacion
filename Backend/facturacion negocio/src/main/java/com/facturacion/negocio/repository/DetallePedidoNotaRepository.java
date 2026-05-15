package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.DetallePedidoNota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DetallePedidoNotaRepository extends JpaRepository<DetallePedidoNota, Long> {
}
