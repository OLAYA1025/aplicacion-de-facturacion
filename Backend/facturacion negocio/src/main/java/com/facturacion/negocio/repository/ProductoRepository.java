package com.facturacion.negocio.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.facturacion.negocio.domain.Categoria;
import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.domain.Producto;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByNombreAndNegocio(String nombre, Negocio negocio);

    @Query("""
            SELECT p
            FROM Producto p
            WHERE p.negocio = :negocio
              AND p.tipoGestion = com.facturacion.negocio.enumerated.TipoGestionProducto.INVENTARIABLE
            """)
    List<Producto> findInventariablesByNegocio(@Param("negocio") Negocio negocio);

    @Query("""
            SELECT p
            FROM Producto p
            WHERE p.negocio = :negocio
              AND p.tipoGestion = com.facturacion.negocio.enumerated.TipoGestionProducto.PREPARACION
            """)
    List<Producto> findPreparacionByNegocio(@Param("negocio") Negocio negocio);

    @Query("SELECT p FROM Producto p WHERE p.categoria = :categoria AND p.negocio = :negocio")
    List<Producto> findByCategoriaAndNegocio(@Param("categoria") Categoria categoria, @Param("negocio") Negocio negocio);

    Boolean existsByNombreAndNegocio(String nombre, Negocio negocio);

    Boolean existsByNombreAndIdNotAndNegocio(String nombre, Long id, Negocio negocio);

    Boolean existsByCodigoAndNegocio(String codigo, Negocio negocio);

    Boolean existsByCodigoAndIdNotAndNegocio(String codigo, Long id, Negocio negocio);

    List<Producto> findByActivoTrueAndNegocioOrderByNombreAsc(Negocio negocio);

    List<Producto> findAllByNegocioOrderByNombreAsc(Negocio negocio);

    Optional<Producto> findByIdAndNegocio(Long id, Negocio negocio);

    boolean existsByIdAndNegocio(Long id, Negocio negocio);


}
