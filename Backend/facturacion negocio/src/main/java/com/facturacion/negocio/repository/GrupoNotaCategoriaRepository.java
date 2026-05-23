package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.domain.GrupoNotaCategoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrupoNotaCategoriaRepository extends JpaRepository<GrupoNotaCategoria, Long> {

    List<GrupoNotaCategoria> findByCategoriaIdAndNegocioOrderByNombreAsc(Long categoriaId, Negocio negocio);

    Optional<GrupoNotaCategoria> findByIdAndCategoriaIdAndNegocio(Long id, Long categoriaId, Negocio negocio);

    boolean existsByCategoriaIdAndNombreIgnoreCaseAndNegocio(Long categoriaId, String nombre, Negocio negocio);

    boolean existsByCategoriaIdAndNombreIgnoreCaseAndIdNot(Long categoriaId, String nombre, Long id, Negocio negocio);

    @Query("""
            SELECT DISTINCT g
            FROM GrupoNotaCategoria g
            LEFT JOIN FETCH g.notas n
            WHERE g.categoria.id = :categoriaId
              AND g.negocio = :negocio
            """)
    List<GrupoNotaCategoria> findByCategoriaIdWithNotas(@Param("categoriaId") Long categoriaId,
                                                        @Param("negocio") Negocio negocio);
}
