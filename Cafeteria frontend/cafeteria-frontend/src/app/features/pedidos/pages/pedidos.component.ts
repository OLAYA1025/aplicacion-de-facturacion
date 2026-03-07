import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { PedidosService } from '../services/pedidos.service';
import { ProductoService } from '../../productos/services/producto.service';
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
import { ModalDecisionComponent } from '../components/modal-decision/modal-decision.component';
import { ModalCobroComponent } from '../components/modal-cobro/modal-cobro.component';
import { FormularioPedidoComponent } from '../components/formulario-pedido/formulario-pedido.component';
import { ModalMensajeService } from '../../../shared/services/modal-mensaje.service';

@Component({
  selector: 'app-pedidos',
  standalone: true,
  imports: [
    CommonModule,
    PedidoCardComponent,
    ModalProductosComponent,
    ModalDecisionComponent,
    ModalCobroComponent,
    FormularioPedidoComponent,
  ],
  templateUrl: './pedidos.component.html'
})
export class PedidosComponent implements OnInit {
  vistaActual: 'lista' | 'crear' | 'editar' = 'lista';
  loading: boolean = false;
  guardando: boolean = false;

  pedidosPendientes: Pedido[] = [];
  productos: Producto[] = [];
  productosFiltrados: Producto[] = [];

  nombreCliente: string = '';
  busquedaProducto: string = '';
  detallesTemp: DetalleTemp[] = [];
  pedidoEditando: Pedido | null = null;

  mostrarModalDecision: boolean = false;
  pedidoRecienCreado: Pedido | null = null;
  mostrarModalCobro: boolean = false;
  mostrarModalProductos: boolean = false;
  categorias: { id: number; nombre: string }[] = [];
  categoriaSeleccionada: number | null = null;

  constructor(
    private pedidosService: PedidosService,
    private productoService: ProductoService,
    private authService: AuthService,
    private modalMensajeService: ModalMensajeService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarPedidosPendientes();
    this.cargarProductos();
  }

  cargarPedidosPendientes(): void {
    this.loading = true;
    this.pedidosService.obtenerPedidosCajaAbierta().subscribe({
      next: (pedidos) => {
        this.pedidosPendientes = pedidos.filter((p) => p.estado === 'PENDIENTE');
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('No se pudieron cargar los pedidos', error);
        this.pedidosPendientes = [];
        this.loading = false;
        this.cdr.detectChanges();
      }
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
        this.categorias = Array.from(categoriasMap.entries()).map(([id, nombre]) => ({ id, nombre }));
        
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('No se pudieron cargar los productos', error);
        this.productos = [];
        this.productosFiltrados = [];
        this.categorias = [];
        this.cdr.detectChanges();
      }
    });
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
    const busqueda = this.busquedaProducto.toLowerCase();
    let productosFiltrados = this.productos;

    if (this.categoriaSeleccionada !== null) {
      productosFiltrados = productosFiltrados.filter(
        (p) => p.categoria.id === this.categoriaSeleccionada
      );
    }

    if (busqueda) {
      productosFiltrados = productosFiltrados.filter(
        (p) =>
          p.nombre.toLowerCase().includes(busqueda) ||
          this.obtenerIdProducto(p).toString().includes(busqueda)
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
    const stockReservado = typeof producto.stockReservado === 'number' ? producto.stockReservado : 0;
    return stockActual - stockReservado;
  }

  agregarProducto(producto: Producto): void {
    const productoId = this.obtenerIdProducto(producto);
    if (productoId <= 0) {
      this.mostrarError(
        'No se pudo identificar el producto seleccionado',
        'Recarga el catálogo e intenta de nuevo.'
      );
      return;
    }

    const existente = this.detallesTemp.find(
      (d) => this.obtenerIdProducto(d.producto) === productoId
    );

    if (this.esInventariable(producto)) {
      const disponible = this.stockDisponible(producto);

      if (disponible <= 0) {
        this.mostrarError(`"${producto.nombre}" no tiene stock disponible.`);
        return;
      }

      if (existente && existente.cantidad >= disponible) {
        this.mostrarError(`Solo hay ${disponible} unidades disponibles de "${producto.nombre}".`);
        return;
      }
    }

    if (existente) {
      existente.cantidad++;
    } else {
      this.detallesTemp.push({ producto, cantidad: 1 });
    }
  }

  actualizarCantidadProductoDesdeModal(event: {
    producto: Producto;
    cantidad: number;
  }): void {
    const productoId = this.obtenerIdProducto(event.producto);
    if (productoId <= 0) return;

    let cantidad = Math.max(0, Math.floor(event.cantidad));
    if (this.esInventariable(event.producto)) {
      const disponible = Math.max(0, this.stockDisponible(event.producto));
      cantidad = Math.min(cantidad, disponible);
    }

    const index = this.detallesTemp.findIndex(
      (d) => this.obtenerIdProducto(d.producto) === productoId
    );

    if (index === -1) {
      if (cantidad > 0) {
        this.detallesTemp.push({
          producto: event.producto,
          cantidad,
        });
      }
      return;
    }

    if (cantidad === 0) {
      this.detallesTemp.splice(index, 1);
      return;
    }

    this.detallesTemp[index].cantidad = cantidad;
  }

  eliminarDetalle(index: number): void {
    this.detallesTemp.splice(index, 1);
  }

  aumentarCantidadDetalle(index: number): void {
    if (!this.detallesTemp[index]) return;
    const detalle = this.detallesTemp[index];
    if (!detalle) return;

    if (this.esInventariable(detalle.producto)) {
      const disponible = this.stockDisponible(detalle.producto);
      if (detalle.cantidad >= disponible) return;
    }

    detalle.cantidad++;
  }

  disminuirCantidadDetalle(index: number): void {
    if (!this.detallesTemp[index]) return;
    this.detallesTemp[index].cantidad =
      this.detallesTemp[index].cantidad > 1
        ? this.detallesTemp[index].cantidad - 1
        : 1;
  }

  get totalPedido(): number {
    return this.detallesTemp.reduce(
      (sum, d) => sum + d.producto.precio * d.cantidad,
      0
    );
  }

  nuevoPedido(): void {
    this.vistaActual = 'crear';
    this.pedidoEditando = null;
    this.nombreCliente = '';
    this.detallesTemp = [];
    this.busquedaProducto = '';
  }

  editarPedido(pedido: Pedido): void {
    this.vistaActual = 'editar';
    this.pedidoEditando = pedido;
    this.nombreCliente = pedido.nombre;

    this.detallesTemp = pedido.detalles
      .map((d) => {
        const producto = this.productos.find((p) => this.obtenerIdProducto(p) === d.productoId);
        if (!producto) return null;
        return { producto, cantidad: d.cantidad };
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
      cantidad: d.cantidad
    }));

    if (detalles.some((detalle) => detalle.productoId <= 0)) {
      this.mostrarError(
        'Hay productos sin identificador válido',
        'Recarga el catálogo e intenta nuevamente.'
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
      detalles
    };

    this.pedidosService.crearPedido(request).subscribe({
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
          'No se pudo crear el pedido',
          this.obtenerMensajeError(error, 'Verifica los datos e intenta nuevamente.'),
          error
        );
        this.guardando = false;
      }
    });
  }

  private actualizarPedidoExistente(detalles: DetallePedidoRequest[]): void {
    if (!this.pedidoEditando) {
      this.guardando = false;
      return;
    }

    const request: ActualizarPedidoRequest = {
      nombre: this.nombreCliente,
      detalles
    };

    this.pedidosService
      .actualizarPedido(this.pedidoEditando.id, request)
      .subscribe({
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
          error
        );
        this.guardando = false;
      }
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

  cerrarModalDecision(): void {
    this.mostrarModalDecision = false;
    this.pedidoRecienCreado = null;
    this.nombreCliente = '';
    this.detallesTemp = [];
    this.pedidoEditando = null;
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
      metodoPago: 'EFECTIVO'
    };

    this.pedidosService.cancelarPedido(pedido.id, request).subscribe({
      next: () => {
        this.cargarPedidosPendientes();
        this.cargarProductos();
      },
      error: (error) => {
        this.mostrarError('No se pudo cancelar el pedido', 'Intenta nuevamente.', error);
      }
    });
  }

  volverALista(): void {
    this.vistaActual = 'lista';
    this.pedidoEditando = null;
    this.nombreCliente = '';
    this.detallesTemp = [];
  }

  actualizarNombreCliente(nombreCliente: string): void {
    this.nombreCliente = nombreCliente;
  }

  private validarDisponibilidadInventariables(): string | null {
    for (const detalle of this.detallesTemp) {
      if (!this.esInventariable(detalle.producto)) {
        continue;
      }

      const disponible = Math.max(0, this.stockDisponible(detalle.producto));
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
            'No se pudo generar la vista previa de la factura. Intenta nuevamente.'
          ),
          error
        );
      },
    });
  }
}
