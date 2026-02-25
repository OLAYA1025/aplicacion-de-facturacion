import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Pedido } from '../../../../core/models/pedido.model';

@Component({
  selector: 'app-modal-decision',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './modal-decision.component.html',
})
export class ModalDecisionComponent {
  @Input() visible: boolean = false;
  @Input() pedido: Pedido | null = null;

  @Output() cobrarAhora = new EventEmitter<void>();
  @Output() dejarPendiente = new EventEmitter<void>();

  formatearMonto(monto: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
    }).format(monto);
  }
}
