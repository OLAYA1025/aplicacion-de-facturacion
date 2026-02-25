import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Pedido } from '../../../../core/models/pedido.model';

@Component({
  selector: 'app-pedido-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pedido-card.component.html',
})
export class PedidoCardComponent {
  @Input({ required: true }) pedido!: Pedido;

  @Output() editar = new EventEmitter<Pedido>();
  @Output() cobrar = new EventEmitter<Pedido>();
  @Output() cancelar = new EventEmitter<Pedido>();

  onEditar(): void {
    this.editar.emit(this.pedido);
  }

  onCobrar(): void {
    this.cobrar.emit(this.pedido);
  }

  onCancelar(): void {
    this.cancelar.emit(this.pedido);
  }

  formatearMonto(monto: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
    }).format(monto);
  }

  formatearFecha(fecha: string): string {
    const date = new Date(fecha);
    return date.toLocaleDateString('es-ES', {
      day: 'numeric',
      month: 'short',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  }
}
