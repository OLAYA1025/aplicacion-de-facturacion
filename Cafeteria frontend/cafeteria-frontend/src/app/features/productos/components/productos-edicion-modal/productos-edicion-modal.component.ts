import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { Categoria } from '../../models/categoria.model';
import { ProductoEditarFormState } from '../../models/productos-ui.model';

@Component({
  selector: 'app-productos-edicion-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './productos-edicion-modal.component.html',
})
export class ProductosEdicionModalComponent {
  @Input() abierto: boolean = false;
  @Input({ required: true }) edicionProducto!: ProductoEditarFormState;
  @Input() categorias: Categoria[] = [];
  @Input() guardandoEdicion: boolean = false;
  @Input() errorEditarProducto: string | null = null;

  @Output() cerrar = new EventEmitter<void>();
  @Output() guardar = new EventEmitter<NgForm>();
  @Output() tipoGestionChange = new EventEmitter<void>();

  get textoBotonGuardar(): string {
    return this.guardandoEdicion ? 'Guardando...' : 'Guardar cambios';
  }

  onCerrar(): void {
    this.cerrar.emit();
  }

  onGuardar(form: NgForm): void {
    this.guardar.emit(form);
  }

  onTipoGestionChange(): void {
    this.tipoGestionChange.emit();
  }
}
