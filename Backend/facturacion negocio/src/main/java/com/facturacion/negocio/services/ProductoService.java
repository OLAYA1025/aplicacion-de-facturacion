package com.facturacion.negocio.services;

import com.facturacion.negocio.domain.Categoria;
import com.facturacion.negocio.domain.Producto;
import com.facturacion.negocio.enumerated.TipoGestionProducto;
import com.facturacion.negocio.repository.CategoriaRepository;
import com.facturacion.negocio.repository.ProductoRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaRepository categoriaRepository) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
    }

    public Producto obtenerProducto(Long codigo) {
        return productoRepository.findById(codigo)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                ("Producto no encontrado"))
                        );
    }

    public List<Producto> obtenerProductos(){
        return productoRepository.findAll();
    }

    public List<Producto> obtenerProductosInventariables(){
        return productoRepository.findInventariables();
    }

    public List<Producto> obtenerProductosPreparacion(){
        return productoRepository.findPreparacion();
    }

    public List<Producto> obtenerProductosPorCategoria(Long categoriaId){
        Categoria categoria = categoriaRepository.findById(categoriaId)
                .orElseThrow(()->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "La categoria no existe")
                );
        return productoRepository.findByCategoria(categoria);
    }

    public Producto crearProducto(String nombre,
                                  BigDecimal precio,
                                  String descripcion,
                                  Long categoriaId,
                                  TipoGestionProducto tipoGestion,
                                  Integer stockInicial,
                                  Integer stockMinimo){
        if(nombre == null || precio == null || categoriaId == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos invalidos");
        }
        if(productoRepository.existsByNombre(nombre)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El producto ya existe");
        }
        Categoria categoria = obtenerCategoriaExistente(categoriaId);

        Producto producto = new Producto(nombre, precio, descripcion, categoria, tipoGestion, stockInicial, stockMinimo);

        return productoRepository.save(producto);
    }

    public Producto editarProducto(Long id,
                                   String nombre,
                                   BigDecimal precio,
                                   String descripcion,
                                   Long categoriaId,
                                   TipoGestionProducto tipoGestion,
                                   Integer stockMinimo){
        if(id  == null || nombre == null || precio == null || categoriaId == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos invalidos");
        }

        if(productoRepository.existsByNombreAndCodigoNot(nombre, id)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de producto ya existe");
        }

        Producto producto = productoRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "El producto no existe")
                );
        Categoria categoria = obtenerCategoriaExistente(categoriaId);
        producto.actualizarProducto(nombre, precio, descripcion, categoria, tipoGestion, stockMinimo);
        return productoRepository.save(producto);
    }

    public Producto desactivarProducto(Long id){
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "El producto no existe")
                );
        producto.desactivar();
        return productoRepository.save(producto);
    }

    public Producto activarProducto(Long id){
        Producto producto = productoRepository.findById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "El producto no existe")
                );
        producto.activar();
        return productoRepository.save(producto);
    }

    private Categoria obtenerCategoriaExistente(Long categoriaId) {
        return categoriaRepository.findById(categoriaId)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "categoria no encontrada")
                );
    }

}
