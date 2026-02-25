import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Pedido } from '../../../../core/models/pedido.model';

type MetodoPago = 'EFECTIVO' | 'TARJETA' | 'TRANSFERENCIA';

@Component({
  selector: 'app-modal-cobro',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './modal-cobro.component.html',
})
export class ModalCobroComponent {
  @Input() visible: boolean = false;
  @Input() pedido: Pedido | null = null;
  @Input() guardando: boolean = false;
  @Input() metodoPagoSeleccionado: MetodoPago = 'EFECTIVO';
  @Input() montoRecibido: number = 0;
  @Input() cambio: number = 0;
  @Input() puedeConfirmarCobro: boolean = false;

  @Output() cerrar = new EventEmitter<void>();
  @Output() confirmar = new EventEmitter<void>();
  @Output() metodoPagoChange = new EventEmitter<MetodoPago>();
  @Output() montoRecibidoChange = new EventEmitter<number>();

  onMetodoPagoChange(metodoPago: MetodoPago): void {
    this.metodoPagoChange.emit(metodoPago);
  }

  onMontoRecibidoChange(valor: string | number): void {
    this.montoRecibidoChange.emit(Number(valor) || 0);
  }

  get cambioAbsoluto(): number {
    return Math.abs(this.cambio);
  }

  formatearMonto(monto: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
    }).format(monto);
  }
}
