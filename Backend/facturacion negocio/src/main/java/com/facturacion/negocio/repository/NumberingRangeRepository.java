package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.NumberingRange;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface NumberingRangeRepository extends JpaRepository<NumberingRange, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        select nr
        from NumberingRange nr
        where nr.id = :id
        """)
    Optional<NumberingRange> findByIdForUpdate(@Param("id") Long id);
}