package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.domain.NumberingRange;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface NumberingRangeRepository extends JpaRepository<NumberingRange, Long> {

    Optional<NumberingRange> findByIdAndNegocioId(Long id, Long negocioId);

    Optional<NumberingRange> findFirstByConfiguracionIdAndActiveTrueOrderByIdAsc(Long configuracionId);

    List<NumberingRange> findByConfiguracionIdOrderByIdAsc(Long configuracionId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select nr
        from NumberingRange nr
        where nr.id = :id
        """)
    Optional<NumberingRange> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select nr
        from NumberingRange nr
        where nr.id = :id
          and nr.negocio.id = :negocioId
        """)
    Optional<NumberingRange> findByIdForUpdateAndNegocioId(@Param("id") Long id, @Param("negocioId") Long negocioId);
}
