import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
  ViewRef,
  inject,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { Cliente } from 'src/app/core/models/cliente.model';
import { AuthService } from 'src/app/core/services/auth.service';
import { ClienteService } from 'src/app/features/cliente/services/cliente.service';
import { obtenerMensajeError } from 'src/app/shared/utils/error-message.util';
import { Pedido } from '../../../../core/models/pedido.model';
import {
  MetodoPago,
  PedidoUsuarioRequest,
  TipoDocumentoFiscal,
} from '../../models/pedidoUsuario.model';
import { PedidosService } from '../../services/pedidos.service';

type TipoIdentificacionCliente = Cliente['tipoIdentificacion'];
type PasoCobro = 'PREGUNTA_FACTURA' | 'BUSCAR_CLIENTE' | 'CONFIRMAR_CLIENTE' | 'CREAR_CLIENTE';

interface BusquedaClienteFormState {
  tipoIdentificacion: TipoIdentificacionCliente;
  numeroIdentificacion: string;
}

interface ClienteFormState {
  nombre: string;
  email: string;
}

interface TipoIdentificacionOption {
  value: TipoIdentificacionCliente;
  label: string;
}

@Component({
  selector: 'app-modal-cobro',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './modal-cobro.component.html',
})
export class ModalCobroComponent implements OnChanges {
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly authService = inject(AuthService);
  private readonly pedidosService = inject(PedidosService);
  private readonly clienteService = inject(ClienteService);

  readonly tiposIdentificacion: TipoIdentificacionOption[] = [
    { value: 'CC', label: 'CC' },
    { value: 'NIT', label: 'NIT' },
    { value: 'CE', label: 'CE' },
    { value: 'PAS', label: 'PASAPORTE' },
  ];

  @Input() visible: boolean = false;
  @Input() pedido: Pedido | null = null;

  @Output() cerrar = new EventEmitter<void>();
  @Output() cobroRealizado = new EventEmitter<void>();

  pedidoId: number | null = null;
  usuarioId: number | null = null;
  metodoPagoSeleccionado: MetodoPago = 'EFECTIVO';
  montoRecibido: number = 0;

  solicitaFactura: boolean | null = null;
  tipoDocumentoFiscal: TipoDocumentoFiscal | null = null;
  pasoCobro: PasoCobro = 'PREGUNTA_FACTURA';

  clienteSeleccionado: Cliente | null = null;
  busquedaCliente: BusquedaClienteFormState = this.crearFormularioBusqueda();
  clienteForm: ClienteFormState = this.crearFormularioCliente();
  editandoCliente = false;

  buscandoCliente = false;
  guardandoCliente = false;
  guardandoCobro = false;

  errorBusquedaCliente: string | null = null;
  mensajeNoEncontrado: string | null = null;
  errorCliente: string | null = null;
  exitoCliente: string | null = null;
  errorCobro: string | null = null;

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible']) {
      if (this.visible) {
        this.inicializarFlujoCobro();
      } else {
        this.limpiarEstadoCobro();
      }
      return;
    }

    if (changes['pedido'] && this.visible) {
      this.inicializarFlujoCobro();
    }
  }

  onMetodoPagoChange(metodoPago: MetodoPago): void {
    this.metodoPagoSeleccionado = metodoPago;
    this.errorCobro = null;
  }

  onMontoRecibidoChange(valor: string | number): void {
    this.montoRecibido = Math.max(0, Number(valor) || 0);
    this.errorCobro = null;
  }

  onSeleccionSolicitudFactura(solicitaFactura: boolean): void {
    this.solicitaFactura = solicitaFactura;
    this.tipoDocumentoFiscal = solicitaFactura ? 'FEV' : 'FEV_CONS_FINAL';
    this.errorCobro = null;

    if (solicitaFactura) {
      this.pasoCobro = 'BUSCAR_CLIENTE';
      return;
    }

    this.reiniciarEstadoCliente();
    this.pasoCobro = 'PREGUNTA_FACTURA';
  }

  onCerrar(): void {
    if (this.estaProcesando) return;
    this.limpiarEstadoCobro();
    this.cerrar.emit();
  }

  volverAPreguntaFactura(): void {
    this.pasoCobro = 'PREGUNTA_FACTURA';
    this.reiniciarEstadoCliente();
  }

  volverABuscarCliente(): void {
    this.pasoCobro = 'BUSCAR_CLIENTE';
    this.editandoCliente = false;
    this.errorCliente = null;
    this.exitoCliente = null;
  }

  buscarCliente(): void {
    this.errorBusquedaCliente = null;
    this.mensajeNoEncontrado = null;
    this.errorCliente = null;
    this.exitoCliente = null;

    const numeroIdentificacion = this.busquedaCliente.numeroIdentificacion.trim();
    if (!numeroIdentificacion) {
      this.errorBusquedaCliente = 'El numero de identificacion es obligatorio.';
      return;
    }

    this.buscandoCliente = true;
    this.clienteService
      .obtenerPorIdentificacion(this.busquedaCliente.tipoIdentificacion, numeroIdentificacion)
      .pipe(
        finalize(() => {
          this.buscandoCliente = false;
          this.refrescarVista();
        })
      )
      .subscribe({
        next: (cliente) => {
          this.busquedaCliente.numeroIdentificacion = numeroIdentificacion;
          this.clienteSeleccionado = cliente;
          this.clienteForm = {
            nombre: cliente.nombre,
            email: cliente.email ?? '',
          };
          this.pasoCobro = 'CONFIRMAR_CLIENTE';
          this.editandoCliente = false;
          this.refrescarVista();
        },
        error: (error: unknown) => {
          if (this.esRespuesta404(error)) {
            this.abrirFlujoCrearCliente(numeroIdentificacion);
            this.refrescarVista();
            return;
          }

          this.errorBusquedaCliente = obtenerMensajeError(
            error,
            'No se pudo buscar el cliente. Verifica los datos e intenta nuevamente.'
          );
          this.refrescarVista();
        },
      });
  }

  habilitarEdicionCliente(): void {
    if (!this.clienteSeleccionado) return;
    this.editandoCliente = true;
    this.errorCliente = null;
    this.exitoCliente = null;
  }

  cancelarEdicionCliente(): void {
    if (!this.clienteSeleccionado) return;

    this.editandoCliente = false;
    this.errorCliente = null;
    this.clienteForm = {
      nombre: this.clienteSeleccionado.nombre,
      email: this.clienteSeleccionado.email ?? '',
    };
  }

  guardarEdicionCliente(): void {
    this.errorCliente = null;
    this.exitoCliente = null;

    if (!this.clienteSeleccionado) {
      this.errorCliente = 'No hay un cliente seleccionado para editar.';
      return;
    }

    const nombre = this.clienteForm.nombre.trim();
    if (!nombre) {
      this.errorCliente = 'El nombre del cliente es obligatorio.';
      return;
    }

    this.guardandoCliente = true;
    this.clienteService
      .editarPorIdentificacion(
        this.clienteSeleccionado.tipoIdentificacion,
        this.clienteSeleccionado.numeroIdentificacion,
        {
          nombre,
          email: this.clienteForm.email.trim() || null,
        }
      )
      .pipe(
        finalize(() => {
          this.guardandoCliente = false;
          this.refrescarVista();
        })
      )
      .subscribe({
        next: (clienteActualizado) => {
          this.clienteSeleccionado = clienteActualizado;
          this.clienteForm = {
            nombre: clienteActualizado.nombre,
            email: clienteActualizado.email ?? '',
          };
          this.editandoCliente = false;
          this.exitoCliente = 'Cliente actualizado correctamente.';
          this.refrescarVista();
        },
        error: (error: unknown) => {
          this.errorCliente = obtenerMensajeError(
            error,
            'No se pudo actualizar el cliente. Verifica los datos e intenta nuevamente.'
          );
          this.refrescarVista();
        },
      });
  }

  confirmarCobroSinFactura(): void {
    if (this.solicitaFactura !== false) {
      this.errorCobro = 'Selecciona si el cliente solicita factura antes de cobrar.';
      return;
    }

    this.ejecutarCobro('FEV_CONS_FINAL', null);
  }

  generarFacturaYCobrar(): void {
    if (this.solicitaFactura !== true) {
      this.errorCobro = 'Este flujo requiere factura a nombre del cliente.';
      return;
    }

    const cliente = this.construirClienteFacturacion();
    if (!cliente) return;

    this.ejecutarCobro('FEV', cliente);
  }

  get cambioAbsoluto(): number {
    return Math.abs(this.cambio);
  }

  get cambio(): number {
    if (this.metodoPagoSeleccionado !== 'EFECTIVO') return 0;
    return this.montoRecibido - (this.pedido?.total ?? 0);
  }

  get puedeConfirmarCobro(): boolean {
    if (this.metodoPagoSeleccionado !== 'EFECTIVO') {
      return true;
    }

    return this.montoRecibido >= (this.pedido?.total ?? 0);
  }

  get puedeBuscarCliente(): boolean {
    return !this.buscandoCliente && !this.guardandoCobro && !!this.busquedaCliente.numeroIdentificacion.trim();
  }

  get estaProcesando(): boolean {
    return this.buscandoCliente || this.guardandoCliente || this.guardandoCobro;
  }

  get tituloModalCliente(): string {
    switch (this.pasoCobro) {
      case 'BUSCAR_CLIENTE':
        return 'Buscar cliente';
      case 'CONFIRMAR_CLIENTE':
        return 'Confirmar cliente';
      case 'CREAR_CLIENTE':
        return 'Crear cliente';
      default:
        return '';
    }
  }

  formatearMonto(monto: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
    }).format(monto);
  }

  private construirClienteFacturacion(): Cliente | null {
    this.errorCliente = null;
    this.errorCobro = null;

    const tipoIdentificacion = this.busquedaCliente.tipoIdentificacion;
    const numeroIdentificacion = this.busquedaCliente.numeroIdentificacion.trim();
    const nombre = this.clienteForm.nombre.trim();
    const email = this.clienteForm.email.trim() || null;

    if (!tipoIdentificacion || !numeroIdentificacion) {
      this.errorCliente = 'Tipo y numero de identificacion son obligatorios.';
      return null;
    }

    if (this.pasoCobro === 'CONFIRMAR_CLIENTE') {
      if (!this.clienteSeleccionado) {
        this.errorCliente = 'No hay cliente confirmado para facturar.';
        return null;
      }

      if (this.editandoCliente) {
        this.errorCliente = 'Guarda o cancela la edicion del cliente antes de cobrar.';
        return null;
      }

      return {
        tipoIdentificacion: this.clienteSeleccionado.tipoIdentificacion,
        numeroIdentificacion: this.clienteSeleccionado.numeroIdentificacion,
        nombre: this.clienteSeleccionado.nombre,
        email: this.clienteSeleccionado.email ?? null,
      };
    }

    if (!nombre) {
      this.errorCliente = 'El nombre del cliente es obligatorio.';
      return null;
    }

    return {
      tipoIdentificacion,
      numeroIdentificacion,
      nombre,
      email,
    };
  }

  private ejecutarCobro(tipoDocumentoFiscal: TipoDocumentoFiscal, cliente: Cliente | null): void {
    this.errorCobro = null;

    if (!this.pedidoId) {
      this.errorCobro = 'No se encontro el pedido a cobrar.';
      return;
    }

    if (!this.usuarioId) {
      this.errorCobro = 'No se encontro un usuario autenticado para registrar el cobro.';
      return;
    }

    if (!this.puedeConfirmarCobro) {
      this.errorCobro = 'El monto recibido no cubre el total del pedido.';
      return;
    }

    this.tipoDocumentoFiscal = tipoDocumentoFiscal;

    const request: PedidoUsuarioRequest = {
      usuarioId: this.usuarioId,
      metodoPago: this.metodoPagoSeleccionado,
      tipoDocumentoFiscal,
    };

    if (cliente) {
      request.cliente = cliente;
      this.clienteSeleccionado = cliente;
    }

    this.guardandoCobro = true;
    this.pedidosService
      .cobrarPedido(this.pedidoId, request)
      .pipe(
        finalize(() => {
          this.guardandoCobro = false;
          this.refrescarVista();
        })
      )
      .subscribe({
        next: () => {
          this.cobroRealizado.emit();
          this.limpiarEstadoCobro();
          this.refrescarVista();
        },
        error: (error: unknown) => {
          this.errorCobro = obtenerMensajeError(error, 'No se pudo cobrar el pedido. Intenta nuevamente.');
          this.refrescarVista();
        },
      });
  }

  private inicializarFlujoCobro(): void {
    this.pedidoId = this.pedido?.id ?? null;
    this.usuarioId = this.authService.getUsuario()?.id ?? null;
    this.metodoPagoSeleccionado = 'EFECTIVO';
    this.montoRecibido = 0;
    this.solicitaFactura = null;
    this.tipoDocumentoFiscal = null;
    this.pasoCobro = 'PREGUNTA_FACTURA';
    this.reiniciarEstadoCliente();
    this.errorCobro = null;
    this.guardandoCobro = false;
  }

  private reiniciarEstadoCliente(): void {
    this.busquedaCliente = this.crearFormularioBusqueda();
    this.clienteSeleccionado = null;
    this.clienteForm = this.crearFormularioCliente();
    this.editandoCliente = false;
    this.buscandoCliente = false;
    this.guardandoCliente = false;
    this.errorBusquedaCliente = null;
    this.mensajeNoEncontrado = null;
    this.errorCliente = null;
    this.exitoCliente = null;
  }

  private limpiarEstadoCobro(): void {
    this.pedidoId = null;
    this.usuarioId = null;
    this.metodoPagoSeleccionado = 'EFECTIVO';
    this.montoRecibido = 0;
    this.solicitaFactura = null;
    this.tipoDocumentoFiscal = null;
    this.pasoCobro = 'PREGUNTA_FACTURA';
    this.reiniciarEstadoCliente();
    this.errorCobro = null;
    this.guardandoCobro = false;
  }

  private crearFormularioBusqueda(): BusquedaClienteFormState {
    return {
      tipoIdentificacion: 'CC',
      numeroIdentificacion: '',
    };
  }

  private crearFormularioCliente(): ClienteFormState {
    return {
      nombre: '',
      email: '',
    };
  }

  private abrirFlujoCrearCliente(numeroIdentificacion: string): void {
    this.clienteSeleccionado = null;
    this.busquedaCliente.numeroIdentificacion = numeroIdentificacion;
    this.clienteForm = this.crearFormularioCliente();
    this.editandoCliente = false;
    this.mensajeNoEncontrado = 'No se encontro el cliente. Completa los datos para facturar.';
    this.pasoCobro = 'CREAR_CLIENTE';
  }

  private esRespuesta404(error: unknown): boolean {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 404) {
        return true;
      }

      const payloadStatus = error.error?.status;
      if (payloadStatus === 404 || payloadStatus === '404') {
        return true;
      }
    }

    const errorObject = error as { status?: unknown; error?: { status?: unknown } } | null;
    if (errorObject?.status === 404 || errorObject?.status === '404') {
      return true;
    }

    const payloadStatus = errorObject?.error?.status;
    if (payloadStatus === 404 || payloadStatus === '404') {
      return true;
    }

    return false;
  }

  private refrescarVista(): void {
    const viewRef = this.cdr as ViewRef;
    if (!viewRef.destroyed) {
      this.cdr.detectChanges();
    }
  }
}
