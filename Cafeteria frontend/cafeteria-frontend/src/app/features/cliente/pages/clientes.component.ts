import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, inject } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { finalize } from 'rxjs';
import { Cliente } from 'src/app/core/models/cliente.model';
import { obtenerMensajeError } from 'src/app/shared/utils/error-message.util';
import { ActualizarClienteRequest } from '../models/actualizarCliente.model';
import { ClienteService } from '../services/cliente.service';

type TipoIdentificacionCliente = Cliente['tipoIdentificacion'];

interface BusquedaClienteFormState {
  tipoIdentificacion: TipoIdentificacionCliente;
  numeroIdentificacion: string;
}

interface EdicionClienteFormState {
  nombre: string;
  email: string;
}

@Component({
  selector: 'app-clientes',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './clientes.component.html',
})
export class ClientesComponent {
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly clienteService = inject(ClienteService);

  readonly tiposDocumento: TipoIdentificacionCliente[] = ['NIT', 'CC', 'CE', 'PAS'];

  buscandoCliente = false;
  guardandoCliente = false;

  errorBusqueda: string | null = null;
  errorEdicion: string | null = null;
  exitoEdicion: string | null = null;

  clienteEncontrado: Cliente | null = null;

  busqueda: BusquedaClienteFormState = this.crearFormularioBusqueda();
  edicion: EdicionClienteFormState = this.crearFormularioEdicion();

  buscarCliente(form: NgForm): void {
    this.errorBusqueda = null;
    this.errorEdicion = null;
    this.exitoEdicion = null;

    if (form.invalid) {
      this.errorBusqueda = 'Ingresa un tipo y numero de identificacion validos.';
      return;
    }

    const numeroIdentificacion = this.busqueda.numeroIdentificacion.trim();
    if (!numeroIdentificacion) {
      this.errorBusqueda = 'El numero de identificacion es obligatorio.';
      return;
    }

    this.buscandoCliente = true;
    this.clienteService
      .obtenerPorIdentificacion(this.busqueda.tipoIdentificacion, numeroIdentificacion)
      .pipe(
        finalize(() => {
          this.buscandoCliente = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (cliente) => {
          this.clienteEncontrado = cliente;
          this.busqueda.numeroIdentificacion = numeroIdentificacion;
          this.edicion = {
            nombre: cliente.nombre,
            email: cliente.email ?? '',
          };
          this.cdr.detectChanges();
        },
        error: (error) => {
          this.clienteEncontrado = null;
          this.edicion = this.crearFormularioEdicion();
          this.errorBusqueda = obtenerMensajeError(
            error,
            'No se pudo encontrar el cliente. Verifica los datos e intenta nuevamente.'
          );
          this.cdr.detectChanges();
        },
      });
  }

  guardarCambios(form: NgForm): void {
    this.errorEdicion = null;
    this.exitoEdicion = null;

    if (!this.clienteEncontrado) {
      this.errorEdicion = 'Primero debes buscar un cliente.';
      return;
    }

    if (form.invalid) {
      this.errorEdicion = 'Completa los campos requeridos antes de guardar.';
      return;
    }

    const nombre = this.edicion.nombre.trim();
    if (!nombre) {
      this.errorEdicion = 'El nombre del cliente es obligatorio.';
      return;
    }

    const request: ActualizarClienteRequest = {
      nombre,
      email: this.edicion.email.trim() || null,
    };

    this.guardandoCliente = true;
    this.clienteService
      .editarPorIdentificacion(
        this.clienteEncontrado.tipoIdentificacion,
        this.clienteEncontrado.numeroIdentificacion,
        request
      )
      .pipe(
        finalize(() => {
          this.guardandoCliente = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (clienteActualizado) => {
          this.clienteEncontrado = clienteActualizado;
          this.edicion = {
            nombre: clienteActualizado.nombre,
            email: clienteActualizado.email ?? '',
          };
          this.exitoEdicion = 'Cliente actualizado correctamente.';
          this.cdr.detectChanges();
        },
        error: (error) => {
          this.errorEdicion = obtenerMensajeError(
            error,
            'No se pudo actualizar el cliente. Verifica los datos e intenta nuevamente.'
          );
          this.cdr.detectChanges();
        },
      });
  }

  limpiarBusqueda(form: NgForm): void {
    this.errorBusqueda = null;
    this.errorEdicion = null;
    this.exitoEdicion = null;
    this.clienteEncontrado = null;
    this.busqueda = this.crearFormularioBusqueda();
    this.edicion = this.crearFormularioEdicion();
    form.resetForm(this.busqueda);
  }

  private crearFormularioBusqueda(): BusquedaClienteFormState {
    return {
      tipoIdentificacion: 'CC',
      numeroIdentificacion: '',
    };
  }

  private crearFormularioEdicion(): EdicionClienteFormState {
    return {
      nombre: '',
      email: '',
    };
  }
}
