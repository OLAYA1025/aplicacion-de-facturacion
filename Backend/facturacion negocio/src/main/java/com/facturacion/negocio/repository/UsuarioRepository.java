package com.facturacion.negocio.repository;


import com.facturacion.negocio.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);
    boolean existsByUsernameAndIdNot(String username, Long id);

    List<Usuario> findByActivoTrue();

    boolean existsUsuarioById(Long id);
}
