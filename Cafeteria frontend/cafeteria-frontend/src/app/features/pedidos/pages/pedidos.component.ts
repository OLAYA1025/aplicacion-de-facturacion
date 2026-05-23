import { Component, OnInit, ChangeDetectorRef, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PedidosService } from '../services/pedidos.service';
import { ProductoService } from '../../productos/services/producto.service';
import { CategoriaService } from '../../productos/services/categoria.service';
import { AuthService } from '../../../core/services/auth.service';
import { Pedido } from '../../../core/models/pedido.model';
import { Producto } from '../../../core/models/producto.model';
import { CrearPedidoRequest } from '../models/crearPedido.model';
import { ActualizarPedidoRequest } from '../models/actualizarPedido.model';
import { DetallePedidoRequest } from '../models/detallePedido.model';
import { PedidoUsuarioRequest } from '../models/pedidoUsuario.model';
import { DetalleTemp } from '../models/detalleTemp.model';
import { PedidoCardComponent } from '../components/pedido-card/pedido-card.component';
import { ModalProductosComponent } from '../components/modal-productos/modal-productos.component';
import { ModalNotasProductoComponent } from '../components/modal-notas-producto/modal-notas-producto.component';
import { ModalDecisionComponent } from '../components/modal-decision/modal-decision.component';
import { ModalCobroComponent } from '../components/modal-cobro/modal-cobro.component';
import { FormularioPedidoComponent } from '../components/formulario-pedido/formulario-pedido.component';
import { ModalMensajeService } from '../../../shared/services/modal-mensaje.service';
import {
  DetallePedidoNota,
  GrupoNotaCategoria,
} from '../../../core/models/notas-predefinidas.model';

@Component({
  selector: 'app-pedidos',
  standalone: true,
  imports: [
    CommonModule,
    PedidoCardComponent,
    ModalProductosComponent,
    ModalNotasProductoComponent,
    ModalDecisionComponent,
    ModalCobroComponent,
    FormularioPedidoComponent,
  ],
  templateUrl: './pedidos.component.html',
})
export class PedidosComponent implements OnInit, OnDestroy {
  vistaActual: 'lista' | 'crear' | 'editar' = 'lista';
  loading: boolean = false;
  guardando: boolean = false;
  imprimiendoPrefacturaId: number | null = null;

  pedidosPendientes: Pedido[] = [];
  productos: Producto[] = [];
  productosFiltrados: Producto[] = [];

  nombreCliente: string = '';
  busquedaProducto: string = '';
  detallesTemp: DetalleTemp[] = [];
  pedidoEditando: Pedido | null = null;
  cantidadesOriginalesPedidoEditando: Record<number, number> = {};

  mostrarModalDecision: boolean = false;
  pedidoRecienCreado: Pedido | null = null;
  mostrarModalCobro: boolean = false;
  mostrarModalProductos: boolean = false;
  mostrarModalNotasProducto: boolean = false;
  categorias: { id: number; nombre: string }[] = [];
  categoriaSeleccionada: number | null = null;
  productoNotasActual: Producto | null = null;
  gruposNotasProductoActual: GrupoNotaCategoria[] = [];
  notaIdsSeleccionadasProductoActual: number[] = [];
  cantidadProductoActual = 1;
  cantidadMaximaProductoActual: number | null = null;
  private readonly gruposNotasPorCategoria = new Map<number, GrupoNotaCategoria[]>();

  private eventSource: EventSource | null = null;
  private reconnectTimeoutId: number | null = null;
  private destroyed = false;

  constructor(
    private pedidosService: PedidosService,
    private productoService: ProductoService,
    private categoriaService: CategoriaService,
    private authService: AuthService,
    private modalMensajeService: ModalMensajeService,
    private cdr: ChangeDetectorRef,
  ) {}

  ngOnInit(): void {
    this.destroyed = false;
    this.cargarPedidosPendientes();
    this.cargarProductos();
    this.conectarSSE();
  }

  cargarPedidosPendientes(): void {
    this.loading = true;
    this.pedidosService.obtenerPedidosCajaAbierta().subscribe({
      next: (pedidos) => {
        this.pedidosPendientes = pedidos.filter(
          (p) => p.estado === 'PENDIENTE' || p.estado === 'CANCELADO',
        );
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('No se pudieron cargar los pedidos', error);
        this.pedidosPendientes = [];
        this.loading = false;
        this.cdr.detectChanges();
      },
    });
  }

  cargarProductos(): void {
    this.productoService.obtenerProductos().subscribe({
      next: (productos) => {
        this.productos = productos.filter((p) => p.activo);
        if (this.busquedaProducto || this.categoriaSeleccionada !== null) {
          this.filtrarProductos();
        } else {
          this.productosFiltrados = this.productos;
        }

        const categoriasMap = new Map<number, string>();
        productos.forEach((p) => {
          if (!categoriasMap.has(p.categoria.id)) {
            categoriasMap.set(p.categoria.id, p.categoria.nombre);
          }
        });
        this.categorias = Array.from(categoriasMap.entries()).map(([id, nombre]) => ({
          id,
          nombre,
        }));
        this.gruposNotasPorCategoria.clear();

        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('No se pudieron cargar los productos', error);
        this.productos = [];
        this.productosFiltrados = [];
        this.categorias = [];
        this.cdr.detectChanges();
      },
    });
  }

  conectarSSE(): void {
    if (this.destroyed) {
      return;
    }

    this.cerrarConexionSSE();
    this.eventSource = this.pedidosService.nuevoListenerPedidos();

    this.eventSource.onopen = () => {
      if (this.reconnectTimeoutId !== null) {
        window.clearTimeout(this.reconnectTimeoutId);
        this.reconnectTimeoutId = null;
      }
    };

    this.eventSource.addEventListener('nuevo-pedido', (event: MessageEvent<string>) => {
      try {
        const pedido = JSON.parse(event.data) as Pedido;
        const index = this.pedidosPendientes.findIndex((p) => p.id === pedido.id);

        if (pedido.estado === 'PENDIENTE' || pedido.estado === 'CANCELADO') {
          this.pedidosPendientes =
            index !== -1
              ? this.pedidosPendientes.map((actual, position) =>
                  position === index ? pedido : actual,
                )
              : [pedido, ...this.pedidosPendientes];
        } else if (index !== -1) {
          this.pedidosPendientes = this.pedidosPendientes.filter((p) => p.id !== pedido.id);
        }

        this.cdr.detectChanges();
      } catch (error) {
        console.error('No se pudo procesar el evento SSE de pedidos', error);
      }
    });

    this.eventSource.onerror = (error) => {
      console.error('Error SSE de pedidos. Reintentando conexión...', error);
      this.cerrarConexionSSE();
      this.programarReconexionSSE();
    };
  }

  ngOnDestroy(): void {
    this.destroyed = true;
    this.cerrarConexionSSE();
    if (this.reconnectTimeoutId !== null) {
      window.clearTimeout(this.reconnectTimeoutId);
      this.reconnectTimeoutId = null;
    }
  }

  private cerrarConexionSSE(): void {
    if (this.eventSource) {
      this.eventSource.close();
      this.eventSource = null;
    }
  }

  private programarReconexionSSE(): void {
    if (this.destroyed || this.reconnectTimeoutId !== null) {
      return;
    }

    this.reconnectTimeoutId = window.setTimeout(() => {
      this.reconnectTimeoutId = null;
      this.conectarSSE();
    }, 5000);
  }

  abrirModalProductos(): void {
    this.mostrarModalProductos = true;
    this.categoriaSeleccionada = null;
    this.busquedaProducto = '';
    this.cargarProductos();
  }

  cerrarModalProductos(): void {
    this.mostrarModalProductos = false;
  }

  filtrarProductos(): void {
    const busqueda = this.busquedaProducto.trim().toLowerCase();
    let productosFiltrados = this.productos;

    if (this.categoriaSeleccionada !== null) {
      productosFiltrados = productosFiltrados.filter(
        (p) => p.categoria.id === this.categoriaSeleccionada,
      );
    }

    if (busqueda) {
      productosFiltrados = productosFiltrados.filter(
        (p) =>
          p.nombre.toLowerCase().includes(busqueda) ||
          (p.codigo ?? '').toLowerCase().includes(busqueda) ||
          this.obtenerIdProducto(p).toString().includes(busqueda),
      );
    }

    this.productosFiltrados = productosFiltrados;
  }

  seleccionarCategoria(categoriaId: number | null): void {
    this.categoriaSeleccionada = categoriaId;
    this.filtrarProductos();
  }

  actualizarBusquedaProducto(busqueda: string): void {
    this.busquedaProducto = busqueda;
    this.filtrarProductos();
  }

  agregarProductoPorCodigoEscaneado(codigo: string): void {
    const producto = this.buscarProductoPorCodigo(codigo);
    if (!producto) {
      return;
    }

    if (this.agregarProducto(producto)) {
      this.busquedaProducto = '';
      this.filtrarProductos();
      this.refrescarVista();
    }
  }

  cerrarModalNotasProducto(): void {
    this.mostrarModalNotasProducto = false;
    this.productoNotasActual = null;
    this.gruposNotasProductoActual = [];
    this.notaIdsSeleccionadasProductoActual = [];
    this.cantidadProductoActual = 1;
    this.cantidadMaximaProductoActual = null;
    this.refrescarVista();
  }

  confirmarNotasProducto(evento: { notaIdsSeleccionadas: number[]; cantidad: number }): void {
    if (!this.productoNotasActual) {
      this.cerrarModalNotasProducto();
      return;
    }

    const producto = this.productoNotasActual;
    const productoId = this.obtenerIdProducto(producto);
    if (productoId <= 0) {
      this.cerrarModalNotasProducto();
      return;
    }

    const notasSeleccionadas = this.construirNotasSeleccionadas(
      evento.notaIdsSeleccionadas,
      this.gruposNotasProductoActual,
    );
    const cantidad = Math.max(1, Math.floor(evento.cantidad));
    this.detallesTemp.push({
      producto,
      cantidad,
      notas: notasSeleccionadas,
    });

    this.cerrarModalNotasProducto();
  }

  private obtenerIdProducto(producto: Producto): number {
    const codigo = (producto as { codigo?: unknown }).codigo;
    if (typeof codigo === 'number' && Number.isFinite(codigo)) {
      return codigo;
    }

    const id = (producto as { id?: unknown }).id;
    if (typeof id === 'number' && Number.isFinite(id)) {
      return id;
    }

    return 0;
  }

  private esInventariable(producto: Producto): boolean {
    return producto.tipoGestion === 'INVENTARIABLE';
  }

  private stockDisponible(producto: Producto): number {
    const stockActual = typeof producto.stockActual === 'number' ? producto.stockActual : 0;
    const stockReservado =
      typeof producto.stockReservado === 'number' ? producto.stockReservado : 0;
    return stockActual - stockReservado;
  }

  private cantidadOriginalPedidoEditando(producto: Producto): number {
    if (this.vistaActual !== 'editar') {
      return 0;
    }

    const productoId = this.obtenerIdProducto(producto);
    if (productoId <= 0) {
      return 0;
    }

    return this.cantidadesOriginalesPedidoEditando[productoId] ?? 0;
  }

  private stockDisponibleParaPedido(producto: Producto): number {
    return this.stockDisponible(producto) + this.cantidadOriginalPedidoEditando(producto);
  }

  private cantidadTotalProductoEnDetallesTemp(productoId: number): number {
    if (productoId <= 0) {
      return 0;
    }
    return this.detallesTemp
      .filter((detalle) => this.obtenerIdProducto(detalle.producto) === productoId)
      .reduce((total, detalle) => total + detalle.cantidad, 0);
  }

  private cantidadMaximaNuevaLinea(producto: Producto): number {
    if (!this.esInventariable(producto)) {
      return 9999;
    }

    const productoId = this.obtenerIdProducto(producto);
    if (productoId <= 0) {
      return 0;
    }

    const totalPermitido = Math.max(0, this.stockDisponibleParaPedido(producto));
    const cantidadYaSeleccionada = this.cantidadTotalProductoEnDetallesTemp(productoId);
    return Math.max(0, totalPermitido - cantidadYaSeleccionada);
  }

  private procesarSeleccionProductoConGrupos(
    producto: Producto,
    gruposNotasCategoria: GrupoNotaCategoria[],
  ): void {
    const cantidadMaxima = this.cantidadMaximaNuevaLinea(producto);
    if (this.esInventariable(producto) && cantidadMaxima <= 0) {
      this.mostrarError(`"${producto.nombre}" no tiene stock disponible.`);
      return;
    }

    this.productoNotasActual = producto;
    this.gruposNotasProductoActual = gruposNotasCategoria;
    this.notaIdsSeleccionadasProductoActual = [];
    this.cantidadProductoActual = 1;
    this.cantidadMaximaProductoActual = this.esInventariable(producto) ? cantidadMaxima : null;
    this.mostrarModalNotasProducto = true;
    this.refrescarVista();
  }

  private construirNotasSeleccionadas(
    notaIdsSeleccionadas: number[],
    grupos: GrupoNotaCategoria[],
  ): DetallePedidoNota[] {
    if (!notaIdsSeleccionadas.length) {
      return [];
    }

    const idsSet = new Set(notaIdsSeleccionadas);
    const notas: DetallePedidoNota[] = [];

    grupos.forEach((grupo) => {
      grupo.notas.forEach((nota) => {
        if (!idsSet.has(nota.id)) {
          return;
        }
        notas.push({
          id: 0,
          notaPredefinidaId: nota.id,
          descripcion: nota.descripcion,
          grupoId: grupo.id,
          grupoNombre: grupo.nombre,
        });
      });
    });

    return notas;
  }

  agregarProducto(producto: Producto): boolean {
    const productoId = this.obtenerIdProducto(producto);
    if (productoId <= 0) {
      this.mostrarError(
        'No se pudo identificar el producto seleccionado',
        'Recarga el catálogo e intenta de nuevo.',
      );
      return false;
    }

    if (this.esInventariable(producto)) {
      const disponibleNuevaLinea = this.cantidadMaximaNuevaLinea(producto);
      if (disponibleNuevaLinea <= 0) {
        this.mostrarError(`"${producto.nombre}" no tiene stock disponible.`);
        return false;
      }
    }

    const categoriaId = producto.categoria?.id;
    if (!categoriaId) {
      this.procesarSeleccionProductoConGrupos(producto, []);
      return true;
    }

    const gruposEnCache = this.gruposNotasPorCategoria.get(categoriaId);
    if (gruposEnCache) {
      this.procesarSeleccionProductoConGrupos(producto, gruposEnCache);
      return true;
    }

    this.categoriaService.obtenerGruposNotasPorCategoria(categoriaId).subscribe({
      next: (gruposNotas) => {
        this.gruposNotasPorCategoria.set(categoriaId, gruposNotas);
        this.procesarSeleccionProductoConGrupos(producto, gruposNotas);
        this.refrescarVista();
      },
      error: (error) => {
        this.mostrarError(
          'No se pudo cargar la configuración de notas del producto',
          'Intenta nuevamente en unos segundos.',
          error,
        );
      },
    });
    return true;
  }

  private buscarProductoPorCodigo(codigo: string): Producto | undefined {
    return this.productos.find((producto) => {
      const codigoProducto = producto.codigo?.trim();
      return !!codigoProducto && codigoProducto === codigo;
    });
  }

  eliminarDetalle(index: number): void {
    this.detallesTemp.splice(index, 1);
  }

  aumentarCantidadDetalle(index: number): void {
    if (!this.detallesTemp[index]) return;
    const detalle = this.detallesTemp[index];
    if (!detalle) return;

    if (this.esInventariable(detalle.producto)) {
      const productoId = this.obtenerIdProducto(detalle.producto);
      const totalPermitido = Math.max(0, this.stockDisponibleParaPedido(detalle.producto));
      const totalSinLineaActual =
        this.cantidadTotalProductoEnDetallesTemp(productoId) - detalle.cantidad;
      const maximoLinea = Math.max(1, totalPermitido - totalSinLineaActual);
      if (detalle.cantidad >= maximoLinea) return;
    }

    detalle.cantidad++;
  }

  disminuirCantidadDetalle(index: number): void {
    if (!this.detallesTemp[index]) return;
    this.detallesTemp[index].cantidad =
      this.detallesTemp[index].cantidad > 1 ? this.detallesTemp[index].cantidad - 1 : 1;
  }

  get totalPedido(): number {
    return this.detallesTemp.reduce((sum, d) => sum + d.producto.precio * d.cantidad, 0);
  }

  nuevoPedido(): void {
    this.vistaActual = 'crear';
    this.pedidoEditando = null;
    this.cantidadesOriginalesPedidoEditando = {};
    this.nombreCliente = '';
    this.detallesTemp = [];
    this.busquedaProducto = '';
    this.cerrarModalNotasProducto();
  }

  editarPedido(pedido: Pedido): void {
    this.vistaActual = 'editar';
    this.pedidoEditando = pedido;
    this.cantidadesOriginalesPedidoEditando = pedido.detalles.reduce<Record<number, number>>(
      (cantidades, detalle) => {
        cantidades[detalle.productoId] = (cantidades[detalle.productoId] ?? 0) + detalle.cantidad;
        return cantidades;
      },
      {},
    );
    this.nombreCliente = pedido.nombre;

    this.detallesTemp = pedido.detalles
      .map((d) => {
        const producto = this.productos.find((p) => this.obtenerIdProducto(p) === d.productoId);
        if (!producto) return null;
        return { producto, cantidad: d.cantidad, notas: d.notas ?? [] };
      })
      .filter((d): d is DetalleTemp => d !== null);
  }

  crearPedido(): void {
    if (this.detallesTemp.length === 0) {
      this.mostrarError('Agrega productos al pedido');
      return;
    }

    if (this.guardando) return;
    this.guardando = true;

    const detalles: DetallePedidoRequest[] = this.detallesTemp.map((d) => ({
      productoId: this.obtenerIdProducto(d.producto),
      cantidad: d.cantidad,
      notaPredefinidaIds: d.notas.map((nota) => nota.notaPredefinidaId),
    }));

    if (detalles.some((detalle) => detalle.productoId <= 0)) {
      this.mostrarError(
        'Hay productos sin identificador válido',
        'Recarga el catálogo e intenta nuevamente.',
      );
      this.guardando = false;
      return;
    }

    const errorDisponibilidad = this.validarDisponibilidadInventariables();
    if (errorDisponibilidad) {
      this.mostrarError(errorDisponibilidad);
      this.guardando = false;
      return;
    }

    if (this.vistaActual === 'crear') {
      this.crearNuevoPedido(detalles);
    } else {
      this.actualizarPedidoExistente(detalles);
    }
  }

  private crearNuevoPedido(detalles: DetallePedidoRequest[]): void {
    const request: CrearPedidoRequest = {
      nombre: this.nombreCliente,
      detalles,
    };

    this.pedidosService.crearPedido(request).subscribe({
      next: (pedido) => {
        this.guardando = false;
        this.pedidoRecienCreado = pedido;
        this.vistaActual = 'lista';
        this.pedidoEditando = null;
        this.cantidadesOriginalesPedidoEditando = {};
        this.mostrarModalDecision = true;
        this.cargarProductos();

        this.cdr.detectChanges();
      },
      error: (error) => {
        this.mostrarError(
          'No se pudo crear el pedido',
          this.obtenerMensajeError(error, 'Verifica los datos e intenta nuevamente.'),
          error,
        );
        this.guardando = false;
      },
    });
  }

  private actualizarPedidoExistente(detalles: DetallePedidoRequest[]): void {
    if (!this.pedidoEditando) {
      this.guardando = false;
      return;
    }

    const request: ActualizarPedidoRequest = {
      nombre: this.nombreCliente,
      detalles,
    };

    this.pedidosService.actualizarPedido(this.pedidoEditando.id, request).subscribe({
      next: (pedido) => {
        this.guardando = false;
        this.pedidoRecienCreado = pedido;
        this.vistaActual = 'lista';
        this.mostrarModalDecision = true;
        this.cargarProductos();

        this.cdr.detectChanges();
      },
      error: (error) => {
        this.mostrarError(
          'No se pudo actualizar el pedido',
          this.obtenerMensajeError(error, 'Verifica los datos e intenta nuevamente.'),
          error,
        );
        this.guardando = false;
      },
    });
  }

  dejarPendiente(): void {
    this.cerrarModalDecision();
    this.cargarPedidosPendientes();
    this.cargarProductos();
  }

  irACobrar(): void {
    if (!this.pedidoRecienCreado) return;

    this.mostrarModalDecision = false;
    this.mostrarModalCobro = true;
  }

  cobrarPedidoPendiente(pedido: Pedido): void {
    this.pedidoRecienCreado = pedido;
    this.mostrarModalCobro = true;
  }

  imprimirPrefactura(pedido: Pedido): void {
    if (pedido.estado !== 'PENDIENTE' || this.imprimiendoPrefacturaId !== null) {
      return;
    }

    this.imprimiendoPrefacturaId = pedido.id;

    this.pedidosService.imprimirPrefactura(pedido.id).subscribe({
      next: () => {
        this.imprimiendoPrefacturaId = null;
      },
      error: (error) => {
        this.imprimiendoPrefacturaId = null;
        this.mostrarError(
          'No se pudo imprimir la prefactura',
          this.obtenerMensajeError(
            error,
            'No se pudo generar la vista previa de la prefactura. Intenta nuevamente.',
          ),
          error,
        );
      },
    });
  }

  cerrarModalDecision(): void {
    this.mostrarModalDecision = false;
    this.pedidoRecienCreado = null;
    this.nombreCliente = '';
    this.detallesTemp = [];
    this.pedidoEditando = null;
    this.cantidadesOriginalesPedidoEditando = {};
    this.cerrarModalNotasProducto();
  }

  cerrarModalCobro(): void {
    this.mostrarModalCobro = false;
    this.pedidoRecienCreado = null;
    this.cargarPedidosPendientes();
    this.cargarProductos();
  }

  onCobroRealizado(): void {
    const pedidoCobradoId = this.pedidoRecienCreado?.id ?? null;
    this.cerrarModalCobro();

    if (!pedidoCobradoId) {
      return;
    }

    void this.confirmarImpresionFactura(pedidoCobradoId);
  }

  cancelarPedido(pedido: Pedido): void {
    if (!confirm(`¿Seguro que deseas cancelar el pedido "${pedido.nombre}"?`)) return;

    const usuario = this.authService.getUsuario();
    if (!usuario?.id) return;

    const request: PedidoUsuarioRequest = {
      usuarioId: usuario.id,
      metodoPago: 'EFECTIVO',
    };

    this.pedidosService.cancelarPedido(pedido.id, request).subscribe({
      next: () => {
        this.cargarPedidosPendientes();
        this.cargarProductos();
      },
      error: (error) => {
        this.mostrarError('No se pudo cancelar el pedido', 'Intenta nuevamente.', error);
      },
    });
  }

  volverALista(): void {
    this.vistaActual = 'lista';
    this.pedidoEditando = null;
    this.cantidadesOriginalesPedidoEditando = {};
    this.nombreCliente = '';
    this.detallesTemp = [];
    this.cerrarModalNotasProducto();
  }

  actualizarNombreCliente(nombreCliente: string): void {
    this.nombreCliente = nombreCliente;
  }

  private validarDisponibilidadInventariables(): string | null {
    const cantidadesPorProducto = new Map<number, { producto: Producto; cantidad: number }>();

    for (const detalle of this.detallesTemp) {
      const productoId = this.obtenerIdProducto(detalle.producto);
      if (productoId <= 0 || !this.esInventariable(detalle.producto)) {
        continue;
      }

      const acumulado = cantidadesPorProducto.get(productoId);
      if (acumulado) {
        acumulado.cantidad += detalle.cantidad;
      } else {
        cantidadesPorProducto.set(productoId, {
          producto: detalle.producto,
          cantidad: detalle.cantidad,
        });
      }
    }

    for (const acumulado of cantidadesPorProducto.values()) {
      const detalle = acumulado;
      const disponible = Math.max(0, this.stockDisponibleParaPedido(detalle.producto));
      if (detalle.cantidad > disponible) {
        return `"${detalle.producto.nombre}" solo tiene ${disponible} unidades disponibles.`;
      }
    }

    return null;
  }

  private obtenerMensajeError(error: unknown, fallback: string): string {
    if (typeof error === 'string' && error.trim().length > 0) {
      return error;
    }

    const errorObject = error as
      | {
          message?: unknown;
          error?: unknown;
        }
      | undefined;

    const payload = errorObject?.error;

    if (typeof payload === 'string' && payload.trim().length > 0) {
      return payload;
    }

    if (payload && typeof payload === 'object') {
      const payloadObject = payload as {
        message?: unknown;
        mensaje?: unknown;
        error?: unknown;
      };

      if (typeof payloadObject.message === 'string' && payloadObject.message.trim().length > 0) {
        return payloadObject.message;
      }

      if (typeof payloadObject.mensaje === 'string' && payloadObject.mensaje.trim().length > 0) {
        return payloadObject.mensaje;
      }

      if (typeof payloadObject.error === 'string' && payloadObject.error.trim().length > 0) {
        return payloadObject.error;
      }
    }

    if (typeof errorObject?.message === 'string' && errorObject.message.trim().length > 0) {
      return errorObject.message;
    }

    return fallback;
  }

  private mostrarError(titulo: string, mensaje: string = '', error?: unknown): void {
    if (error !== undefined) {
      console.error(titulo, error);
    }

    void this.modalMensajeService.error({
      titulo,
      mensaje,
      textoConfirmar: 'Entendido',
    });
  }

  private async confirmarImpresionFactura(pedidoId: number): Promise<void> {
    const confirmarImpresion = await this.modalMensajeService.confirmar({
      titulo: 'Pedido cobrado correctamente',
      mensaje: '¿Deseas imprimir la factura?',
      textoConfirmar: 'Imprimir',
    });

    if (!confirmarImpresion) {
      return;
    }

    this.pedidosService.imprimirFactura(pedidoId).subscribe({
      next: (resultado) => {
        if (resultado.tipo !== 'PARCIAL') {
          return;
        }

        void this.modalMensajeService.abrir({
          tipo: 'confirmacion',
          titulo: 'Factura en proceso',
          mensaje: resultado.mensaje,
          textoConfirmar: 'Entendido',
        });
      },
      error: (error) => {
        this.mostrarError(
          'No se pudo imprimir la factura',
          this.obtenerMensajeError(
            error,
            'No se pudo generar la vista previa de la factura. Intenta nuevamente.',
          ),
          error,
        );
      },
    });
  }

  private refrescarVista(): void {
    queueMicrotask(() => {
      this.cdr.detectChanges();
    });
  }
}
