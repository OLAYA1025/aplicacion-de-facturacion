import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Producto } from '../../../../core/models/producto.model';

@Component({
  selector: 'app-productos-listado',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './productos-listado.component.html',
})
export class ProductosListadoComponent {
  @Input() loadingProductos: boolean = false;
  @Input() productosFiltrados: Producto[] = [];
  @Input() esInventariable: boolean = false;
  @Input() guardandoEstadoIds: ReadonlySet<number> = new Set<number>();

  @Output() editar = new EventEmitter<Producto>();
  @Output() cambiarEstado = new EventEmitter<Producto>();

  onEditar(producto: Producto): void {
    this.editar.emit(producto);
  }

  onCambiarEstado(producto: Producto): void {
    this.cambiarEstado.emit(producto);
  }

  stockDisponible(producto: Producto): number {
    return producto.stockActual - producto.stockReservado;
  }

  estaProcesandoEstado(productoId: number): boolean {
    return this.guardandoEstadoIds.has(productoId);
  }

  formatearMoneda(valor: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
    }).format(valor);
  }
}
