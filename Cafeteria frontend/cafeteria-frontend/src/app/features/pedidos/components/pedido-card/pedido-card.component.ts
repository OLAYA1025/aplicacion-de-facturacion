import { CommonModule } from '@angular/common';
import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
} from '@angular/core';
import { Pedido } from '../../../../core/models/pedido.model';

@Component({
  selector: 'app-pedido-card',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './pedido-card.component.html',
})
export class PedidoCardComponent implements OnInit, OnChanges {
  @Input({ required: true }) pedido!: Pedido;
  @Input() imprimiendoPrefactura = false;

  @Output() editar = new EventEmitter<Pedido>();
  @Output() cobrar = new EventEmitter<Pedido>();
  @Output() cancelar = new EventEmitter<Pedido>();
  @Output() imprimirPrefactura = new EventEmitter<Pedido>();
  esPendiente = false;
  esCancelado = false;

  ngOnInit(): void {
    this.actualizarEstado();
  }

  ngOnChanges(_: SimpleChanges): void {
    this.actualizarEstado();
  }

  private actualizarEstado(): void {
    this.esPendiente = this.pedido.estado === 'PENDIENTE';
    this.esCancelado = this.pedido.estado === 'CANCELADO';
  }

  onEditar(): void {
    this.editar.emit(this.pedido);
  }

  onCobrar(): void {
    this.cobrar.emit(this.pedido);
  }

  onCancelar(): void {
    this.cancelar.emit(this.pedido);
  }

  onImprimirPrefactura(): void {
    this.imprimirPrefactura.emit(this.pedido);
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
