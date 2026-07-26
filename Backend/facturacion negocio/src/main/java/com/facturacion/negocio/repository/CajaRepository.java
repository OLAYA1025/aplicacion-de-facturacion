package com.facturacion.negocio.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.facturacion.negocio.domain.Caja;
import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.enumerated.EstadoCaja;

import jakarta.persistence.LockModeType;

@Repository
public interface CajaRepository extends JpaRepository<Caja, Long> {

    @Override
    @EntityGraph(attributePaths = {"usuarioApertura", "usuarioCierre"})
    Optional<Caja> findById(Long id);

    @EntityGraph(attributePaths = {"usuarioApertura", "usuarioCierre"})
    Optional<Caja> findByIdAndNegocio(Long id, Negocio negocio);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM Caja c WHERE c.id = :id AND c.negocio = :negocio")
    Optional<Caja> findByIdForUpdate(@Param("id") Long id, @Param("negocio") Negocio negocio);

    /*Los retiros se restan al final, luego, que el usuario escoge cuanto retira, caundo va a cerrar caja*/
    @Query(" SELECT " +
            "COALESCE(" +
            "SUM(CASE WHEN m.tipo = com.facturacion.negocio.enumerated.TipoMovimiento.INGRESO THEN m.monto ELSE 0 END)" +
            "-" +
            "SUM(CASE WHEN m.tipo <> com.facturacion.negocio.enumerated.TipoMovimiento.INGRESO THEN m.monto ELSE 0 END), 0) " +
            "FROM MovimientoCaja m WHERE m.caja.id = :cajaId AND m.negocio = :negocio")
    BigDecimal calcularSaldoPorCaja(@Param("cajaId") Long cajaId, @Param("negocio") Negocio negocio);

    @Query("SELECT CASE " +
            "WHEN COUNT(c) > 0 " +
            "THEN true " +
            "ELSE false END " +
            "FROM Caja c WHERE c.estado = com.facturacion.negocio.enumerated.EstadoCaja.ABIERTA AND c.negocio = :negocio")
    boolean existeCajaAbierta(@Param("negocio") Negocio negocio);

    @EntityGraph(attributePaths = {"usuarioApertura", "usuarioCierre"})
    @Query("""
            SELECT c
            FROM Caja c
            WHERE c.estado = com.facturacion.negocio.enumerated.EstadoCaja.ABIERTA
              AND c.negocio = :negocio
            """)
    Optional<Caja> obtenerCajaAbierta(@Param("negocio") Negocio negocio);


    @EntityGraph(attributePaths = {"usuarioApertura", "usuarioCierre"})
    @Query("""
SELECT c FROM Caja c
WHERE c.estado = com.facturacion.negocio.enumerated.EstadoCaja.CERRADA
AND c.negocio = :negocio
AND c.fechaCierre = (
    SELECT MAX(c2.fechaCierre)
    FROM Caja c2
    WHERE c2.estado = com.facturacion.negocio.enumerated.EstadoCaja.CERRADA
      AND c2.negocio = :negocio
)
""")
    Optional<Caja> obtenerUltimaCajaCerrada(@Param("negocio") Negocio negocio);

    @EntityGraph(attributePaths = {"usuarioApertura", "usuarioCierre"})
    List<Caja> findByEstadoAndFechaCierreGreaterThanEqualAndFechaCierreLessThanAndNegocio(
            EstadoCaja estado,
            Instant desdeInclusive,
            Instant hastaExclusive,
            Negocio negocio
    );

    @EntityGraph(attributePaths = {"usuarioApertura", "usuarioCierre"})
    List<Caja> findByEstadoAndFechaCierreGreaterThanEqualAndFechaCierreLessThanAndNegocioOrderByFechaCierreDesc(
            EstadoCaja estado,
            Instant desdeInclusive,
            Instant hastaExclusive,
            Negocio negocio
    );

    @EntityGraph(attributePaths = {"usuarioApertura", "usuarioCierre"})
    Page<Caja> findByEstadoAndFechaCierreGreaterThanEqualAndFechaCierreLessThanAndNegocioOrderByFechaCierreDesc(
            EstadoCaja estado,
            Instant desdeInclusive,
            Instant hastaExclusive,
            Negocio negocio,
            Pageable pageable
    );

    long countByEstadoAndFechaCierreGreaterThanEqualAndFechaCierreLessThanAndNegocio(
            EstadoCaja estado,
            Instant desdeInclusive,
            Instant hastaExclusive,
            Negocio negocio
    );

    @Query(value = """
            SELECT (c.fecha_cierre AT TIME ZONE :zonaHoraria)::date AS dia,
                   COALESCE(SUM(c.utilidad), 0) AS total
            FROM caja c
            WHERE c.estado = 'CERRADA'
              AND c.negocio_id = :negocioId
              AND c.fecha_cierre >= :desde
              AND c.fecha_cierre < :hasta
            GROUP BY 1
            ORDER BY 1
            """, nativeQuery = true)
    List<UtilidadDiariaProjection> calcularUtilidadDiariaEnRango(
            @Param("negocioId") Long negocioId,
            @Param("desde") Instant desde,
            @Param("hasta") Instant hasta,
            @Param("zonaHoraria") String zonaHoraria
    );


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
