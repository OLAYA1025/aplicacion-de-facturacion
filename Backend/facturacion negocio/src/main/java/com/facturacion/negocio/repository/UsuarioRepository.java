package com.facturacion.negocio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;

import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.domain.Usuario;
import com.facturacion.negocio.enumerated.Rol;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    List<Usuario> findAllByUsername(String username);

    @EntityGraph(attributePaths = "negocio")
    List<Usuario> findAllWithNegocioByUsername(String username);

    Optional<Usuario> findByIdAndNegocioId(Long id, Long negocioId);

    Optional<Usuario> findByIdAndNegocio(Long id, Negocio negocio);

    Optional<Usuario> findByIdAndRol(Long id, Rol rol);

    Optional<Usuario> findByUsernameAndNegocioId(String username, Long negocioId);

    Optional<Usuario> findByUsernameAndNegocio(String username, Negocio negocio);

    boolean existsByUsernameAndNegocio(String username, Negocio negocio);

    boolean existsByUsername(String username);

    boolean existsByUsernameAndIdNotAndNegocio(String username, Long id, Negocio negocio);

    List<Usuario> findByActivoTrueAndNegocioOrderByNombreAsc(Negocio negocio);

    List<Usuario> findAllByNegocioOrderByNombreAsc(Negocio negocio);

    boolean existsByRol(Rol rol);

    boolean existsUsuarioById(Long id);
}
