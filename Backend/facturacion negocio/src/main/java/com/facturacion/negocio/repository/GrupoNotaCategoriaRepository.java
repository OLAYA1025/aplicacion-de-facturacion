package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.GrupoNotaCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrupoNotaCategoriaRepository extends JpaRepository<GrupoNotaCategoria, Long> {

    List<GrupoNotaCategoria> findByCategoriaIdOrderByNombreAsc(Long categoriaId);

    Optional<GrupoNotaCategoria> findByIdAndCategoriaId(Long id, Long categoriaId);

    boolean existsByCategoriaIdAndNombreIgnoreCase(Long categoriaId, String nombre);

    boolean existsByCategoriaIdAndNombreIgnoreCaseAndIdNot(Long categoriaId, String nombre, Long id);

    @Query("""
            SELECT DISTINCT g
            FROM GrupoNotaCategoria g
            LEFT JOIN FETCH g.notas n
            WHERE g.categoria.id = :categoriaId
            """)
    List<GrupoNotaCategoria> findByCategoriaIdWithNotas(@Param("categoriaId") Long categoriaId);
}
