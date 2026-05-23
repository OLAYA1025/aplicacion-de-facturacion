package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.domain.NotaPredefinida;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface NotaPredefinidaRepository extends JpaRepository<NotaPredefinida, Long> {

    List<NotaPredefinida> findByGrupoIdAndNegocioOrderByDescripcionAsc(Long grupoId, Negocio negocio);

    Optional<NotaPredefinida> findByIdAndGrupoIdAndNegocio(Long notaId, Long grupoId, Negocio negocio);

    boolean existsByGrupoIdAndDescripcionIgnoreCaseAndNegocio(Long grupoId, String descripcion, Negocio negocio);

    boolean existsByGrupoIdAndDescripcionIgnoreCaseAndIdNotAndNegocio(Long grupoId,
                                                                      String descripcion,
                                                                      Long id,
                                                                      Negocio negocio);

    @Query("""
            SELECT n
            FROM NotaPredefinida n
            JOIN FETCH n.grupo g
            JOIN FETCH g.categoria c
            WHERE n.id IN :ids
              AND n.negocio = :negocio
            """)
    List<NotaPredefinida> findByIdsWithGrupoYCategoria(@Param("ids") Collection<Long> ids,
                                                       @Param("negocio") Negocio negocio);
}
