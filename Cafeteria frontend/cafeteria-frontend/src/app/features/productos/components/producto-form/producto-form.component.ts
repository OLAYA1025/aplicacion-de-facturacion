import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Categoria } from '../../models/categoria.model';
import {
  ProductoCrearFormState,
  SelectorCategoriaCrear,
} from '../../models/productos-ui.model';

@Component({
  selector: 'app-producto-form',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './producto-form.component.html',
})
export class ProductoFormComponent {
  @Input({ required: true }) formIdPrefix!: string;
  @Input({ required: true }) nuevoProducto!: ProductoCrearFormState;
  @Input() selectorCategoriaCrear: SelectorCategoriaCrear = null;
  @Input() categorias: Categoria[] = [];
  @Input() mostrarCrearCategoria: boolean = false;
  @Input() nuevaCategoriaNombre: string = '';
  @Input() guardandoCategoria: boolean = false;
  @Input() guardandoProducto: boolean = false;
  @Input() errorCrearCategoria: string | null = null;
  @Input() errorCrearProducto: string | null = null;
  @Input() mostrarBotonCancelar: boolean = false;

  @Output() submitForm = new EventEmitter<NgForm>();
  @Output() categoriaChange = new EventEmitter<SelectorCategoriaCrear>();
  @Output() tipoGestionChange = new EventEmitter<void>();
  @Output() nuevaCategoriaNombreChange = new EventEmitter<string>();
  @Output() crearCategoria = new EventEmitter<void>();
  @Output() cancelarCrearCategoria = new EventEmitter<void>();
  @Output() cancelarFormulario = new EventEmitter<void>();

  get textoBotonCrearCategoria(): string {
    return this.guardandoCategoria ? 'Guardando...' : 'Crear';
  }

  get textoBotonSubmit(): string {
    return this.guardandoProducto ? 'Guardando producto...' : 'Crear producto';
  }

  onSubmit(form: NgForm): void {
    this.submitForm.emit(form);
  }

  onCategoriaChange(value: SelectorCategoriaCrear): void {
    this.categoriaChange.emit(value);
  }

  onTipoGestionChange(): void {
    this.tipoGestionChange.emit();
  }

  onNuevaCategoriaNombreChange(value: string): void {
    this.nuevaCategoriaNombreChange.emit(value);
  }

  onCrearCategoria(): void {
    this.crearCategoria.emit();
  }

  onCancelarCrearCategoria(): void {
    this.cancelarCrearCategoria.emit();
  }

  onCancelarFormulario(): void {
    this.cancelarFormulario.emit();
  }
}
