import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DetalleTemp } from '../../models/detalleTemp.model';
import { ResumenPedidoComponent } from '../resumen-pedido/resumen-pedido.component';

@Component({
  selector: 'app-formulario-pedido',
  standalone: true,
  imports: [CommonModule, FormsModule, ResumenPedidoComponent],
  templateUrl: './formulario-pedido.component.html',
})
export class FormularioPedidoComponent {
  @Input() vistaActual: 'crear' | 'editar' = 'crear';
  @Input() pedidoEditandoId: number | null = null;
  @Input() nombreCliente: string = '';
  @Input() detallesTemp: DetalleTemp[] = [];
  @Input() totalPedido: number = 0;
  @Input() guardando: boolean = false;

  @Output() volver = new EventEmitter<void>();
  @Output() nombreClienteChange = new EventEmitter<string>();
  @Output() abrirModalProductos = new EventEmitter<void>();
  @Output() eliminarDetalle = new EventEmitter<number>();
  @Output() aumentarCantidad = new EventEmitter<number>();
  @Output() disminuirCantidad = new EventEmitter<number>();
  @Output() guardarPedido = new EventEmitter<void>();
  @Output() cancelar = new EventEmitter<void>();

  onNombreClienteChange(nombre: string): void {
    this.nombreClienteChange.emit(nombre);
  }
}
