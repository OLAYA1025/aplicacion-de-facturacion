import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { finalize } from 'rxjs';
import { UsuarioResponse } from '../../../core/models/usuario.model';
import { CrearUsuarioRequest } from '../../../core/models/crearUsuarioRequest.model';
import { CambiarPasswordUsuarioRequest } from '../../../core/models/cambiarPasswordUsuarioRequest.model';
import { UsuarioService } from '../../../core/services/usuario.service';
import { ModalMensajeService } from '../../../shared/services/modal-mensaje.service';
import { obtenerMensajeError } from '../../../shared/utils/error-message.util';

type FiltroEstadoUsuario = 'TODOS' | 'ACTIVOS' | 'DESACTIVADOS';
type RolUsuario = 'ADMINISTRADOR' | 'CAJERO' | 'MESERO' | 'COCINERO';

interface NuevoUsuarioFormState {
  nombre: string;
  username: string;
  email: string;
  rol: RolUsuario;
  password: string;
  confirmarPassword: string;
}

interface CambiarPasswordFormState {
  nuevaPassword: string;
  confirmarPassword: string;
}

@Component({
  selector: 'app-usuarios',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './usuarios.component.html',
})
export class UsuariosComponent implements OnInit {
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly usuarioService = inject(UsuarioService);
  private readonly modalMensajeService = inject(ModalMensajeService);

  loadingUsuarios: boolean = false;
  guardandoUsuario: boolean = false;
  guardandoPassword: boolean = false;
  errorListado: string | null = null;
  errorCrearUsuario: string | null = null;
  errorCambiarPassword: string | null = null;

  usuarios: UsuarioResponse[] = [];
  usuariosFiltrados: UsuarioResponse[] = [];
  procesandoEstadoIds: Set<number> = new Set();

  busqueda: string = '';
  filtroEstado: FiltroEstadoUsuario = 'TODOS';
  readonly roles: RolUsuario[] = ['ADMINISTRADOR', 'CAJERO', 'MESERO', 'COCINERO'];

  modalCrearUsuarioAbierto: boolean = false;
  modalCambiarPasswordAbierto: boolean = false;
  usuarioPassword: UsuarioResponse | null = null;

  nuevoUsuario: NuevoUsuarioFormState = this.crearFormularioInicial();
  cambioPassword: CambiarPasswordFormState = this.crearFormularioPasswordInicial();

  ngOnInit(): void {
    this.cargarUsuarios();
  }

  cargarUsuarios(): void {
    this.loadingUsuarios = true;
    this.errorListado = null;

    this.usuarioService
      .listarUsuarios()
      .pipe(
        finalize(() => {
          this.loadingUsuarios = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (usuarios) => {
          this.usuarios = usuarios;
          this.aplicarFiltros();
          this.cdr.detectChanges();
      
        },
        error: (error) => {
          this.mostrarError(
            'No se pudieron cargar los usuarios',
            'Verifica la conexión e intenta nuevamente.',
            error
          );
          this.usuarios = [];
          this.usuariosFiltrados = [];
          this.errorListado =
            'No se pudo obtener la lista de usuarios. Verifica la conexión e intenta de nuevo.';
          this.cdr.detectChanges();
        },
        
        
        
      });
      
  }

  aplicarFiltros(): void {
    const termino = this.busqueda.trim().toLowerCase();
    let resultado = [...this.usuarios];

    if (termino) {
      resultado = resultado.filter((usuario) => {
        return (
          usuario.nombre.toLowerCase().includes(termino) ||
          usuario.username.toLowerCase().includes(termino)
        );
      });
    }

    if (this.filtroEstado === 'ACTIVOS') {
      resultado = resultado.filter((usuario) => usuario.activo);
    }

    if (this.filtroEstado === 'DESACTIVADOS') {
      resultado = resultado.filter((usuario) => !usuario.activo);
    }

    resultado.sort((a, b) => a.nombre.localeCompare(b.nombre, 'es', { sensitivity: 'base' }));
    this.usuariosFiltrados = resultado;
  }

  onBusquedaChange(value: string): void {
    this.busqueda = value;
    this.aplicarFiltros();
  }

  onFiltroEstadoChange(value: FiltroEstadoUsuario): void {
    this.filtroEstado = value;
    this.aplicarFiltros();
  }

  abrirModalCrearUsuario(): void {
    this.nuevoUsuario = this.crearFormularioInicial();
    this.errorCrearUsuario = null;
    this.modalCrearUsuarioAbierto = true;
  }

  cerrarModalCrearUsuario(): void {
    this.modalCrearUsuarioAbierto = false;
    this.nuevoUsuario = this.crearFormularioInicial();
    this.errorCrearUsuario = null;
    this.cdr.detectChanges();
  }

  crearUsuario(form: NgForm): void {
    this.errorCrearUsuario = null;

    if (form.invalid) {
      this.errorCrearUsuario = 'Completa los campos obligatorios para crear el usuario.';
      return;
    }

    const nombre = this.nuevoUsuario.nombre.trim();
    const username = this.nuevoUsuario.username.trim();
    const password = this.nuevoUsuario.password.trim();
    const confirmarPassword = this.nuevoUsuario.confirmarPassword.trim();

    if (nombre.length < 2) {
      this.errorCrearUsuario = 'El nombre debe tener mínimo 2 caracteres.';
      return;
    }

    if (username.length < 2) {
      this.errorCrearUsuario = 'El username debe tener mínimo 2 caracteres.';
      return;
    }

    if (!password) {
      this.errorCrearUsuario = 'La contraseña es obligatoria.';
      return;
    }

    if (!confirmarPassword) {
      this.errorCrearUsuario = 'Debes confirmar la contraseña.';
      return;
    }

    if (password !== confirmarPassword) {
      this.errorCrearUsuario = 'Las contraseñas no coinciden.';
      return;
    }

    const request: CrearUsuarioRequest = {
      nombre,
      username,
      password,
      email: this.nuevoUsuario.email.trim() || null,
      rol: this.nuevoUsuario.rol,
    };

    this.guardandoUsuario = true;
    this.usuarioService
      .crearUsuario(request)
      .pipe(
        finalize(() => {
          this.guardandoUsuario = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          this.cerrarModalCrearUsuario();
          this.cargarUsuarios();
        },
        error: (error) => {
          this.mostrarError(
            'No se pudo crear el usuario',
            'Verifica los datos e intenta de nuevo.',
            error
          );
          this.errorCrearUsuario = 'No se pudo crear el usuario. Verifica los datos e intenta nuevamente.';
          this.cdr.detectChanges();
        },
      });
  }

  abrirModalCambiarPassword(usuario: UsuarioResponse): void {
    this.usuarioPassword = usuario;
    this.cambioPassword = this.crearFormularioPasswordInicial();
    this.errorCambiarPassword = null;
    this.modalCambiarPasswordAbierto = true;
  }

  cerrarModalCambiarPassword(): void {
    this.modalCambiarPasswordAbierto = false;
    this.usuarioPassword = null;
    this.cambioPassword = this.crearFormularioPasswordInicial();
    this.errorCambiarPassword = null;
    this.cdr.detectChanges();
  }

  cambiarPassword(form: NgForm): void {
    if (!this.usuarioPassword) {
      return;
    }

    this.errorCambiarPassword = null;

    if (form.invalid) {
      this.errorCambiarPassword = 'Completa los campos obligatorios para cambiar la contraseña.';
      return;
    }

    if (!this.cambioPassword.nuevaPassword.trim()) {
      this.errorCambiarPassword = 'La nueva contraseña es obligatoria.';
      return;
    }

    if (this.cambioPassword.nuevaPassword !== this.cambioPassword.confirmarPassword) {
      this.errorCambiarPassword = 'Las contraseñas no coinciden.';
      return;
    }

    const request: CambiarPasswordUsuarioRequest = {
      nuevaPassword: this.cambioPassword.nuevaPassword.trim(),
    };

    this.guardandoPassword = true;
    this.usuarioService
      .cambiarPassword(this.usuarioPassword.id, request)
      .pipe(
        finalize(() => {
          this.guardandoPassword = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          this.cerrarModalCambiarPassword();
        },
        error: (error) => {
          this.mostrarError(
            'No se pudo cambiar la contraseña',
            'Verifica los datos e intenta de nuevo.',
            error
          );
          this.errorCambiarPassword =
            'No se pudo cambiar la contraseña. Verifica los datos e intenta nuevamente.';
          this.cdr.detectChanges();
        },
      });
  }

  toggleEstadoUsuario(usuario: UsuarioResponse): void {
     if (usuario.rol === 'ADMINISTRADOR') {
      this.mostrarError(
        'Acción no permitida',
        'Solo los usuarios con rol distinto de administrador pueden cambiar su estado.',
      );
      return;
    } 

    if (this.estaProcesandoEstado(usuario.id)) {
      return;
    }

    this.setProcesandoEstado(usuario.id, true);
    const request$ = usuario.activo
      ? this.usuarioService.desactivarUsuario(usuario.id)
      : this.usuarioService.activarUsuario(usuario.id);

    request$
      .pipe(
        finalize(() => {
          this.setProcesandoEstado(usuario.id, false);
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (actualizado) => {
          this.usuarios = this.usuarios.map((item) =>
            item.id === actualizado.id ? actualizado : item
          );
          this.aplicarFiltros();
          this.cdr.detectChanges();
        },
        error: (error) => {
          this.mostrarError(
            'No se pudo actualizar el estado',
            'Verifica la conexión e intenta nuevamente.',
            error
          );
        },
      });
  }

  private crearFormularioInicial(): NuevoUsuarioFormState {
    return {
      nombre: '',
      username: '',
      email: '',
      rol: 'CAJERO',
      password: '',
      confirmarPassword: '',
    };
  }

  private crearFormularioPasswordInicial(): CambiarPasswordFormState {
    return {
      nuevaPassword: '',
      confirmarPassword: '',
    };
  }

  estaProcesandoEstado(id: number): boolean {
    return this.procesandoEstadoIds.has(id);
  }

  private setProcesandoEstado(id: number, enProceso: boolean): void {
    if (enProceso) {
      this.procesandoEstadoIds.add(id);
    } else {
      this.procesandoEstadoIds.delete(id);
    }
  }

  private mostrarError(titulo: string, mensaje: string, error?: unknown): void {
    const mensajeModal = error !== undefined ? obtenerMensajeError(error, mensaje) : mensaje;

    if (error !== undefined) {
      console.error(titulo, error);
    }

    void this.modalMensajeService.error({
      titulo,
      mensaje: mensajeModal,
      textoConfirmar: 'Entendido',
    });
  }
}
