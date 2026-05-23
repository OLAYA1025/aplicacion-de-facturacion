package com.facturacion.negocio.services;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.facturacion.negocio.domain.Categoria;
import com.facturacion.negocio.domain.Negocio;
import com.facturacion.negocio.domain.Producto;
import com.facturacion.negocio.enumerated.TipoGestionProducto;
import com.facturacion.negocio.enumerated.TipoImpuesto;
import com.facturacion.negocio.repository.CategoriaRepository;
import com.facturacion.negocio.repository.ProductoRepository;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final TenantService tenantService;

    public ProductoService(ProductoRepository productoRepository,
                           CategoriaRepository categoriaRepository,
                           TenantService tenantService) {
        this.productoRepository = productoRepository;
        this.categoriaRepository = categoriaRepository;
        this.tenantService = tenantService;
    }

    public Producto obtenerProducto(Long id) {
        Negocio negocio = tenantService.getNegocioActual();
        return productoRepository.findByIdAndNegocio(id, negocio)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                ("Producto no encontrado"))
                        );
    }

    public List<Producto> obtenerProductos(){
        return productoRepository.findAllByNegocioOrderByNombreAsc(tenantService.getNegocioActual());
    }

    public List<Producto> obtenerProductosInventariables(){
        return productoRepository.findInventariablesByNegocio(tenantService.getNegocioActual());
    }

    public List<Producto> obtenerProductosPreparacion(){
        return productoRepository.findPreparacionByNegocio(tenantService.getNegocioActual());
    }

    public List<Producto> obtenerProductosPorCategoria(Long categoriaId){
        Negocio negocio = tenantService.getNegocioActual();
        Categoria categoria = categoriaRepository.findByIdAndNegocio(categoriaId, negocio)
                .orElseThrow(()->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "La categoria no existe")
                );
        return productoRepository.findByCategoriaAndNegocio(categoria, negocio);
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Producto crearProducto(String nombre,
                                  String codigo,
                                  BigDecimal precio,
                                  String descripcion,
                                  Long categoriaId,
                                  TipoGestionProducto tipoGestion,
                                  TipoImpuesto tipoImpuesto,
                                  BigDecimal impuestoPorcentaje,
                                  Integer stockInicial,
                                  Integer stockMinimo){
        if(nombre == null || precio == null || categoriaId == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos invalidos");
        }
        Negocio negocio = tenantService.getNegocioActual();
        if(productoRepository.existsByNombreAndNegocio(nombre, negocio)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El producto ya existe");
        }
        String codigoNormalizado = normalizarCodigo(codigo);
        validarCodigoDisponible(codigoNormalizado, negocio);
        Categoria categoria = obtenerCategoriaExistente(categoriaId);

        try {
            Producto producto = new Producto(
                    nombre,
                    precio,
                    descripcion,
                    categoria,
                    tipoGestion,
                    tipoImpuesto,
                    impuestoPorcentaje,
                    stockInicial,
                    stockMinimo
            );
            producto.setCodigo(codigoNormalizado);
            producto.setNegocio(negocio);
            return productoRepository.save(producto);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public Producto editarProducto(Long id,
                                   String nombre,
                                   String codigo,
                                   BigDecimal precio,
                                   String descripcion,
                                   Long categoriaId,
                                   TipoGestionProducto tipoGestion,
                                   TipoImpuesto tipoImpuesto,
                                   BigDecimal impuestoPorcentaje,
                                   Integer stockMinimo){
        if(id  == null || nombre == null || precio == null || categoriaId == null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Datos invalidos");
        }

        Negocio negocio = tenantService.getNegocioActual();
        if(productoRepository.existsByNombreAndIdNotAndNegocio(nombre, id, negocio)){
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El nombre de producto ya existe");
        }
        String codigoNormalizado = normalizarCodigo(codigo);
        validarCodigoDisponibleParaEdicion(codigoNormalizado, id, negocio);

        Producto producto = productoRepository.findByIdAndNegocio(id, negocio)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "El producto no existe")
                );
        Categoria categoria = obtenerCategoriaExistente(categoriaId);
        try {
            producto.actualizarProducto(
                    nombre,
                    precio,
                    descripcion,
                    categoria,
                    tipoGestion,
                    tipoImpuesto,
                    impuestoPorcentaje,
                    stockMinimo
            );
            producto.actualizarCodigo(codigoNormalizado);
            return productoRepository.save(producto);
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    public Producto desactivarProducto(Long id){
        Producto producto = productoRepository.findByIdAndNegocio(id, tenantService.getNegocioActual())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "El producto no existe")
                );
        producto.desactivar();
        return productoRepository.save(producto);
    }

    public Producto activarProducto(Long id){
        Producto producto = productoRepository.findByIdAndNegocio(id, tenantService.getNegocioActual())
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "El producto no existe")
                );
        producto.activar();
        return productoRepository.save(producto);
    }

    private Categoria obtenerCategoriaExistente(Long categoriaId) {
        return categoriaRepository.findByIdAndNegocio(categoriaId, tenantService.getNegocioActual())
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "categoria no encontrada")
                );
    }

    private String normalizarCodigo(String codigo) {
        if (codigo == null) {
            return null;
        }
        String codigoNormalizado = codigo.trim();
        return codigoNormalizado.isEmpty() ? null : codigoNormalizado;
    }

    private void validarCodigoDisponible(String codigo, Negocio negocio) {
        if (codigo == null) {
            return;
        }
        if (productoRepository.existsByCodigoAndNegocio(codigo, negocio)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un producto con ese codigo en este negocio"
            );
        }
    }

    private void validarCodigoDisponibleParaEdicion(String codigo, Long id, Negocio negocio) {
        if (codigo == null) {
            return;
        }
        if (productoRepository.existsByCodigoAndIdNotAndNegocio(codigo, id, negocio)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ya existe un producto con ese codigo en este negocio"
            );
        }
    }

}
