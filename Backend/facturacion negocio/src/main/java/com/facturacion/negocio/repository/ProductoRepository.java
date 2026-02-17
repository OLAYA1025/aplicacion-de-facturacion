package com.facturacion.negocio.repository;

import com.facturacion.negocio.domain.Categoria;
import com.facturacion.negocio.domain.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    Optional<Producto> findByNombre(String nombre);

    @Query("SELECT p FROM Producto p WHERE p.tipoGestion = com.facturacion.negocio.enumerated.TipoGestionProducto.INVENTARIABLE")
    List<Producto> findInventariables();

    @Query("SELECT p FROM Producto p WHERE p.tipoGestion = com.facturacion.negocio.enumerated.TipoGestionProducto.PREPARACION")
    List<Producto> findPreparacion();

    @Query("SELECT p FROM Producto p WHERE p.categoria = :categoria")
    List<Producto> findByCategoria(@Param("categoria") Categoria categoria);



    Boolean existsByNombre(String nombre);

    Boolean existsByNombreAndCodigoNot(String nombre, Long codigo);

    List<Producto> findByActivoTrue();

    Optional<Producto> findByCodigo(Long codigo);

    boolean existsByCodigo(Long codigo);


}
