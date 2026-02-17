package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.Caja;
import com.facturacion.negocio.domain.Usuario;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;

import java.util.List;
import java.util.Optional;

@Repository
public interface CajaRepository extends JpaRepository<Caja, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Caja c WHERE c.id = :id")
    Optional<Caja> findByIdForUpdate(@Param("id") Long id);

    /*Los retiros se restan al final, luego, que el usuario escoge cuanto retira, caundo va a cerrar caja*/
    @Query(" SELECT " +
            "COALESCE(" +
            "SUM(CASE WHEN m.tipo = com.facturacion.negocio.enumerated.TipoMovimiento.INGRESO THEN m.monto ELSE 0 END)" +
            "-" +
            "SUM(CASE WHEN m.tipo <> com.facturacion.negocio.enumerated.TipoMovimiento.INGRESO THEN m.monto ELSE 0 END), 0) " +
            "FROM MovimientoCaja m WHERE m.caja.id = :cajaId")
    BigDecimal calcularSaldoPorCaja(@Param("cajaId") Long cajaId);

    @Query("SELECT CASE " +
            "WHEN COUNT(c) > 0 " +
            "THEN true " +
            "ELSE false END " +
            "FROM Caja c WHERE c.estado = com.facturacion.negocio.enumerated.EstadoCaja.ABIERTA ")
    boolean existeCajaAbierta();

    @Query("SELECT c FROM Caja c WHERE c.estado = com.facturacion.negocio.enumerated.EstadoCaja.ABIERTA")
    Optional<Caja> obtenerCajaAbierta();


    @Query("""
SELECT c FROM Caja c
WHERE c.estado = com.facturacion.negocio.enumerated.EstadoCaja.CERRADA
AND c.fechaCierre = (
    SELECT MAX(c2.fechaCierre)
    FROM Caja c2
    WHERE c2.estado = com.facturacion.negocio.enumerated.EstadoCaja.CERRADA
)
""")
    Optional<Caja> obtenerUltimaCajaCerrada();


    /*
    List<Caja> findCajaByDateBetween(
            LocalDate inicio,
            LocalDate fin
    );

    List<Caja> findCajaByUsuarioAndDateBetween(
            Usuario usuario,
            LocalDate inicio,
            LocalDate fin
    );
    */


}
