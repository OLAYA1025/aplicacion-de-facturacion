import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnDestroy, OnInit, inject } from '@angular/core';
import { NgForm } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { finalize } from 'rxjs';
import { Producto } from '../../../core/models/producto.model';
import { ProductoFormComponent } from '../components/producto-form/producto-form.component';
import { ProductosEdicionModalComponent } from '../components/productos-edicion-modal/productos-edicion-modal.component';
import { ProductosFiltrosComponent } from '../components/productos-filtros/productos-filtros.component';
import { ProductosHeaderComponent } from '../components/productos-header/productos-header.component';
import { ProductosListadoComponent } from '../components/productos-listado/productos-listado.component';
import { ProductosToastExitoComponent } from '../components/productos-toast-exito/productos-toast-exito.component';
import { actualizarProducto } from '../models/actualizarProducto.model';
import { Categoria } from '../models/categoria.model';
import { CrearProducto } from '../models/crearProducto.model';
import {
  CampoOrden,
  DireccionOrden,
  EstadoFiltro,
  ProductoCrearFormState,
  ProductoEditarFormState,
  SelectorCategoriaCrear,
  TabProductos,
  TipoImpuesto,
} from '../models/productos-ui.model';
import { CategoriaService } from '../services/categoria.service';
import { ProductoService } from '../services/producto.service';
import { ModalMensajeService } from '../../../shared/services/modal-mensaje.service';
import { obtenerMensajeError } from '../../../shared/utils/error-message.util';

@Component({
  selector: 'app-productos',
  standalone: true,
  imports: [
    CommonModule,
    ProductosHeaderComponent,
    ProductosFiltrosComponent,
    ProductosListadoComponent,
    ProductoFormComponent,
    ProductosToastExitoComponent,
    ProductosEdicionModalComponent,
  ],
  templateUrl: './productos.component.html',
})
export class ProductosComponent implements OnInit, OnDestroy {
  private static readonly IVA_PERMITIDOS = [0, 5, 19] as const;
  private static readonly INC_PORCENTAJE = 8;

  private readonly cdr = inject(ChangeDetectorRef);
  private readonly productoService = inject(ProductoService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly route = inject(ActivatedRoute);
  private readonly modalMensajeService = inject(ModalMensajeService);

  private toastTimeoutId: ReturnType<typeof setTimeout> | null = null;

  tabActual: TabProductos = 'inventariables';

  loadingProductos: boolean = false;
  guardandoEstadoIds = new Set<number>();
  guardandoProducto: boolean = false;
  guardandoEdicion: boolean = false;
  guardandoCategoria: boolean = false;

  productos: Producto[] = [];
  productosFiltrados: Producto[] = [];
  categorias: Categoria[] = [];

  busqueda: string = '';
  filtroCategoriaId: number | null = null;
  filtroEstado: EstadoFiltro = 'TODOS';
  campoOrden: CampoOrden = 'nombre';
  direccionOrden: DireccionOrden = 'asc';

  mostrarCrearCategoria: boolean = false;
  nuevaCategoriaNombre: string = '';
  selectorCategoriaCrear: SelectorCategoriaCrear = null;
  modalCreacionAbierto: boolean = false;
  toastExitoVisible: boolean = false;
  mensajeExitoCreacion: string = '';

  modalEdicionAbierto: boolean = false;
  productoEditando: Producto | null = null;

  errorCrearProducto: string | null = null;
  errorEditarProducto: string | null = null;
  errorCrearCategoria: string | null = null;

  nuevoProducto: ProductoCrearFormState = this.crearEstadoInicialFormulario();
  edicionProducto: ProductoEditarFormState = this.crearEstadoInicialEdicion();

  ngOnInit(): void {
    this.tabActual = this.obtenerTabDesdeRuta();
    this.cargarCategorias();

    if (this.esVistaListado) {
      this.cargarProductos();
    }
  }

  ngOnDestroy(): void {
    if (this.toastTimeoutId !== null) {
      clearTimeout(this.toastTimeoutId);
      this.toastTimeoutId = null;
    }
  }

  get esVistaListado(): boolean {
    return this.tabActual === 'inventariables' || this.tabActual === 'preparaciones';
  }

  get esInventariable(): boolean {
    return this.tabActual === 'inventariables';
  }

  cargarProductos(): void {
    this.loadingProductos = true;

    const request$ = this.esInventariable
      ? this.productoService.obtenerProductosInventariables()
      : this.productoService.obtenerProductosPreparacion();

    request$
      .pipe(
        finalize(() => {
          this.loadingProductos = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (productos) => {
          this.productos = productos;
          this.sincronizarCategoriasDesdeProductos(productos);
          this.aplicarFiltrosYOrden();
          this.cdr.detectChanges();
        },
        error: (error) => {
          this.mostrarError(
            'No se pudieron cargar los productos',
            'Verifica la conexión e intenta nuevamente.',
            error
          );
          this.productos = [];
          this.productosFiltrados = [];
          this.cdr.detectChanges();
        },
      });
  }

  aplicarFiltrosYOrden(): void {
    let resultado = [...this.productos];
    const termino = this.busqueda.trim().toLowerCase();

    if (termino) {
      resultado = resultado.filter(
        (producto) =>
          producto.nombre.toLowerCase().includes(termino) ||
          producto.codigo.toString().includes(termino)
      );
    }

    if (this.filtroCategoriaId !== null) {
      resultado = resultado.filter((producto) => producto.categoria.id === this.filtroCategoriaId);
    }

    if (this.filtroEstado === 'ACTIVOS') {
      resultado = resultado.filter((producto) => producto.activo);
    }

    if (this.filtroEstado === 'DESACTIVADOS') {
      resultado = resultado.filter((producto) => !producto.activo);
    }

    const factor = this.direccionOrden === 'asc' ? 1 : -1;
    resultado.sort((a, b) => this.compararProductos(a, b) * factor);

    this.productosFiltrados = resultado;
  }

  async cambiarEstadoProducto(producto: Producto): Promise<void> {
    if (this.guardandoEstadoIds.has(producto.codigo)) {
      return;
    }

    const accion = producto.activo ? 'desactivar' : 'activar';
    const confirmado = await this.modalMensajeService.confirmar({
      titulo: `¿Deseas ${accion} "${producto.nombre}"?`,
      mensaje: producto.activo
        ? 'El producto dejará de estar disponible para nuevos pedidos.'
        : 'El producto volverá a estar disponible para nuevos pedidos.',
      textoConfirmar: producto.activo ? 'Desactivar' : 'Activar',
    });

    if (!confirmado || this.guardandoEstadoIds.has(producto.codigo)) {
      return;
    }

    this.guardandoEstadoIds.add(producto.codigo);
    const request$ = producto.activo
      ? this.productoService.desactivarProducto(producto.codigo)
      : this.productoService.activarProducto(producto.codigo);

    request$
      .pipe(
        finalize(() => {
          this.guardandoEstadoIds.delete(producto.codigo);
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (productoActualizado) => {
          this.reemplazarProductoEnLista(productoActualizado);
          this.cdr.detectChanges();
        },
        error: (error) => {
          this.mostrarError(
            'No se pudo actualizar el estado del producto',
            'Intenta de nuevo en unos segundos.',
            error
          );
          this.cdr.detectChanges();
        },
      });
  }

  abrirModalEdicion(producto: Producto): void {
    const tipoImpuesto = this.resolverTipoImpuestoProducto(producto);
    const porcentajeActual = this.resolverImpuestoPorcentajeProducto(producto);

    this.productoEditando = producto;
    this.modalEdicionAbierto = true;
    this.errorEditarProducto = null;
    this.edicionProducto = {
      nombre: producto.nombre,
      precio: producto.precio,
      tipoGestion: producto.tipoGestion,
      tipoImpuesto: producto.tipoGestion === 'INVENTARIABLE' ? tipoImpuesto : 'INC',
      impuestoPorcentaje:
        producto.tipoGestion === 'INVENTARIABLE'
          ? this.normalizarPorcentajeSegunTipo(tipoImpuesto, porcentajeActual)
          : ProductosComponent.INC_PORCENTAJE,
      stockMinimo: producto.tipoGestion === 'INVENTARIABLE' ? producto.stockMinimo : null,
      categoriaId: producto.categoria.id,
    };
  }

  cerrarModalEdicion(): void {
    this.modalEdicionAbierto = false;
    this.productoEditando = null;
    this.errorEditarProducto = null;
    this.edicionProducto = this.crearEstadoInicialEdicion();
  }

  onTipoGestionEdicionChange(): void {
    if (this.edicionProducto.tipoGestion === 'PREPARACION') {
      this.edicionProducto.tipoImpuesto = 'INC';
      this.edicionProducto.impuestoPorcentaje = ProductosComponent.INC_PORCENTAJE;
      this.edicionProducto.stockMinimo = null;
      return;
    }

    this.edicionProducto.tipoImpuesto = this.edicionProducto.tipoImpuesto ?? 'IVA';
    this.edicionProducto.impuestoPorcentaje = this.normalizarPorcentajeSegunTipo(
      this.edicionProducto.tipoImpuesto,
      this.edicionProducto.impuestoPorcentaje
    );

    if (this.edicionProducto.stockMinimo === null) {
      this.edicionProducto.stockMinimo = 0;
    }
  }

  onTipoImpuestoEdicionChange(): void {
    this.edicionProducto.impuestoPorcentaje = this.normalizarPorcentajeSegunTipo(
      this.edicionProducto.tipoImpuesto,
      this.edicionProducto.impuestoPorcentaje
    );
  }

  guardarEdicion(form: NgForm): void {
    if (!this.productoEditando) {
      return;
    }

    this.errorEditarProducto = this.validarEdicion(form);
    if (this.errorEditarProducto) {
      return;
    }

    const payload: actualizarProducto = {
      nombre: this.edicionProducto.nombre.trim(),
      precio: Number(this.edicionProducto.precio),
      tipoGestion: this.edicionProducto.tipoGestion,
      ...(this.edicionProducto.tipoGestion === 'INVENTARIABLE'
        ? {
            tipoImpuesto: this.edicionProducto.tipoImpuesto,
            impuestoPorcentaje: Number(this.edicionProducto.impuestoPorcentaje),
          }
        : {}),
      stockMinimo:
        this.edicionProducto.tipoGestion === 'INVENTARIABLE'
          ? Number(this.edicionProducto.stockMinimo)
          : 0,
      categoriaId: Number(this.edicionProducto.categoriaId),
    };

    this.guardandoEdicion = true;
    this.productoService
      .actualizarProducto(this.productoEditando.codigo, payload)
      .pipe(
        finalize(() => {
          this.guardandoEdicion = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          this.cerrarModalEdicion();
          this.cargarProductos();
          this.cdr.detectChanges();
        },
        error: (error) => {
          this.mostrarError(
            'No se pudo editar el producto',
            'Verifica los datos e intenta de nuevo.',
            error
          );
          this.errorEditarProducto = 'No se pudo guardar la edición. Verifica los datos e intenta de nuevo.';
          this.cdr.detectChanges();
        },
      });
  }

  onTipoGestionCrearChange(): void {
    if (this.nuevoProducto.tipoGestion === 'PREPARACION') {
      this.nuevoProducto.tipoImpuesto = 'INC';
      this.nuevoProducto.impuestoPorcentaje = ProductosComponent.INC_PORCENTAJE;
      this.nuevoProducto.stockInicial = null;
      this.nuevoProducto.stockMinimo = null;
      return;
    }

    this.nuevoProducto.tipoImpuesto = this.nuevoProducto.tipoImpuesto ?? 'IVA';
    this.nuevoProducto.impuestoPorcentaje = this.normalizarPorcentajeSegunTipo(
      this.nuevoProducto.tipoImpuesto,
      this.nuevoProducto.impuestoPorcentaje
    );

    if (this.nuevoProducto.stockInicial === null) {
      this.nuevoProducto.stockInicial = 0;
    }

    if (this.nuevoProducto.stockMinimo === null) {
      this.nuevoProducto.stockMinimo = 0;
    }
  }

  onTipoImpuestoCrearChange(): void {
    this.nuevoProducto.impuestoPorcentaje = this.normalizarPorcentajeSegunTipo(
      this.nuevoProducto.tipoImpuesto,
      this.nuevoProducto.impuestoPorcentaje
    );
  }

  abrirModalCrearProducto(): void {
    this.errorCrearProducto = null;
    this.errorCrearCategoria = null;
    this.mostrarCrearCategoria = false;
    this.modalCreacionAbierto = true;
    this.sincronizarCategoriaSeleccionadaEnFormulario();
    this.cdr.detectChanges();
  }

  cerrarModalCrearProducto(): void {
    this.modalCreacionAbierto = false;
    this.restablecerFormularioCreacion();
    this.cdr.detectChanges();
  }

  cerrarToastExito(): void {
    this.toastExitoVisible = false;
    if (this.toastTimeoutId !== null) {
      clearTimeout(this.toastTimeoutId);
      this.toastTimeoutId = null;
    }
    this.cdr.detectChanges();
  }

  onCategoriaCrearChange(value: number | 'crear' | null): void {
    if (value === 'crear') {
      this.mostrarCrearCategoria = true;
      this.selectorCategoriaCrear = this.nuevoProducto.categoriaId;
      return;
    }

    this.mostrarCrearCategoria = false;
    this.errorCrearCategoria = null;
    this.nuevoProducto.categoriaId = value;
    this.selectorCategoriaCrear = value;
  }

  crearCategoria(): void {
    const nombreCategoria = this.nuevaCategoriaNombre.trim();
    if (nombreCategoria.length < 2) {
      this.errorCrearCategoria = 'El nombre de la categoría debe tener mínimo 2 caracteres.';
      return;
    }

    this.errorCrearCategoria = null;
    this.guardandoCategoria = true;
    this.categoriaService
      .crearCategoria({ nombre: nombreCategoria })
      .pipe(
        finalize(() => {
          this.guardandoCategoria = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (categoria) => {
          this.categorias = this.normalizarCategorias([...this.categorias, categoria]);
          this.nuevoProducto.categoriaId = categoria.id;
          this.selectorCategoriaCrear = categoria.id;
          this.nuevaCategoriaNombre = '';
          this.mostrarCrearCategoria = false;
          this.cdr.detectChanges();
        },
        error: (error) => {
          this.mostrarError(
            'No se pudo crear la categoría',
            'Verifica que el nombre no exista e intenta de nuevo.',
            error
          );
          this.errorCrearCategoria =
            'No se pudo crear la categoría. Verifica que el nombre no exista e intenta de nuevo.';
          this.cdr.detectChanges();
        },
      });
  }

  cancelarCrearCategoria(): void {
    this.mostrarCrearCategoria = false;
    this.nuevaCategoriaNombre = '';
    this.errorCrearCategoria = null;
    this.selectorCategoriaCrear = this.nuevoProducto.categoriaId;
  }

  crearProducto(form: NgForm): void {
    this.errorCrearProducto = this.validarCreacion(form);
    if (this.errorCrearProducto) {
      return;
    }

    const payload: CrearProducto = {
      nombre: this.nuevoProducto.nombre.trim(),
      precio: Number(this.nuevoProducto.precio),
      tipoGestion: this.nuevoProducto.tipoGestion,
      ...(this.nuevoProducto.tipoGestion === 'INVENTARIABLE'
        ? {
            tipoImpuesto: this.nuevoProducto.tipoImpuesto,
            impuestoPorcentaje: Number(this.nuevoProducto.impuestoPorcentaje),
          }
        : {}),
      stockInicial:
        this.nuevoProducto.tipoGestion === 'INVENTARIABLE'
          ? Number(this.nuevoProducto.stockInicial)
          : 0,
      stockMinimo:
        this.nuevoProducto.tipoGestion === 'INVENTARIABLE'
          ? Number(this.nuevoProducto.stockMinimo)
          : 0,
      categoriaId: Number(this.nuevoProducto.categoriaId),
    };

    this.guardandoProducto = true;
    this.productoService
      .crearProducto(payload)
      .pipe(
        finalize(() => {
          this.guardandoProducto = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (productoCreado) => {
          this.errorCrearProducto = null;
          this.sincronizarCategoriasDesdeProductos([productoCreado]);
          this.restablecerFormularioCreacion(form);

          if (this.modalCreacionAbierto) {
            this.cerrarModalCrearProducto();
            this.cargarProductos();
          }

          this.mostrarToastExitoCreacion(productoCreado.nombre);
          this.cdr.detectChanges();
        },
        error: (error) => {
          this.mostrarError(
            'No se pudo crear el producto',
            'Verifica los datos e intenta de nuevo.',
            error
          );
          this.errorCrearProducto = 'No fue posible crear el producto. Verifica los datos e intenta de nuevo.';
          this.cdr.detectChanges();
        },
      });
  }

  stockDisponible(producto: Producto): number {
    return producto.stockActual - producto.stockReservado;
  }

  private obtenerTabDesdeRuta(): TabProductos {
    const tab = this.route.snapshot.data['productosTab'];
    if (tab === 'preparaciones' || tab === 'inventariables' || tab === 'crear-producto') {
      return tab;
    }
    return 'inventariables';
  }

  private cargarCategorias(): void {
    this.categoriaService.obtenerCategorias().subscribe({
      next: (categorias) => {
        this.categorias = this.normalizarCategorias(categorias);
        this.sincronizarCategoriaSeleccionadaEnFormulario();
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.mostrarError(
          'No se pudieron cargar las categorías',
          'Intenta nuevamente en unos segundos.',
          error
        );
        this.sincronizarCategoriasDesdeProductos(this.productos);
        this.cdr.detectChanges();
      },
    });
  }

  private compararProductos(a: Producto, b: Producto): number {
    if (this.campoOrden === 'precio') {
      return a.precio - b.precio;
    }

    if (this.campoOrden === 'stockDisponible') {
      return this.stockDisponible(a) - this.stockDisponible(b);
    }

    return a.nombre.localeCompare(b.nombre, 'es', { sensitivity: 'base' });
  }

  private reemplazarProductoEnLista(productoActualizado: Producto): void {
    this.productos = this.productos.map((producto) =>
      producto.codigo === productoActualizado.codigo ? productoActualizado : producto
    );
    this.sincronizarCategoriasDesdeProductos([productoActualizado]);
    this.aplicarFiltrosYOrden();
  }

  private sincronizarCategoriasDesdeProductos(productos: Producto[]): void {
    if (!productos.length && this.categorias.length) {
      this.sincronizarCategoriaSeleccionadaEnFormulario();
      return;
    }

    const categoriasDesdeProductos = productos.map((producto) => ({
      id: producto.categoria.id,
      nombre: producto.categoria.nombre,
    }));

    this.categorias = this.normalizarCategorias([...this.categorias, ...categoriasDesdeProductos]);
    this.sincronizarCategoriaSeleccionadaEnFormulario();
  }

  private sincronizarCategoriaSeleccionadaEnFormulario(): void {
    const categoriaValida = this.categorias.some(
      (categoria) => categoria.id === this.nuevoProducto.categoriaId
    );

    if (categoriaValida) {
      this.selectorCategoriaCrear = this.nuevoProducto.categoriaId;
      return;
    }

    if (!this.categorias.length) {
      this.nuevoProducto.categoriaId = null;
      this.selectorCategoriaCrear = null;
      return;
    }

    this.nuevoProducto.categoriaId = this.categorias[0].id;
    this.selectorCategoriaCrear = this.nuevoProducto.categoriaId;
  }

  private normalizarCategorias(categorias: Categoria[]): Categoria[] {
    const categoriasMap = new Map<number, Categoria>();
    categorias.forEach((categoria) => {
      categoriasMap.set(categoria.id, categoria);
    });

    return Array.from(categoriasMap.values()).sort((a, b) =>
      a.nombre.localeCompare(b.nombre, 'es', { sensitivity: 'base' })
    );
  }

  private crearEstadoInicialFormulario(): ProductoCrearFormState {
    return {
      nombre: '',
      precio: null,
      tipoGestion: 'INVENTARIABLE',
      tipoImpuesto: 'IVA',
      impuestoPorcentaje: 19,
      stockInicial: 0,
      stockMinimo: 0,
      categoriaId: null,
    };
  }

  private crearEstadoInicialEdicion(): ProductoEditarFormState {
    return {
      nombre: '',
      precio: null,
      tipoGestion: 'INVENTARIABLE',
      tipoImpuesto: 'IVA',
      impuestoPorcentaje: 19,
      stockMinimo: 0,
      categoriaId: null,
    };
  }

  private restablecerFormularioCreacion(form?: NgForm): void {
    this.nuevoProducto = this.crearEstadoInicialFormulario();
    this.sincronizarCategoriaSeleccionadaEnFormulario();
    this.selectorCategoriaCrear = this.nuevoProducto.categoriaId;
    this.nuevaCategoriaNombre = '';
    this.mostrarCrearCategoria = false;
    this.errorCrearCategoria = null;
    this.errorCrearProducto = null;

    if (form) {
      form.resetForm(this.nuevoProducto);
    }
    this.onTipoGestionCrearChange();
  }

  private mostrarToastExitoCreacion(nombreProducto: string): void {
    this.mensajeExitoCreacion = `Producto "${nombreProducto}" creado correctamente.`;
    this.toastExitoVisible = true;

    if (this.toastTimeoutId !== null) {
      clearTimeout(this.toastTimeoutId);
    }

    this.toastTimeoutId = setTimeout(() => {
      this.toastExitoVisible = false;
      this.toastTimeoutId = null;
      this.cdr.detectChanges();
    }, 3500);
  }

  private validarCreacion(form: NgForm): string | null {
    if (form.invalid) {
      return 'Completa los campos obligatorios del formulario.';
    }

    if ((this.nuevoProducto.nombre || '').trim().length < 2) {
      return 'El nombre del producto debe tener mínimo 2 caracteres.';
    }

    if (this.nuevoProducto.precio === null || this.nuevoProducto.precio < 0) {
      return 'El precio debe ser un valor válido.';
    }

    if (this.nuevoProducto.categoriaId === null) {
      return 'Selecciona una categoría para continuar.';
    }

    if (this.nuevoProducto.tipoGestion === 'INVENTARIABLE') {
      if (
        !this.esCombinacionImpuestoValida(
          this.nuevoProducto.tipoImpuesto,
          this.nuevoProducto.impuestoPorcentaje
        )
      ) {
        return 'Selecciona un impuesto válido: IVA (0%, 5%, 19%) o INC (8%).';
      }

      if (this.nuevoProducto.stockInicial === null || this.nuevoProducto.stockInicial < 0) {
        return 'El stock inicial debe ser un valor mayor o igual a 0.';
      }

      if (this.nuevoProducto.stockMinimo === null || this.nuevoProducto.stockMinimo < 0) {
        return 'El stock mínimo debe ser un valor mayor o igual a 0.';
      }
    }

    return null;
  }

  private validarEdicion(form: NgForm): string | null {
    if (form.invalid) {
      return 'Completa los campos obligatorios para actualizar el producto.';
    }

    if ((this.edicionProducto.nombre || '').trim().length < 2) {
      return 'El nombre del producto debe tener mínimo 2 caracteres.';
    }

    if (this.edicionProducto.precio === null || this.edicionProducto.precio < 0) {
      return 'El precio debe ser un valor válido.';
    }

    if (this.edicionProducto.categoriaId === null) {
      return 'Selecciona una categoría para el producto.';
    }

    if (this.edicionProducto.tipoGestion === 'INVENTARIABLE') {
      if (
        !this.esCombinacionImpuestoValida(
          this.edicionProducto.tipoImpuesto,
          this.edicionProducto.impuestoPorcentaje
        )
      ) {
        return 'Selecciona un impuesto válido: IVA (0%, 5%, 19%) o INC (8%).';
      }

      if (this.edicionProducto.stockMinimo === null || this.edicionProducto.stockMinimo < 0) {
        return 'El stock mínimo debe ser mayor o igual a 0.';
      }
    }

    return null;
  }

  private resolverTipoImpuestoProducto(producto: Producto): TipoImpuesto {
    if (producto.tipoImpuesto === 'IVA' || producto.tipoImpuesto === 'INC') {
      return producto.tipoImpuesto;
    }
    return producto.tipoGestion === 'PREPARACION' ? 'INC' : 'IVA';
  }

  private resolverImpuestoPorcentajeProducto(producto: Producto): number | null {
    if (typeof producto.impuestoPorcentaje === 'number') {
      return producto.impuestoPorcentaje;
    }
    return typeof producto.ivaPorcentaje === 'number' ? producto.ivaPorcentaje : null;
  }

  private normalizarPorcentajeSegunTipo(
    tipoImpuesto: TipoImpuesto,
    impuestoPorcentaje: number | null
  ): number {
    if (tipoImpuesto === 'INC') {
      return ProductosComponent.INC_PORCENTAJE;
    }
    return this.esIvaPermitido(impuestoPorcentaje) ? Number(impuestoPorcentaje) : 19;
  }

  private esCombinacionImpuestoValida(
    tipoImpuesto: TipoImpuesto | null,
    impuestoPorcentaje: number | null
  ): boolean {
    if (tipoImpuesto === 'INC') {
      return impuestoPorcentaje === ProductosComponent.INC_PORCENTAJE;
    }
    if (tipoImpuesto === 'IVA') {
      return this.esIvaPermitido(impuestoPorcentaje);
    }
    return false;
  }

  private esIvaPermitido(iva: number | null): boolean {
    return iva !== null && ProductosComponent.IVA_PERMITIDOS.includes(iva as 0 | 5 | 19);
  }

  private mostrarError(titulo: string, mensaje: string, error?: unknown): void {
    const mensajeModal = error !== undefined ? obtenerMensajeError(error, mensaje) : mensaje;

    if (error !== undefined) {
      console.error(titulo, error);
    }

    void this.modalMensajeService.error({
      titulo,
      mensaje: mensajeModal,
      textoConfirmar: 'Entendido',
    });
  }
}
