import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Producto } from '../../../../core/models/producto.model';
import { DetalleTemp } from '../../models/detalleTemp.model';

interface CategoriaProducto {
  id: number;
  nombre: string;
}

@Component({
  selector: 'app-modal-productos',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './modal-productos.component.html',
})
export class ModalProductosComponent {
  @Input() visible: boolean = false;
  @Input() busquedaProducto: string = '';
  @Input() categorias: CategoriaProducto[] = [];
  @Input() categoriaSeleccionada: number | null = null;
  @Input() productos: Producto[] = [];
  @Input() productosFiltrados: Producto[] = [];
  @Input() detallesSeleccionados: DetalleTemp[] = [];

  @Output() cerrar = new EventEmitter<void>();
  @Output() busquedaChange = new EventEmitter<string>();
  @Output() categoriaChange = new EventEmitter<number | null>();
  @Output() agregarProducto = new EventEmitter<Producto>();
  @Output() cantidadProductoChange = new EventEmitter<{
    producto: Producto;
    cantidad: number;
  }>();

  onBusquedaChange(value: string): void {
    this.busquedaChange.emit(value);
  }

  onSeleccionarCategoria(categoriaId: number | null): void {
    this.categoriaChange.emit(categoriaId);
  }

  onAgregarProducto(producto: Producto): void {
    this.agregarProducto.emit(producto);
  }

  onCantidadProductoChange(producto: Producto, value: string | number): void {
    const cantidad = Number(value);
    if (Number.isNaN(cantidad)) return;

    this.cantidadProductoChange.emit({
      producto,
      cantidad: Math.max(0, Math.floor(cantidad)),
    });
  }

  obtenerIdProducto(producto: Producto): number {
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

  esInventariable(producto: Producto): boolean {
    return producto.tipoGestion === 'INVENTARIABLE';
  }

  stockDisponible(producto: Producto): number {
    const stockActual = typeof producto.stockActual === 'number' ? producto.stockActual : 0;
    const stockReservado = typeof producto.stockReservado === 'number' ? producto.stockReservado : 0;
    return stockActual - stockReservado;
  }

  obtenerCantidadSeleccionada(productoId: number): number {
    return (
      this.detallesSeleccionados.find((d) => this.obtenerIdProducto(d.producto) === productoId)
        ?.cantidad || 0
    );
  }

  esProductoSeleccionado(productoId: number): boolean {
    return this.obtenerCantidadSeleccionada(productoId) > 0;
  }

  contarProductosPorCategoria(categoriaId: number): number {
    if (!this.productos.length) return 0;
    return this.productos.filter((p) => p.categoria.id === categoriaId).length;
  }

  formatearMonto(monto: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
    }).format(monto);
  }
}
