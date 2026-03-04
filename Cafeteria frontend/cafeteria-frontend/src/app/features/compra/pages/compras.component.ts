import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, finalize, forkJoin, of } from 'rxjs';
import { Producto } from '../../../core/models/producto.model';
import { Categoria } from '../../productos/models/categoria.model';
import { CrearProducto } from '../../productos/models/crearProducto.model';
import { CategoriaService } from '../../productos/services/categoria.service';
import { ProductoService } from '../../productos/services/producto.service';
import { CrearProveedorRequest } from '../../proveedor/models/crearProveedor.model';
import { Proveedor } from '../../proveedor/models/proveedor.model';
import { TipoDocumento } from '../../proveedor/models/tipoDocumento.model';
import { ProveedorService } from '../../proveedor/services/proveedor.service';
import { CompraResponse } from '../models/compraResponse.model';
import { CompraResumenResponse } from '../models/compraResumenResponse.model';
import { CompraService } from '../services/compra.service';
import { ModalMensajeService } from '../../../shared/services/modal-mensaje.service';
import { obtenerMensajeError } from '../../../shared/utils/error-message.util';

type TabCompras = 'listado' | 'nueva';
type EstadoCompraFiltro = 'TODAS' | 'BORRADOR' | 'CONFIRMADA' | 'ANULADA';
type CampoOrdenCompra = 'fechaCreacion' | 'total' | 'estado' | 'numeroFactura';
type DireccionOrden = 'asc' | 'desc';
type AccionCompraBorrador = 'CONFIRMAR' | 'ANULAR';
const OPCION_AGREGAR_PRODUCTO = '__agregar_producto__' as const;
type OpcionAgregarProducto = typeof OPCION_AGREGAR_PRODUCTO;
const OPCION_AGREGAR_PROVEEDOR = '__agregar_proveedor__' as const;
type OpcionAgregarProveedor = typeof OPCION_AGREGAR_PROVEEDOR;

interface DetalleCompraFormState {
  productoId: number | OpcionAgregarProducto | null;
  cantidad: number | null;
  costoUnitario: number | null;
}

interface NuevaCompraFormState {
  proveedorId: number | null;
  numeroFactura: string;
  observacion: string;
  detalles: DetalleCompraFormState[];
}

interface NuevoProductoRapidoFormState {
  nombre: string;
  precio: number | null;
  categoriaId: number | null;
  ivaPorcentaje: number | null;
  stockInicial: number | null;
  stockMinimo: number | null;
}

interface NuevoProveedorRapidoFormState {
  id: number | null;
  nombre: string;
  tipoDocumento: TipoDocumento;
  telefono: string;
  correo: string;
}

@Component({
  selector: 'app-compras',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './compras.component.html',
})
export class ComprasComponent implements OnInit {
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly compraService = inject(CompraService);
  private readonly proveedorService = inject(ProveedorService);
  private readonly productoService = inject(ProductoService);
  private readonly categoriaService = inject(CategoriaService);
  private readonly modalMensajeService = inject(ModalMensajeService);

  tabActual: TabCompras = 'listado';

  loadingCompras: boolean = false;
  errorListado: string | null = null;
  errorFiltros: string | null = null;

  comprasPagina: CompraResumenResponse[] = [];
  comprasFiltradas: CompraResumenResponse[] = [];

  paginaActual: number = 0;
  tamanoPagina: number = 10;
  totalPaginas: number = 0;
  totalElementos: number = 0;

  busqueda: string = '';
  estadoFiltro: EstadoCompraFiltro = 'TODAS';
  desde: string = '';
  hasta: string = '';
  campoOrden: CampoOrdenCompra = 'fechaCreacion';
  direccionOrden: DireccionOrden = 'desc';

  cargandoFormulario: boolean = false;
  guardandoCompra: boolean = false;
  errorCrearCompra: string | null = null;
  exitoCrearCompra: string | null = null;
  modalCrearCompraAbierto: boolean = false;
  modalResumenCompraAbierto: boolean = false;
  cargandoResumenCompra: boolean = false;
  errorResumenCompra: string | null = null;
  compraResumenSolicitadaId: number | null = null;
  compraResumenSeleccionada: CompraResponse | null = null;
  procesandoAccionCompraId: number | null = null;
  errorAccionCompra: string | null = null;
  exitoAccionCompra: string | null = null;
  modalConfirmacionAccionAbierto: boolean = false;
  accionCompraPendiente: AccionCompraBorrador | null = null;
  compraAccionPendiente: CompraResumenResponse | null = null;
  modalCrearProductoRapidoAbierto: boolean = false;
  cargandoCategoriasProductoRapido: boolean = false;
  guardandoProductoRapido: boolean = false;
  errorCrearProductoRapido: string | null = null;
  indiceDetalleProductoRapido: number | null = null;
  categoriasProductoRapido: Categoria[] = [];
  modalCrearProveedorRapidoAbierto: boolean = false;
  guardandoProveedorRapido: boolean = false;
  errorCrearProveedorRapido: string | null = null;

  proveedores: Proveedor[] = [];
  productosDisponibles: Producto[] = [];
  readonly opcionAgregarProducto = OPCION_AGREGAR_PRODUCTO;
  readonly opcionAgregarProveedor = OPCION_AGREGAR_PROVEEDOR;
  readonly tiposDocumentoProveedorRapido: TipoDocumento[] = ['NIT', 'CC', 'CE', 'PAS'];

  nuevaCompra: NuevaCompraFormState = this.crearFormularioInicial();
  nuevoProductoRapido: NuevoProductoRapidoFormState = this.crearFormularioProductoRapidoInicial();
  nuevoProveedorRapido: NuevoProveedorRapidoFormState = this.crearFormularioProveedorRapidoInicial();

  ngOnInit(): void {
    this.tabActual = this.obtenerTabDesdeRuta();
    if (this.tabActual === 'listado') {
      this.cargarCompras();
      return;
    }

    this.cargarDatosFormulario();
  }

  get formularioBloqueado(): boolean {
    return false;
  }

  get totalDetallesCompra(): number {
    return this.nuevaCompra.detalles.reduce((acumulado, detalle) => {
      return acumulado + this.calcularSubtotalDetalle(detalle);
    }, 0);
  }

  get paginasVisibles(): number[] {
    if (this.totalPaginas <= 0) {
      return [];
    }

    const paginas: number[] = [];
    const maxPaginasVisibles = 5;
    let inicio = Math.max(0, this.paginaActual - Math.floor(maxPaginasVisibles / 2));
    let fin = Math.min(this.totalPaginas - 1, inicio + maxPaginasVisibles - 1);

    if (fin - inicio < maxPaginasVisibles - 1) {
      inicio = Math.max(0, fin - maxPaginasVisibles + 1);
    }

    for (let i = inicio; i <= fin; i += 1) {
      paginas.push(i);
    }

    return paginas;
  }

  cargarCompras(): void {
    this.loadingCompras = true;
    this.errorListado = null;

    this.compraService
      .obtenerCompras({
        page: this.paginaActual,
        size: this.tamanoPagina,
        ordenarPor: this.campoOrden,
        direccion: this.direccionOrden,
        estado: this.estadoFiltro === 'TODAS' ? undefined : this.estadoFiltro,
        desde: this.desde || undefined,
        hasta: this.hasta || undefined,
      })
      .pipe(
        finalize(() => {
          this.loadingCompras = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (response) => {
          this.comprasPagina = response.content;
          this.totalElementos = response.totalElements;
          this.totalPaginas = response.totalPages;
          this.paginaActual = response.number;
          this.aplicarFiltrosLocales();
          this.cdr.detectChanges();
        },
        error: (error) => {
          this.mostrarError(
            'No se pudo cargar el listado de compras',
            'Verifica tu conexión e intenta de nuevo.',
            error
          );
          this.comprasPagina = [];
          this.comprasFiltradas = [];
          this.totalPaginas = 0;
          this.totalElementos = 0;
          this.errorListado =
            'No se pudo cargar el listado de compras. Verifica tu conexión e intenta de nuevo.';
          this.cdr.detectChanges();
        },
      });
  }

  aplicarFiltrosLocales(): void {
    const termino = this.busqueda.trim().toLowerCase();
    let resultado = [...this.comprasPagina];

    if (termino) {
      resultado = resultado.filter((compra) => {
        return (
          compra.proveedorNombre.toLowerCase().includes(termino) ||
          compra.numeroFactura.toLowerCase().includes(termino) ||
          compra.compraId.toString().includes(termino)
        );
      });
    }

    this.comprasFiltradas = resultado;
  }

  aplicarFiltrosRemotos(): void {
    if (this.desde && this.hasta && this.desde > this.hasta) {
      this.errorFiltros = 'La fecha inicial no puede ser mayor que la fecha final.';
      return;
    }

    this.errorFiltros = null;
    this.paginaActual = 0;
    this.cargarCompras();
  }

  limpiarFiltros(): void {
    this.busqueda = '';
    this.estadoFiltro = 'TODAS';
    this.desde = '';
    this.hasta = '';
    this.campoOrden = 'fechaCreacion';
    this.direccionOrden = 'desc';
    this.errorFiltros = null;
    this.paginaActual = 0;
    this.cargarCompras();
  }

  onBusquedaChange(value: string): void {
    this.busqueda = value;
    this.aplicarFiltrosLocales();
  }

  onEstadoFiltroChange(value: EstadoCompraFiltro): void {
    this.estadoFiltro = value;
    this.paginaActual = 0;
    this.cargarCompras();
  }

  onOrdenChange(): void {
    this.paginaActual = 0;
    this.cargarCompras();
  }

  onTamanoPaginaChange(value: number | string): void {
    const nuevoTamano = Number(value);
    if (!Number.isFinite(nuevoTamano) || nuevoTamano <= 0) {
      return;
    }

    this.tamanoPagina = nuevoTamano;
    this.paginaActual = 0;
    this.cargarCompras();
  }

  irAPagina(pagina: number): void {
    if (pagina < 0 || pagina >= this.totalPaginas || pagina === this.paginaActual) {
      return;
    }

    this.paginaActual = pagina;
    this.cargarCompras();
  }

  paginaAnterior(): void {
    if (this.paginaActual <= 0) {
      return;
    }

    this.irAPagina(this.paginaActual - 1);
  }

  paginaSiguiente(): void {
    if (this.paginaActual >= this.totalPaginas - 1) {
      return;
    }

    this.irAPagina(this.paginaActual + 1);
  }

  onProveedorCompraChange(value: number | OpcionAgregarProveedor | null): void {
    if (value === OPCION_AGREGAR_PROVEEDOR) {
      this.abrirModalCrearProveedorRapido();
      return;
    }

    this.nuevaCompra.proveedorId = value === null ? null : Number(value);
  }

  agregarDetalle(): void {
    this.nuevaCompra.detalles.push({
      productoId: null,
      cantidad: null,
      costoUnitario: null,
    });
  }

  eliminarDetalle(index: number): void {
    if (this.nuevaCompra.detalles.length === 1) {
      this.nuevaCompra.detalles[0] = {
        productoId: null,
        cantidad: null,
        costoUnitario: null,
      };
      return;
    }

    this.nuevaCompra.detalles.splice(index, 1);
  }

  onProductoDetalleChange(
    index: number,
    productoId: number | OpcionAgregarProducto | null
  ): void {
    const detalle = this.nuevaCompra.detalles[index];
    if (!detalle) {
      return;
    }

    if (productoId === OPCION_AGREGAR_PRODUCTO) {
      detalle.productoId = null;
      this.abrirModalCrearProductoRapido(index);
      return;
    }

    if (productoId === null) {
      return;
    }

    const productoSeleccionado = this.productosDisponibles.find((producto) => producto.codigo === productoId);
    if (!productoSeleccionado) {
      return;
    }

    if (detalle.costoUnitario === null || detalle.costoUnitario <= 0) {
      detalle.costoUnitario = productoSeleccionado.precio;
    }
  }

  calcularSubtotalDetalle(detalle: DetalleCompraFormState): number {
    const cantidad = Number(detalle.cantidad ?? 0);
    const costoUnitario = Number(detalle.costoUnitario ?? 0);
    return cantidad * costoUnitario;
  }

  crearCompra(form: NgForm): void {
    this.errorCrearCompra = null;
    this.exitoCrearCompra = null;

    if (form.invalid) {
      this.errorCrearCompra = 'Completa los campos obligatorios para registrar la compra.';
      return;
    }

    if (this.nuevaCompra.proveedorId === null) {
      this.errorCrearCompra = 'Selecciona un proveedor para continuar.';
      return;
    }

    const detalles = this.nuevaCompra.detalles
      .filter((detalle) => {
        return (
          typeof detalle.productoId === 'number' &&
          detalle.cantidad !== null &&
          detalle.costoUnitario !== null
        );
      })
      .map((detalle) => ({
        productoId: detalle.productoId as number,
        cantidad: Number(detalle.cantidad),
        costoUnitario: Number(detalle.costoUnitario),
      }))
      .filter((detalle) => detalle.cantidad > 0 && detalle.costoUnitario > 0);

    if (!detalles.length) {
      this.errorCrearCompra =
        'Agrega al menos un producto con cantidad y costo unitario mayores a cero.';
      return;
    }

    this.guardandoCompra = true;
    this.compraService
      .crearCompra({
        proveedorId: Number(this.nuevaCompra.proveedorId),
        numeroFactura: this.nuevaCompra.numeroFactura.trim(),
        observacion: this.nuevaCompra.observacion.trim(),
        detalles,
      })
      .pipe(
        finalize(() => {
          this.guardandoCompra = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (compra) => {
          this.exitoCrearCompra = `Compra #${compra.compraId} registrada correctamente.`;
          this.nuevaCompra = this.crearFormularioInicial();
          form.resetForm(this.nuevaCompra);
          this.cdr.detectChanges();

          if (this.modalCrearCompraAbierto) {
            setTimeout(() => {
              this.cerrarModalCrearCompra();
              this.cargarCompras();
            }, 600);
            return;
          }

          setTimeout(() => {
            this.router.navigate(['/compras/listado']);
          }, 800);
        },
        error: (error) => {
          this.mostrarError(
            'No se pudo registrar la compra',
            'Verifica los datos e intenta de nuevo.',
            error
          );
          this.errorCrearCompra = 'No se pudo registrar la compra. Verifica los datos e intenta de nuevo.';
          this.cdr.detectChanges();
        },
      });
  }

  abrirModalCrearCompra(): void {
    this.modalCrearCompraAbierto = true;
    this.errorCrearCompra = null;
    this.exitoCrearCompra = null;
    this.nuevaCompra = this.crearFormularioInicial();
    this.refrescarVista();

    this.cargarDatosFormulario();
  }

  cerrarModalCrearCompra(): void {
    this.modalCrearCompraAbierto = false;
    this.cerrarModalCrearProductoRapido(false);
    this.cerrarModalCrearProveedorRapido(false);
    this.errorCrearCompra = null;
    this.exitoCrearCompra = null;
    this.nuevaCompra = this.crearFormularioInicial();
    this.cdr.detectChanges();
  }

  abrirModalCrearProveedorRapido(): void {
    this.errorCrearProveedorRapido = null;
    this.nuevoProveedorRapido = this.crearFormularioProveedorRapidoInicial();
    this.modalCrearProveedorRapidoAbierto = true;
    this.refrescarVista();
  }

  cerrarModalCrearProveedorRapido(_restablecerProveedorSeleccionado: boolean = true): void {
    this.modalCrearProveedorRapidoAbierto = false;
    this.guardandoProveedorRapido = false;
    this.errorCrearProveedorRapido = null;
    this.nuevoProveedorRapido = this.crearFormularioProveedorRapidoInicial();

    this.refrescarVista();
  }

  crearProveedorRapido(form: NgForm): void {
    this.errorCrearProveedorRapido = null;

    if (form.invalid) {
      this.errorCrearProveedorRapido = 'Completa los datos obligatorios para crear el proveedor.';
      return;
    }

    const id = this.nuevoProveedorRapido.id;
    if (id === null || !Number.isFinite(id) || id <= 0) {
      this.errorCrearProveedorRapido = 'El ID del proveedor debe ser un numero mayor a 0.';
      return;
    }

    const nombre = this.nuevoProveedorRapido.nombre.trim();
    if (nombre.length < 2) {
      this.errorCrearProveedorRapido = 'El nombre del proveedor debe tener al menos 2 caracteres.';
      return;
    }

    const payload: CrearProveedorRequest = {
      id,
      nombre,
      tipoDocumento: this.nuevoProveedorRapido.tipoDocumento,
      telefono: this.nuevoProveedorRapido.telefono.trim() || null,
      correo: this.nuevoProveedorRapido.correo.trim() || null,
    };

    this.guardandoProveedorRapido = true;

    this.proveedorService
      .crearProveedor(payload)
      .pipe(
        finalize(() => {
          this.guardandoProveedorRapido = false;
          this.refrescarVista();
        })
      )
      .subscribe({
        next: (proveedorCreado) => {
          this.proveedores = [...this.proveedores, proveedorCreado]
            .filter((proveedor, index, array) => {
              return array.findIndex((item) => item.id === proveedor.id) === index;
            })
            .sort((a, b) => a.nombre.localeCompare(b.nombre, 'es', { sensitivity: 'base' }));

          this.nuevaCompra.proveedorId = proveedorCreado.id;
          this.cerrarModalCrearProveedorRapido(false);
          this.refrescarVista();
        },
        error: (error) => {
          this.mostrarError(
            'No se pudo crear el proveedor',
            'Verifica los datos e intenta nuevamente.',
            error
          );
          this.errorCrearProveedorRapido =
            'No se pudo crear el proveedor. Verifica los datos e intenta nuevamente.';
          this.refrescarVista();
        },
      });
  }

  abrirModalCrearProductoRapido(indexDetalle: number): void {
    this.indiceDetalleProductoRapido = indexDetalle;
    this.errorCrearProductoRapido = null;
    this.nuevoProductoRapido = this.crearFormularioProductoRapidoInicial();
    this.modalCrearProductoRapidoAbierto = true;

    if (this.categoriasProductoRapido.length === 0 && !this.cargandoCategoriasProductoRapido) {
      this.cargarCategoriasProductoRapido();
    }

    this.refrescarVista();
  }

  cerrarModalCrearProductoRapido(limpiarIndice: boolean = true): void {
    this.modalCrearProductoRapidoAbierto = false;
    this.errorCrearProductoRapido = null;
    this.guardandoProductoRapido = false;
    this.nuevoProductoRapido = this.crearFormularioProductoRapidoInicial();

    if (limpiarIndice) {
      this.indiceDetalleProductoRapido = null;
    }

    this.refrescarVista();
  }

  onCategoriaProductoRapidoChange(value: number | null): void {
    this.nuevoProductoRapido.categoriaId = value;
  }

  crearProductoRapido(form: NgForm): void {
    this.errorCrearProductoRapido = null;

    if (form.invalid) {
      this.errorCrearProductoRapido = 'Completa los datos obligatorios para crear el producto.';
      return;
    }

    const nombre = this.nuevoProductoRapido.nombre.trim();
    if (nombre.length < 2) {
      this.errorCrearProductoRapido = 'El nombre del producto debe tener mínimo 2 caracteres.';
      return;
    }

    if (this.nuevoProductoRapido.precio === null || this.nuevoProductoRapido.precio < 0) {
      this.errorCrearProductoRapido = 'El precio debe ser mayor o igual a 0.';
      return;
    }

    if (this.nuevoProductoRapido.categoriaId === null) {
      this.errorCrearProductoRapido = 'Selecciona una categoría para continuar.';
      return;
    }

    if (
      !this.esIvaPermitido(this.nuevoProductoRapido.ivaPorcentaje) ||
      this.nuevoProductoRapido.stockInicial === null ||
      this.nuevoProductoRapido.stockInicial < 0 ||
      this.nuevoProductoRapido.stockMinimo === null ||
      this.nuevoProductoRapido.stockMinimo < 0
    ) {
      this.errorCrearProductoRapido =
        'Selecciona un IVA válido (0%, 5% o 19%) y verifica stock inicial/mínimo.';
      return;
    }

    const payload: CrearProducto = {
      nombre,
      precio: Number(this.nuevoProductoRapido.precio),
      tipoGestion: 'INVENTARIABLE',
      ivaPorcentaje: Number(this.nuevoProductoRapido.ivaPorcentaje),
      stockInicial: Number(this.nuevoProductoRapido.stockInicial),
      stockMinimo: Number(this.nuevoProductoRapido.stockMinimo),
      categoriaId: Number(this.nuevoProductoRapido.categoriaId),
    };

    this.guardandoProductoRapido = true;

    this.productoService
      .crearProducto(payload)
      .pipe(
        finalize(() => {
          this.guardandoProductoRapido = false;
          this.refrescarVista();
        })
      )
      .subscribe({
        next: (productoCreado) => {
          if (productoCreado.activo) {
            this.productosDisponibles = [...this.productosDisponibles, productoCreado]
              .filter((producto, index, array) => {
                return array.findIndex((item) => item.codigo === producto.codigo) === index;
              })
              .sort((a, b) => a.nombre.localeCompare(b.nombre, 'es', { sensitivity: 'base' }));
          }

          const indiceDetalle = this.indiceDetalleProductoRapido;
          if (indiceDetalle !== null) {
            const detalle = this.nuevaCompra.detalles[indiceDetalle];
            if (detalle) {
              detalle.productoId = productoCreado.codigo;
              if (detalle.costoUnitario === null || detalle.costoUnitario <= 0) {
                detalle.costoUnitario = productoCreado.precio;
              }
            }
          }

          this.cerrarModalCrearProductoRapido();
          this.refrescarVista();
        },
        error: (error) => {
          this.mostrarError(
            'No se pudo crear el producto',
            'Verifica los datos e intenta nuevamente.',
            error
          );
          this.errorCrearProductoRapido =
            'No se pudo crear el producto. Verifica los datos e intenta nuevamente.';
          this.refrescarVista();
        },
      });
  }

  abrirResumenCompra(compra: CompraResumenResponse): void {
    this.modalResumenCompraAbierto = true;
    this.cargandoResumenCompra = true;
    this.errorResumenCompra = null;
    this.compraResumenSeleccionada = null;
    this.compraResumenSolicitadaId = compra.compraId;
    this.refrescarVista();

    this.compraService
      .obtenerCompra(compra.compraId)
      .subscribe({
        next: (resumen) => {
          this.compraResumenSeleccionada = {
            ...resumen,
            detalles: resumen.detalles ?? [],
          };
          this.cargandoResumenCompra = false;
          this.errorResumenCompra = null;
          this.refrescarVista();
        },
        error: (error) => {
          this.mostrarError(
            'No se pudo cargar el resumen de la compra',
            'Intenta nuevamente.',
            error
          );
          this.cargandoResumenCompra = false;
          this.compraResumenSeleccionada = null;
          this.errorResumenCompra =
            'No se pudo obtener el resumen de la compra. Intenta nuevamente.';
          this.refrescarVista();
        },
      });
  }

  confirmarCompraBorrador(compra: CompraResumenResponse): void {
    if (compra.estado !== 'BORRADOR' || this.procesandoAccionCompraId !== null) {
      return;
    }

    this.procesandoAccionCompraId = compra.compraId;
    this.errorAccionCompra = null;
    this.exitoAccionCompra = null;
    this.refrescarVista();

    this.compraService
      .confirmarCompra(compra.compraId)
      .pipe(
        finalize(() => {
          this.procesandoAccionCompraId = null;
          this.refrescarVista();
        })
      )
      .subscribe({
        next: () => {
          this.exitoAccionCompra = `Compra #${compra.compraId} confirmada correctamente.`;
          this.cargarCompras();
          this.refrescarVista();
        },
        error: (error) => {
          this.mostrarError('No se pudo confirmar la compra', 'Intenta nuevamente.', error);
          this.errorAccionCompra = 'No se pudo confirmar la compra. Intenta nuevamente.';
          this.refrescarVista();
        },
      });
  }

  anularCompraBorrador(compra: CompraResumenResponse): void {
    if (compra.estado !== 'BORRADOR' || this.procesandoAccionCompraId !== null) {
      return;
    }

    this.procesandoAccionCompraId = compra.compraId;
    this.errorAccionCompra = null;
    this.exitoAccionCompra = null;
    this.refrescarVista();

    this.compraService
      .anularCompra(compra.compraId)
      .pipe(
        finalize(() => {
          this.procesandoAccionCompraId = null;
          this.refrescarVista();
        })
      )
      .subscribe({
        next: () => {
          this.exitoAccionCompra = `Compra #${compra.compraId} anulada correctamente.`;
          this.cargarCompras();
          this.refrescarVista();
        },
        error: (error) => {
          this.mostrarError('No se pudo anular la compra', 'Intenta nuevamente.', error);
          this.errorAccionCompra = 'No se pudo anular la compra. Intenta nuevamente.';
          this.refrescarVista();
        },
      });
  }

  abrirModalConfirmacionAccion(
    compra: CompraResumenResponse,
    accion: AccionCompraBorrador
  ): void {
    if (compra.estado !== 'BORRADOR' || this.procesandoAccionCompraId !== null) {
      return;
    }

    this.compraAccionPendiente = compra;
    this.accionCompraPendiente = accion;
    this.modalConfirmacionAccionAbierto = true;
    this.refrescarVista();
  }

  cerrarModalConfirmacionAccion(): void {
    this.modalConfirmacionAccionAbierto = false;
    this.compraAccionPendiente = null;
    this.accionCompraPendiente = null;
    this.refrescarVista();
  }

  confirmarAccionCompraPendiente(): void {
    if (!this.compraAccionPendiente || !this.accionCompraPendiente) {
      return;
    }

    const compra = this.compraAccionPendiente;
    const accion = this.accionCompraPendiente;

    this.cerrarModalConfirmacionAccion();

    if (accion === 'CONFIRMAR') {
      this.confirmarCompraBorrador(compra);
      return;
    }

    this.anularCompraBorrador(compra);
  }

  cerrarResumenCompra(): void {
    this.modalResumenCompraAbierto = false;
    this.cargandoResumenCompra = false;
    this.errorResumenCompra = null;
    this.compraResumenSolicitadaId = null;
    this.compraResumenSeleccionada = null;
    this.refrescarVista();
  }

  formatearMoneda(valor: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
    }).format(valor);
  }

  formatearFechaHora(fechaIso: string | null): string {
    if (!fechaIso) {
      return '-';
    }

    const fecha = new Date(fechaIso);
    if (Number.isNaN(fecha.getTime())) {
      return '-';
    }

    return fecha.toLocaleString('es-CO', {
      dateStyle: 'medium',
      timeStyle: 'short',
    });
  }

  textoEstado(estado: CompraResumenResponse['estado']): string {
    if (estado === 'BORRADOR') {
      return 'Borrador';
    }

    if (estado === 'CONFIRMADA') {
      return 'Confirmada';
    }

    return 'Anulada';
  }

  claseEstado(estado: CompraResumenResponse['estado']): string {
    if (estado === 'BORRADOR') {
      return 'bg-amber-100 text-amber-700';
    }

    if (estado === 'CONFIRMADA') {
      return 'bg-emerald-100 text-emerald-700';
    }

    return 'bg-red-100 text-red-700';
  }

  private cargarDatosFormulario(): void {
    this.cargandoFormulario = true;
    this.errorCrearCompra = null;

    forkJoin({
      proveedores: this.proveedorService.obtenerProveedores().pipe(
        catchError((error) => {
          this.mostrarError(
            'No se pudieron cargar los proveedores',
            'Intenta nuevamente en unos segundos.',
            error
          );
          return of([] as Proveedor[]);
        })
      ),
      categorias: this.categoriaService.obtenerCategorias().pipe(
        catchError((error) => {
          this.mostrarError(
            'No se pudieron cargar las categorías',
            'Intenta nuevamente en unos segundos.',
            error
          );
          return of([] as Categoria[]);
        })
      ),
      productos: this.productoService.obtenerProductosInventariables().pipe(
        catchError((error) => {
          this.mostrarError(
            'No se pudieron cargar los productos inventariables',
            'Intentaremos cargar el catálogo general.',
            error
          );
          return this.productoService.obtenerProductos().pipe(
            catchError((fallbackError) => {
              this.mostrarError(
                'No se pudo cargar el catálogo de productos',
                'Intenta nuevamente en unos segundos.',
                fallbackError
              );
              return of([] as Producto[]);
            })
          );
        })
      ),
    })
      .pipe(
        finalize(() => {
          this.cargandoFormulario = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe(({ proveedores, categorias, productos }) => {
        this.proveedores = proveedores;
        this.categoriasProductoRapido = [...categorias].sort((a, b) =>
          a.nombre.localeCompare(b.nombre, 'es', { sensitivity: 'base' })
        );
        this.productosDisponibles = productos.filter((producto) => producto.activo);

        if (!this.nuevoProductoRapido.categoriaId && this.categoriasProductoRapido.length > 0) {
          this.nuevoProductoRapido.categoriaId = this.categoriasProductoRapido[0].id;
        }

        this.cdr.detectChanges();
      });
  }

  private cargarCategoriasProductoRapido(): void {
    this.cargandoCategoriasProductoRapido = true;

    this.categoriaService
      .obtenerCategorias()
      .pipe(
        finalize(() => {
          this.cargandoCategoriasProductoRapido = false;
          this.refrescarVista();
        })
      )
      .subscribe({
        next: (categorias) => {
          this.categoriasProductoRapido = [...categorias].sort((a, b) =>
            a.nombre.localeCompare(b.nombre, 'es', { sensitivity: 'base' })
          );

          if (!this.nuevoProductoRapido.categoriaId && this.categoriasProductoRapido.length > 0) {
            this.nuevoProductoRapido.categoriaId = this.categoriasProductoRapido[0].id;
          }

          this.refrescarVista();
        },
        error: (error) => {
          this.mostrarError(
            'No se pudieron cargar categorías para crear producto',
            'Intenta nuevamente.',
            error
          );
          this.errorCrearProductoRapido = 'No se pudieron cargar categorías. Intenta nuevamente.';
          this.refrescarVista();
        },
      });
  }

  private obtenerTabDesdeRuta(): TabCompras {
    const tab = this.route.snapshot.data['comprasTab'];
    if (tab === 'nueva') {
      return 'nueva';
    }
    return 'listado';
  }

  private crearFormularioInicial(): NuevaCompraFormState {
    return {
      proveedorId: null,
      numeroFactura: '',
      observacion: '',
      detalles: [
        {
          productoId: null,
          cantidad: null,
          costoUnitario: null,
        },
      ],
    };
  }

  private crearFormularioProductoRapidoInicial(): NuevoProductoRapidoFormState {
    return {
      nombre: '',
      precio: null,
      categoriaId: this.categoriasProductoRapido[0]?.id ?? null,
      ivaPorcentaje: 19,
      stockInicial: 0,
      stockMinimo: 0,
    };
  }

  private crearFormularioProveedorRapidoInicial(): NuevoProveedorRapidoFormState {
    return {
      id: null,
      nombre: '',
      tipoDocumento: 'NIT',
      telefono: '',
      correo: '',
    };
  }

  private refrescarVista(): void {
    requestAnimationFrame(() => {
      this.cdr.detectChanges();
    });
  }

  private esIvaPermitido(iva: number | null): boolean {
    return iva === 0 || iva === 5 || iva === 19;
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
