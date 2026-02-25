import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { DetalleTemp } from '../../models/detalleTemp.model';

@Component({
  selector: 'app-resumen-pedido',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './resumen-pedido.component.html',
})
export class ResumenPedidoComponent {
  @Input() detallesTemp: DetalleTemp[] = [];
  @Input() totalPedido: number = 0;
  @Input() nombreCliente: string = '';
  @Input() vistaActual: 'crear' | 'editar' = 'crear';
  @Input() guardando: boolean = false;

  @Output() eliminarDetalle = new EventEmitter<number>();
  @Output() aumentarCantidad = new EventEmitter<number>();
  @Output() disminuirCantidad = new EventEmitter<number>();
  @Output() guardar = new EventEmitter<void>();
  @Output() cancelar = new EventEmitter<void>();

  formatearMonto(monto: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
    }).format(monto);
  }
}
