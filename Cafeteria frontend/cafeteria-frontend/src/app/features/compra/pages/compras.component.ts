import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, HostListener, OnInit, inject } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { catchError, finalize, forkJoin, of } from 'rxjs';
import { Producto } from '../../../core/models/producto.model';
import { ModalMensajeService } from '../../../shared/services/modal-mensaje.service';
import { obtenerMensajeError } from '../../../shared/utils/error-message.util';
import { Categoria } from '../../productos/models/categoria.model';
import { CrearProducto } from '../../productos/models/crearProducto.model';
import { CategoriaService } from '../../productos/services/categoria.service';
import { ProductoService } from '../../productos/services/producto.service';
import { CrearProveedorRequest } from '../../proveedor/models/crearProveedor.model';
import { Proveedor } from '../../proveedor/models/proveedor.model';
import { TipoDocumento } from '../../proveedor/models/tipoDocumento.model';
import { ProveedorService } from '../../proveedor/services/proveedor.service';
import { DetalleCompraRequest } from '../models/compraDetalle.model';
import { CompraResponse } from '../models/compraResponse.model';
import { CompraResumenResponse } from '../models/compraResumenResponse.model';
import { CompraService } from '../services/compra.service';

type TabCompras = 'listado' | 'nueva';
type EstadoCompraFiltro = 'TODAS' | 'BORRADOR' | 'CONFIRMADA' | 'ANULADA';
type CampoOrdenCompra = 'fechaCreacion' | 'total' | 'estado' | 'numeroFactura';
type DireccionOrden = 'asc' | 'desc';
type TipoImpuestoProducto = 'IVA' | 'INC';
type AccionCompraBorrador = 'CONFIRMAR' | 'ANULAR';
const OPCION_AGREGAR_PRODUCTO = '__agregar_producto__' as const;
type OpcionAgregarProducto = typeof OPCION_AGREGAR_PRODUCTO;
const OPCION_AGREGAR_PROVEEDOR = '__agregar_proveedor__' as const;
type OpcionAgregarProveedor = typeof OPCION_AGREGAR_PROVEEDOR;

interface DetalleCompraFormState {
  productoId: number | string | null;
  productoBusqueda: string;
  productoBusquedaAbierta: boolean;
  productoBusquedaIndiceActiva: number;
  cantidad: number | null;
  subtotal: number | null;
}

interface PosicionBusquedaProductoOverlay {
  top: number;
  left: number;
  width: number;
  maxHeight: number;
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
  tipoImpuesto: TipoImpuestoProducto;
  impuestoPorcentaje: number | null;
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

type ProductoConId = Producto & { id?: number | string | null };

@Component({
  selector: 'app-compras',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './compras.component.html',
})
export class ComprasComponent implements OnInit {
  private static readonly IVA_PERMITIDOS = [0, 5, 19] as const;
  private static readonly INC_PORCENTAJE = 8;

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
  indiceBusquedaProductoActiva: number | null = null;
  elementoBusquedaProductoActiva: HTMLInputElement | null = null;
  posicionBusquedaProducto: PosicionBusquedaProductoOverlay | null = null;

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

  get detalleBusquedaProductoActiva(): DetalleCompraFormState | null {
    if (this.indiceBusquedaProductoActiva === null) {
      return null;
    }

    return this.nuevaCompra.detalles[this.indiceBusquedaProductoActiva] ?? null;
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
      productoBusqueda: '',
      productoBusquedaAbierta: false,
      productoBusquedaIndiceActiva: 0,
      cantidad: null,
      subtotal: null,
    });
  }

  eliminarDetalle(index: number): void {
    if (this.nuevaCompra.detalles.length === 1) {
      this.nuevaCompra.detalles[0] = {
        productoId: null,
        productoBusqueda: '',
        productoBusquedaAbierta: false,
        productoBusquedaIndiceActiva: 0,
        cantidad: null,
        subtotal: null,
      };
      return;
    }

    this.nuevaCompra.detalles.splice(index, 1);
  }

  onProductoDetalleChange(
    index: number,
    productoId: number | string | null
  ): void {
    const detalle = this.nuevaCompra.detalles[index];
    if (!detalle) {
      return;
    }

    if (productoId === OPCION_AGREGAR_PRODUCTO) {
      detalle.productoId = null;
      detalle.productoBusqueda = '';
      detalle.productoBusquedaAbierta = false;
      detalle.productoBusquedaIndiceActiva = 0;
      this.abrirModalCrearProductoRapido(index);
      return;
    }

    const productoIdNormalizado = this.normalizarProductoId(productoId);
    if (productoIdNormalizado === null) {
      detalle.productoId = null;
      return;
    }

    detalle.productoId = productoIdNormalizado;

    const productoSeleccionado = this.buscarProductoPorId(productoIdNormalizado);
    if (!productoSeleccionado) {
      detalle.productoBusqueda = '';
      detalle.productoBusquedaAbierta = false;
      detalle.productoBusquedaIndiceActiva = 0;
      return;
    }

    detalle.productoBusqueda = productoSeleccionado.nombre;
    detalle.productoBusquedaAbierta = false;
    detalle.productoBusquedaIndiceActiva = 0;

    const cantidad = this.normalizarCantidad(detalle.cantidad) ?? 1;
    if (detalle.cantidad === null || detalle.cantidad <= 0) {
      detalle.cantidad = cantidad;
    }
    if (this.normalizarValorMonetario(detalle.subtotal) === null) {
      detalle.subtotal = this.redondearMoneda(cantidad * Number(productoSeleccionado.precio));
    }
  }

  onCantidadDetalleChange(index: number): void {
    const detalle = this.nuevaCompra.detalles[index];
    if (!detalle) {
      return;
    }

    const cantidad = this.normalizarCantidad(detalle.cantidad);
    if (cantidad === null) {
      return;
    }

    const subtotal = this.normalizarValorMonetario(detalle.subtotal);
    if (subtotal !== null) {
      detalle.subtotal = subtotal;
      return;
    }

    const productoId = this.normalizarProductoId(detalle.productoId);
    if (productoId === null) {
      return;
    }

    const productoSeleccionado = this.buscarProductoPorId(productoId);
    if (!productoSeleccionado) {
      return;
    }
    detalle.subtotal = this.redondearMoneda(cantidad * Number(productoSeleccionado.precio));
  }

  onSubtotalDetalleChange(index: number): void {
    const detalle = this.nuevaCompra.detalles[index];
    if (!detalle) {
      return;
    }

    const subtotal = this.normalizarValorMonetario(detalle.subtotal);
    detalle.subtotal = subtotal;
  }

  onBusquedaProductoDetalleChange(index: number, valor: string, input?: HTMLInputElement): void {
    const detalle = this.nuevaCompra.detalles[index];
    if (!detalle) {
      return;
    }

    detalle.productoBusqueda = valor;
    detalle.productoBusquedaAbierta = true;
    detalle.productoBusquedaIndiceActiva = 0;
    this.establecerBusquedaProductoActiva(index, input);

    if (valor.trim().length === 0) {
      detalle.productoId = null;
      detalle.subtotal = null;
      this.actualizarPosicionBusquedaProductoActiva();
      return;
    }

    const productoSeleccionado = this.buscarProductoPorNombreOCodigo(valor);
    if (!productoSeleccionado) {
      detalle.productoId = null;
      detalle.subtotal = null;
      this.actualizarPosicionBusquedaProductoActiva();
      return;
    }

    const productoId = this.normalizarProductoId(this.obtenerProductoId(productoSeleccionado));
    if (productoId === null) {
      detalle.productoId = null;
      detalle.subtotal = null;
      return;
    }

    detalle.productoId = productoId;
    detalle.productoBusqueda = productoSeleccionado.nombre;
    detalle.productoBusquedaAbierta = false;
    detalle.productoBusquedaIndiceActiva = 0;
    this.limpiarBusquedaProductoActiva(index);

    const cantidad = this.normalizarCantidad(detalle.cantidad) ?? 1;
    if (detalle.cantidad === null || detalle.cantidad <= 0) {
      detalle.cantidad = cantidad;
    }
    if (this.normalizarValorMonetario(detalle.subtotal) === null) {
      detalle.subtotal = this.redondearMoneda(cantidad * Number(productoSeleccionado.precio));
    }
  }

  productosFiltradosParaDetalle(index: number): Producto[] {
    const detalle = this.nuevaCompra.detalles[index];
    if (!detalle) {
      return [];
    }

    const termino = detalle.productoBusqueda.trim().toLowerCase();
    let resultado = [...this.productosDisponibles];

    if (termino) {
      resultado = resultado.filter((producto) => {
        return (
          producto.nombre.toLowerCase().includes(termino) ||
          (this.obtenerProductoId(producto)?.toString().toLowerCase() ?? '').includes(termino) ||
          (producto.codigo ?? '').toLowerCase().includes(termino)
        );
      });
    }

    return resultado.slice(0, 8);
  }

  abrirBusquedaProductoDetalle(index: number, input?: HTMLInputElement): void {
    const detalle = this.nuevaCompra.detalles[index];
    if (!detalle) {
      return;
    }

    detalle.productoBusquedaAbierta = true;
    this.establecerBusquedaProductoActiva(index, input);
    if (detalle.productoBusquedaIndiceActiva >= this.productosFiltradosParaDetalle(index).length) {
      detalle.productoBusquedaIndiceActiva = 0;
    }

    this.actualizarPosicionBusquedaProductoActiva();
  }

  cerrarBusquedaProductoDetalle(index: number): void {
    const detalle = this.nuevaCompra.detalles[index];
    if (!detalle) {
      return;
    }

    detalle.productoBusquedaAbierta = false;
    detalle.productoBusquedaIndiceActiva = 0;
    this.limpiarBusquedaProductoActiva(index);
  }

  manejarTeclaBusquedaProductoDetalle(index: number, event: KeyboardEvent, input?: HTMLInputElement): void {
    const detalle = this.nuevaCompra.detalles[index];
    if (!detalle) {
      return;
    }

    const sugerencias = this.productosFiltradosParaDetalle(index);
    this.establecerBusquedaProductoActiva(index, input);
    if (!sugerencias.length) {
      if (event.key === 'Escape') {
        this.cerrarBusquedaProductoDetalle(index);
      }
      return;
    }

    if (event.key === 'ArrowDown') {
      event.preventDefault();
      detalle.productoBusquedaAbierta = true;
      detalle.productoBusquedaIndiceActiva = (detalle.productoBusquedaIndiceActiva + 1) % sugerencias.length;
      this.actualizarPosicionBusquedaProductoActiva();
      return;
    }

    if (event.key === 'ArrowUp') {
      event.preventDefault();
      detalle.productoBusquedaAbierta = true;
      detalle.productoBusquedaIndiceActiva =
        (detalle.productoBusquedaIndiceActiva - 1 + sugerencias.length) % sugerencias.length;
      this.actualizarPosicionBusquedaProductoActiva();
      return;
    }

    if (event.key === 'Enter') {
      event.preventDefault();
      const productoSeleccionado = sugerencias[detalle.productoBusquedaIndiceActiva] ?? sugerencias[0];
      if (productoSeleccionado) {
        this.seleccionarProductoDetalle(index, productoSeleccionado);
        this.cerrarBusquedaProductoDetalle(index);
      }
      return;
    }

    if (event.key === 'Escape') {
      event.preventDefault();
      this.cerrarBusquedaProductoDetalle(index);
    }
  }

  seleccionarProductoDetalle(index: number, producto: Producto): void {
    const detalle = this.nuevaCompra.detalles[index];
    if (!detalle) {
      return;
    }

    const productoId = this.normalizarProductoId(this.obtenerProductoId(producto));
    if (productoId === null) {
      return;
    }

    detalle.productoId = productoId;
    detalle.productoBusqueda = producto.nombre;
    detalle.productoBusquedaAbierta = false;
    detalle.productoBusquedaIndiceActiva = 0;
    this.limpiarBusquedaProductoActiva(index);

    const cantidad = this.normalizarCantidad(detalle.cantidad) ?? 1;
    if (detalle.cantidad === null || detalle.cantidad <= 0) {
      detalle.cantidad = cantidad;
    }
    if (this.normalizarValorMonetario(detalle.subtotal) === null) {
      detalle.subtotal = this.redondearMoneda(cantidad * Number(producto.precio));
    }
  }

  calcularCostoUnitarioDetalle(detalle: DetalleCompraFormState): number | null {
    const cantidad = this.normalizarCantidad(detalle.cantidad);
    const subtotal = this.normalizarValorMonetario(detalle.subtotal);
    if (cantidad === null || subtotal === null) {
      return null;
    }

    return this.redondearMoneda(subtotal / cantidad);
  }

  formatearCostoUnitarioDetalle(detalle: DetalleCompraFormState): string {
    const costoUnitario = this.calcularCostoUnitarioDetalle(detalle);
    if (costoUnitario === null) {
      return '-';
    }
    return this.formatearMoneda(costoUnitario);
  }

  obtenerProductoId(producto: Producto): number | string | null {
    const productoConId = producto as ProductoConId;
    return productoConId.id ?? null;
  }

  mostrarNombreProductoDetalle(detalle: DetalleCompraFormState): string {
    if (detalle.productoBusqueda.trim()) {
      return detalle.productoBusqueda;
    }

    const productoId = this.normalizarProductoId(detalle.productoId);
    if (productoId === null) {
      return '';
    }

    return this.buscarProductoPorId(productoId)?.nombre ?? '';
  }

  calcularSubtotalDetalle(detalle: DetalleCompraFormState): number {
    const subtotal = this.normalizarValorMonetario(detalle.subtotal);
    if (subtotal !== null) {
      return subtotal;
    }
    return 0;
  }

  crearCompra(form: NgForm): void {
    this.errorCrearCompra = null;
    this.exitoCrearCompra = null;

    if (this.nuevaCompra.proveedorId === null || Number.isNaN(Number(this.nuevaCompra.proveedorId))) {
      this.errorCrearCompra = 'Selecciona un proveedor para continuar.';
      return;
    }

    if (!this.nuevaCompra.numeroFactura.trim()) {
      this.errorCrearCompra = 'Ingresa el número de factura para continuar.';
      return;
    }

    const detallesConDatos = this.nuevaCompra.detalles.filter((detalle) => !this.esDetalleCompraVacio(detalle));
    if (detallesConDatos.some((detalle) => !this.esDetalleCompraCompleto(detalle))) {
      this.errorCrearCompra = 'Completa producto, cantidad y subtotal en cada fila de detalle.';
      return;
    }

    const detalles = detallesConDatos
      .map((detalle) => this.construirDetalleCompraRequest(detalle))
      .filter((detalle): detalle is DetalleCompraRequest => detalle !== null);

    if (!detalles.length) {
      this.errorCrearCompra = 'Agrega al menos un producto con cantidad y subtotal mayores a cero.';
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

  onTipoImpuestoProductoRapidoChange(): void {
    this.nuevoProductoRapido.impuestoPorcentaje = this.normalizarPorcentajeImpuesto(
      this.nuevoProductoRapido.tipoImpuesto,
      this.nuevoProductoRapido.impuestoPorcentaje
    );
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
      !this.esCombinacionImpuestoValida(
        this.nuevoProductoRapido.tipoImpuesto,
        this.nuevoProductoRapido.impuestoPorcentaje
      ) ||
      this.nuevoProductoRapido.stockInicial === null ||
      this.nuevoProductoRapido.stockInicial < 0 ||
      this.nuevoProductoRapido.stockMinimo === null ||
      this.nuevoProductoRapido.stockMinimo < 0
    ) {
      this.errorCrearProductoRapido =
        'Selecciona un impuesto válido: IVA (0%, 5%, 19%) o INC (8%), y verifica stock inicial/mínimo.';
      return;
    }

    const payload: CrearProducto = {
      nombre,
      precio: Number(this.nuevoProductoRapido.precio),
      costo: 0,
      tipoGestion: 'INVENTARIABLE',
      tipoImpuesto: this.nuevoProductoRapido.tipoImpuesto,
      impuestoPorcentaje: Number(this.nuevoProductoRapido.impuestoPorcentaje),
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
                return (
                  array.findIndex((item) => {
                    return (
                      this.normalizarProductoId(this.obtenerProductoId(item)) ===
                      this.normalizarProductoId(this.obtenerProductoId(producto))
                    );
                  }) === index
                );
              })
              .sort((a, b) => a.nombre.localeCompare(b.nombre, 'es', { sensitivity: 'base' }));
          }

          const indiceDetalle = this.indiceDetalleProductoRapido;
          if (indiceDetalle !== null) {
            const detalle = this.nuevaCompra.detalles[indiceDetalle];
            if (detalle) {
              detalle.productoId = this.obtenerProductoId(productoCreado);
              const cantidad = this.normalizarCantidad(detalle.cantidad) ?? 1;
              if (detalle.cantidad === null || detalle.cantidad <= 0) {
                detalle.cantidad = cantidad;
              }
              if (this.normalizarValorMonetario(detalle.subtotal) === null) {
                detalle.subtotal = this.redondearMoneda(cantidad * Number(productoCreado.precio));
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
        this.productosDisponibles = productos
          .filter((producto) => producto.activo)
          .sort((a, b) => a.nombre.localeCompare(b.nombre, 'es', { sensitivity: 'base' }));

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
          productoBusqueda: '',
          productoBusquedaAbierta: false,
          productoBusquedaIndiceActiva: 0,
          cantidad: null,
          subtotal: null,
        },
      ],
    };
  }

  private esDetalleCompraVacio(detalle: DetalleCompraFormState): boolean {
    return (
      this.normalizarProductoId(detalle.productoId) === null &&
      this.normalizarCantidad(detalle.cantidad) === null &&
      this.normalizarValorMonetario(detalle.subtotal) === null
    );
  }

  private esDetalleCompraCompleto(detalle: DetalleCompraFormState): boolean {
    return (
      this.normalizarProductoId(detalle.productoId) !== null &&
      this.normalizarCantidad(detalle.cantidad) !== null &&
      this.normalizarValorMonetario(detalle.subtotal) !== null
    );
  }

  private construirDetalleCompraRequest(detalle: DetalleCompraFormState): DetalleCompraRequest | null {
    const productoId = this.normalizarProductoId(detalle.productoId);
    if (productoId === null) {
      return null;
    }

    const cantidad = this.normalizarCantidad(detalle.cantidad);
    if (cantidad === null) {
      return null;
    }

    const subtotal = this.normalizarValorMonetario(detalle.subtotal);
    if (subtotal === null) {
      return null;
    }

    detalle.subtotal = subtotal;
    detalle.productoId = productoId;

    return {
      productoId,
      cantidad,
      subtotal,
    };
  }

  private normalizarProductoId(valor: number | string | null | undefined): number | null {
    if (valor === null || valor === undefined || valor === '' || valor === OPCION_AGREGAR_PRODUCTO) {
      return null;
    }

    const numero = Number(valor);
    if (!Number.isInteger(numero) || numero <= 0) {
      return null;
    }

    return numero;
  }

  private buscarProductoPorId(productoId: number): Producto | undefined {
    return this.productosDisponibles.find((producto) => {
      return this.normalizarProductoId(this.obtenerProductoId(producto)) === productoId;
    });
  }

  private buscarProductoPorNombreOCodigo(terminoBusqueda: string): Producto | undefined {
    const termino = terminoBusqueda.trim().toLowerCase();
    if (!termino) {
      return undefined;
    }

    return this.productosDisponibles.find((producto) => {
      const idProducto = this.obtenerProductoId(producto)?.toString().toLowerCase() ?? '';
      return (
        producto.nombre.toLowerCase() === termino ||
        idProducto === termino ||
        (producto.codigo ?? '').toLowerCase() === termino
      );
    });
  }

  private normalizarCantidad(valor: number | null): number | null {
    if (valor === null) {
      return null;
    }

    const numero = Number(valor);
    if (!Number.isFinite(numero) || numero <= 0) {
      return null;
    }

    return numero;
  }

  private normalizarValorMonetario(valor: number | null): number | null {
    if (valor === null) {
      return null;
    }

    const numero = Number(valor);
    if (!Number.isFinite(numero) || numero <= 0) {
      return null;
    }

    return this.redondearMoneda(numero);
  }

  private redondearMoneda(valor: number): number {
    return Math.round((valor + Number.EPSILON) * 100) / 100;
  }

  private crearFormularioProductoRapidoInicial(): NuevoProductoRapidoFormState {
    return {
      nombre: '',
      precio: null,
      categoriaId: this.categoriasProductoRapido[0]?.id ?? null,
      tipoImpuesto: 'IVA',
      impuestoPorcentaje: 19,
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

  @HostListener('window:resize')
  onWindowResize(): void {
    this.actualizarPosicionBusquedaProductoActiva();
  }

  onFormularioCompraScroll(): void {
    this.actualizarPosicionBusquedaProductoActiva();
  }

  private establecerBusquedaProductoActiva(index: number, input?: HTMLInputElement): void {
    this.nuevaCompra.detalles.forEach((detalle, detalleIndex) => {
      if (detalleIndex !== index) {
        detalle.productoBusquedaAbierta = false;
        detalle.productoBusquedaIndiceActiva = 0;
      }
    });

    this.indiceBusquedaProductoActiva = index;
    if (input) {
      this.elementoBusquedaProductoActiva = input;
    }
  }

  private limpiarBusquedaProductoActiva(index: number): void {
    if (this.indiceBusquedaProductoActiva === index) {
      this.indiceBusquedaProductoActiva = null;
      this.elementoBusquedaProductoActiva = null;
      this.posicionBusquedaProducto = null;
    }
  }

  private actualizarPosicionBusquedaProductoActiva(): void {
    const input = this.elementoBusquedaProductoActiva;
    const indice = this.indiceBusquedaProductoActiva;
    if (!input || indice === null) {
      this.posicionBusquedaProducto = null;
      return;
    }

    const rect = input.getBoundingClientRect();
    const margen = 12;
    const espacioAbajo = Math.max(0, window.innerHeight - rect.bottom - margen);
    const maxHeight = Math.max(140, Math.min(280, espacioAbajo));
    const width = Math.min(rect.width, window.innerWidth - margen * 2);
    const left = Math.max(margen, Math.min(rect.left, window.innerWidth - margen - width));
    const top = rect.bottom + 8;

    this.posicionBusquedaProducto = {
      top,
      left,
      width,
      maxHeight,
    };
  }

  private normalizarPorcentajeImpuesto(
    tipoImpuesto: TipoImpuestoProducto,
    impuestoPorcentaje: number | null
  ): number {
    if (tipoImpuesto === 'INC') {
      return ComprasComponent.INC_PORCENTAJE;
    }

    return this.esIvaPermitido(impuestoPorcentaje) ? Number(impuestoPorcentaje) : 19;
  }

  private esCombinacionImpuestoValida(
    tipoImpuesto: TipoImpuestoProducto | null,
    impuestoPorcentaje: number | null
  ): boolean {
    if (tipoImpuesto === 'INC') {
      return impuestoPorcentaje === ComprasComponent.INC_PORCENTAJE;
    }

    if (tipoImpuesto === 'IVA') {
      return this.esIvaPermitido(impuestoPorcentaje);
    }

    return false;
  }

  private esIvaPermitido(iva: number | null): boolean {
    return iva !== null && ComprasComponent.IVA_PERMITIDOS.includes(iva as 0 | 5 | 19);
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
