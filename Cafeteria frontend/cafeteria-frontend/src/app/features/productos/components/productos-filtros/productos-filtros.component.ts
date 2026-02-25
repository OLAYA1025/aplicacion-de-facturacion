import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Categoria } from '../../models/categoria.model';
import {
  CampoOrden,
  DireccionOrden,
  EstadoFiltro,
} from '../../models/productos-ui.model';

@Component({
  selector: 'app-productos-filtros',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './productos-filtros.component.html',
})
export class ProductosFiltrosComponent {
  @Input() busqueda: string = '';
  @Input() filtroCategoriaId: number | null = null;
  @Input() filtroEstado: EstadoFiltro = 'TODOS';
  @Input() campoOrden: CampoOrden = 'nombre';
  @Input() direccionOrden: DireccionOrden = 'asc';
  @Input() esInventariable: boolean = false;
  @Input() categorias: Categoria[] = [];

  @Output() busquedaChange = new EventEmitter<string>();
  @Output() filtroCategoriaIdChange = new EventEmitter<number | null>();
  @Output() filtroEstadoChange = new EventEmitter<EstadoFiltro>();
  @Output() campoOrdenChange = new EventEmitter<CampoOrden>();
  @Output() direccionOrdenChange = new EventEmitter<DireccionOrden>();
  @Output() filtrosChange = new EventEmitter<void>();

  onBusquedaChange(value: string): void {
    this.busquedaChange.emit(value);
    this.filtrosChange.emit();
  }

  onCategoriaChange(value: number | null): void {
    this.filtroCategoriaIdChange.emit(value);
    this.filtrosChange.emit();
  }

  onEstadoChange(value: EstadoFiltro): void {
    this.filtroEstadoChange.emit(value);
    this.filtrosChange.emit();
  }

  onCampoOrdenChange(value: CampoOrden): void {
    this.campoOrdenChange.emit(value);
    this.filtrosChange.emit();
  }

  onDireccionOrdenChange(value: DireccionOrden): void {
    this.direccionOrdenChange.emit(value);
    this.filtrosChange.emit();
  }
}
