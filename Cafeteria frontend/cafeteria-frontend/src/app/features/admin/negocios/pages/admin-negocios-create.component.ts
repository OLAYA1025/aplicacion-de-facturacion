import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ModalMensajeService } from '../../../../shared/services/modal-mensaje.service';
import { obtenerMensajeError } from '../../../../shared/utils/error-message.util';
import { CrearNegocioPlataformaRequestDTO } from '../models/crear-negocio-plataforma-request.dto';
import { PlatformNegocioService } from '../services/platform-negocio.service';

@Component({
  selector: 'app-admin-negocios-create',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './admin-negocios-create.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminNegociosCreateComponent {
  private readonly formBuilder = inject(FormBuilder);
  private readonly platformNegocioService = inject(PlatformNegocioService);
  private readonly modalMensajeService = inject(ModalMensajeService);
  private readonly router = inject(Router);

  creating = false;
  backendError: string | null = null;

  readonly form = this.formBuilder.nonNullable.group({
    nombre: ['', [Validators.required, Validators.minLength(2)]],
    nit: ['', [Validators.required, Validators.minLength(3)]],
    direccion: ['', [Validators.required, Validators.minLength(4)]],
    telefono: ['', [Validators.required, Validators.minLength(7)]],
    adminNombre: ['', [Validators.required, Validators.minLength(2)]],
    adminUsername: ['', [Validators.required, Validators.minLength(3)]],
    adminPassword: ['', [Validators.required, Validators.minLength(6)]],
    adminEmail: ['', [Validators.required, Validators.email]],
  });

  crearNegocio(): void {
    this.backendError = null;
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    const payload: CrearNegocioPlataformaRequestDTO = {
      nombre: this.form.controls.nombre.value.trim(),
      nit: this.form.controls.nit.value.trim(),
      direccion: this.form.controls.direccion.value.trim(),
      telefono: this.form.controls.telefono.value.trim(),
      adminNombre: this.form.controls.adminNombre.value.trim(),
      adminUsername: this.form.controls.adminUsername.value.trim(),
      adminPassword: this.form.controls.adminPassword.value,
      adminEmail: this.form.controls.adminEmail.value.trim(),
    };

    this.creating = true;
    this.platformNegocioService
      .crearNegocio(payload)
      .pipe(finalize(() => (this.creating = false)))
      .subscribe({
        next: () => {
          void this.modalMensajeService.abrir({
            tipo: 'confirmacion',
            titulo: 'Negocio creado',
            mensaje: `El negocio "${payload.nombre}" se creó correctamente.`,
            textoConfirmar: 'Entendido',
          });
          void this.router.navigate(['/admin/negocios']);
        },
        error: (error) => {
          this.backendError = obtenerMensajeError(
            error,
            'No se pudo crear el negocio. Verifica los datos e intenta nuevamente.'
          );
          void this.modalMensajeService.error({
            titulo: 'Error al crear negocio',
            mensaje: this.backendError,
          });
        },
      });
  }

  hasError(controlName: keyof typeof this.form.controls): boolean {
    const control = this.form.controls[controlName];
    return control.invalid && (control.dirty || control.touched);
  }
}
