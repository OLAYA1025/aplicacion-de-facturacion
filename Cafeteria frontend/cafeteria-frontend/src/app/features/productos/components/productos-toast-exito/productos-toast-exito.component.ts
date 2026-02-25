import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-productos-toast-exito',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './productos-toast-exito.component.html',
})
export class ProductosToastExitoComponent {
  @Input() visible: boolean = false;
  @Input() mensaje: string = '';

  @Output() cerrar = new EventEmitter<void>();

  onCerrar(): void {
    this.cerrar.emit();
  }
}
