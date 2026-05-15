package com.facturacion.negocio.repository;

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

    List<NotaPredefinida> findByGrupoIdOrderByDescripcionAsc(Long grupoId);

    Optional<NotaPredefinida> findByIdAndGrupoId(Long notaId, Long grupoId);

    boolean existsByGrupoIdAndDescripcionIgnoreCase(Long grupoId, String descripcion);

    boolean existsByGrupoIdAndDescripcionIgnoreCaseAndIdNot(Long grupoId, String descripcion, Long id);

    @Query("""
            SELECT n
            FROM NotaPredefinida n
            JOIN FETCH n.grupo g
            JOIN FETCH g.categoria c
            WHERE n.id IN :ids
            """)
    List<NotaPredefinida> findByIdsWithGrupoYCategoria(@Param("ids") Collection<Long> ids);
}
