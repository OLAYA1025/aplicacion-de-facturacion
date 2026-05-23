package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.Categoria;
import com.facturacion.negocio.domain.Negocio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    boolean existsByNombreIgnoreCaseAndNegocio(String nombre, Negocio negocio);

    boolean existsByNombreIgnoreCaseAndIdNotAndNegocio(String nombre, Long id, Negocio negocio);

    List<Categoria> findAllByNegocioOrderByNombreAsc(Negocio negocio);

    Optional<Categoria> findByIdAndNegocio(Long id, Negocio negocio);
}
