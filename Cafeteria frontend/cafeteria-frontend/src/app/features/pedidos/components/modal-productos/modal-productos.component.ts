import { CommonModule } from '@angular/common';
import {
  Component,
  ElementRef,
  EventEmitter,
  Input,
  OnChanges,
  Output,
  SimpleChanges,
  ViewChild,
} from '@angular/core';
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
export class ModalProductosComponent implements OnChanges {
  @ViewChild('busquedaInput') busquedaInput?: ElementRef<HTMLInputElement>;

  @Input() visible: boolean = false;
  @Input() busquedaProducto: string = '';
  @Input() categorias: CategoriaProducto[] = [];
  @Input() categoriaSeleccionada: number | null = null;
  @Input() productos: Producto[] = [];
  @Input() productosFiltrados: Producto[] = [];
  @Input() detallesSeleccionados: DetalleTemp[] = [];
  @Input() cantidadesOriginalesEdicion: Record<number, number> = {};

  @Output() cerrar = new EventEmitter<void>();
  @Output() busquedaChange = new EventEmitter<string>();
  @Output() categoriaChange = new EventEmitter<number | null>();
  @Output() agregarProducto = new EventEmitter<Producto>();
  @Output() codigoEscaneado = new EventEmitter<string>();

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['visible']?.currentValue === true) {
      this.enfocarBusqueda();
    }
  }

  onBusquedaChange(value: string): void {
    this.busquedaChange.emit(value);
  }

  onBusquedaEnter(event: Event): void {
    const codigo = this.busquedaProducto.trim();
    if (!codigo) {
      return;
    }

    event.preventDefault();
    this.codigoEscaneado.emit(codigo);
    this.enfocarBusqueda();
  }

  onSeleccionarCategoria(categoriaId: number | null): void {
    this.categoriaChange.emit(categoriaId);
  }

  onAgregarProducto(producto: Producto): void {
    this.agregarProducto.emit(producto);
  }

  obtenerIdProducto(producto: Producto): number {
    const id = (producto as { id?: unknown }).id;
    if (typeof id === 'number' && Number.isFinite(id)) {
      return id;
    }

    const codigo = (producto as { codigo?: unknown }).codigo;
    if (typeof codigo === 'number' && Number.isFinite(codigo)) {
      return codigo;
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

  stockDisponibleParaPedido(producto: Producto): number {
    const productoId = this.obtenerIdProducto(producto);
    if (productoId <= 0) {
      return this.stockDisponible(producto);
    }

    return this.stockDisponible(producto) + (this.cantidadesOriginalesEdicion[productoId] ?? 0);
  }

  obtenerCantidadSeleccionada(productoId: number): number {
    if (productoId <= 0) {
      return 0;
    }
    return this.detallesSeleccionados
      .filter((detalle) => this.obtenerIdProducto(detalle.producto) === productoId)
      .reduce((total, detalle) => total + detalle.cantidad, 0);
  }

  obtenerLineasSeleccionadas(productoId: number): number {
    if (productoId <= 0) {
      return 0;
    }
    return this.detallesSeleccionados
      .filter((detalle) => this.obtenerIdProducto(detalle.producto) === productoId).length;
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

  private enfocarBusqueda(): void {
    setTimeout(() => {
      this.busquedaInput?.nativeElement.focus();
    });
  }
}
