import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { ModalMensajeService } from '../../services/modal-mensaje.service';

@Component({
  selector: 'app-modal-mensaje-global',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './modal-mensaje-global.component.html',
})
export class ModalMensajeGlobalComponent {
  private readonly modalMensajeService = inject(ModalMensajeService);
  readonly estado = this.modalMensajeService.estado;

  get esError(): boolean {
    return this.estado().tipo === 'error';
  }

  onFondoClick(): void {
    this.modalMensajeService.cerrarPorFondo();
  }

  onConfirmar(): void {
    this.modalMensajeService.confirmarAccion();
  }

  onCancelar(): void {
    this.modalMensajeService.cancelar();
  }
}
