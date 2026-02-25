import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule, NgForm } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ActualizarProveedorRequest } from '../models/actualizarProveedor.model';
import { CrearProveedorRequest } from '../models/crearProveedor.model';
import { Proveedor } from '../models/proveedor.model';
import { TipoDocumento } from '../models/tipoDocumento.model';
import { ProveedorService } from '../services/proveedor.service';
import { ModalMensajeService } from '../../../shared/services/modal-mensaje.service';
import { obtenerMensajeError } from '../../../shared/utils/error-message.util';

type CampoOrdenProveedor = 'id' | 'nombre';
type DireccionOrden = 'asc' | 'desc';
type TabProveedores = 'listado' | 'nuevo';

interface NuevoProveedorFormState {
  id: number | null;
  nombre: string;
  tipoDocumento: TipoDocumento;
  telefono: string;
  correo: string;
}

interface EditarProveedorFormState {
  nombre: string;
  tipoDocumento: TipoDocumento;
  telefono: string;
  correo: string;
}

@Component({
  selector: 'app-proveedores',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './proveedores.component.html',
})
export class ProveedoresComponent implements OnInit {
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);
  private readonly proveedorService = inject(ProveedorService);
  private readonly modalMensajeService = inject(ModalMensajeService);

  tabActual: TabProveedores = 'listado';

  loadingProveedores: boolean = false;
  guardandoProveedor: boolean = false;
  guardandoEdicionProveedor: boolean = false;
  errorListado: string | null = null;
  errorCrearProveedor: string | null = null;
  errorEditarProveedor: string | null = null;
  exitoCrearProveedor: string | null = null;
  modalCrearProveedorAbierto: boolean = false;
  modalEditarProveedorAbierto: boolean = false;
  proveedorEditando: Proveedor | null = null;

  proveedores: Proveedor[] = [];
  proveedoresFiltrados: Proveedor[] = [];

  busqueda: string = '';
  campoOrden: CampoOrdenProveedor = 'nombre';
  direccionOrden: DireccionOrden = 'asc';
  readonly tiposDocumento: TipoDocumento[] = ['NIT', 'CC', 'CE', 'PAS'];
  nuevoProveedor: NuevoProveedorFormState = this.crearFormularioInicial();
  edicionProveedor: EditarProveedorFormState = this.crearFormularioInicialEdicion();

  ngOnInit(): void {
    this.tabActual = this.obtenerTabDesdeRuta();
    if (this.tabActual === 'listado') {
      this.cargarProveedores();
    }
  }

  cargarProveedores(): void {
    this.loadingProveedores = true;
    this.errorListado = null;

    this.proveedorService
      .obtenerProveedores()
      .pipe(
        finalize(() => {
          this.loadingProveedores = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (proveedores) => {
          this.proveedores = proveedores;
          this.aplicarFiltrosYOrden();
          this.cdr.detectChanges();
        },
        error: (error) => {
          this.mostrarError(
            'No se pudo cargar proveedores',
            'Verifica la conexión e intenta de nuevo.',
            error
          );
          this.proveedores = [];
          this.proveedoresFiltrados = [];
          this.errorListado =
            'No se pudo obtener la lista de proveedores. Verifica la conexión e intenta de nuevo.';
          this.cdr.detectChanges();
        },
      });
  }

  aplicarFiltrosYOrden(): void {
    const termino = this.busqueda.trim().toLowerCase();
    let resultado = [...this.proveedores];

    if (termino) {
      resultado = resultado.filter((proveedor) => {
        return (
          proveedor.nombre.toLowerCase().includes(termino) ||
          proveedor.id.toString().includes(termino) ||
          (proveedor.telefono ?? '').toLowerCase().includes(termino) ||
          (proveedor.correo ?? '').toLowerCase().includes(termino)
        );
      });
    }

    const factor = this.direccionOrden === 'asc' ? 1 : -1;
    resultado.sort((a, b) => this.compararProveedores(a, b) * factor);

    this.proveedoresFiltrados = resultado;
  }

  onBusquedaChange(value: string): void {
    this.busqueda = value;
    this.aplicarFiltrosYOrden();
  }

  onCampoOrdenChange(value: CampoOrdenProveedor): void {
    this.campoOrden = value;
    this.aplicarFiltrosYOrden();
  }

  onDireccionOrdenChange(value: DireccionOrden): void {
    this.direccionOrden = value;
    this.aplicarFiltrosYOrden();
  }

  crearProveedor(form: NgForm): void {
    this.errorCrearProveedor = null;
    this.exitoCrearProveedor = null;

    if (form.invalid) {
      this.errorCrearProveedor = 'Completa los campos obligatorios para registrar el proveedor.';
      return;
    }

    const nombre = this.nuevoProveedor.nombre.trim();
    if (nombre.length < 2) {
      this.errorCrearProveedor = 'El nombre del proveedor debe tener al menos 2 caracteres.';
      return;
    }

    const telefono = this.nuevoProveedor.telefono.trim() || null;
    const correo = this.nuevoProveedor.correo.trim() || null;

    this.guardandoProveedor = true;

    const id = this.nuevoProveedor.id;
    if (id === null || !Number.isFinite(id) || id <= 0) {
      this.guardandoProveedor = false;
      this.errorCrearProveedor = 'El ID del proveedor debe ser un numero mayor a 0.';
      this.cdr.detectChanges();
      return;
    }

    const request: CrearProveedorRequest = {
      id,
      nombre,
      telefono,
      correo,
      tipoDocumento: this.nuevoProveedor.tipoDocumento,
    };

    this.proveedorService
      .crearProveedor(request)
      .pipe(
        finalize(() => {
          this.guardandoProveedor = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: (proveedor) => {
          this.exitoCrearProveedor = `Proveedor "${proveedor.nombre}" registrado correctamente.`;
          this.restablecerFormulario(form);
          this.cargarProveedores();

          if (this.modalCrearProveedorAbierto) {
            setTimeout(() => {
              this.cerrarModalCrearProveedor();
            }, 500);
          }

          this.cdr.detectChanges();
        },
        error: (error) => {
          this.mostrarError(
            'No se pudo crear el proveedor',
            'Verifica los datos e intenta de nuevo.',
            error
          );
          this.errorCrearProveedor =
            'No se pudo registrar el proveedor. Verifica los datos e intenta de nuevo.';
          this.cdr.detectChanges();
        },
      });
  }

  abrirModalCrearProveedor(): void {
    this.reiniciarFormularioProveedor();
    this.errorCrearProveedor = null;
    this.exitoCrearProveedor = null;
    this.modalCrearProveedorAbierto = true;
  }

  cerrarModalCrearProveedor(): void {
    this.modalCrearProveedorAbierto = false;
    this.reiniciarFormularioProveedor();
    this.errorCrearProveedor = null;
    this.exitoCrearProveedor = null;
    this.cdr.detectChanges();
  }

  abrirModalEditarProveedor(proveedor: Proveedor): void {
    this.proveedorEditando = proveedor;
    this.errorEditarProveedor = null;
    this.edicionProveedor = {
      nombre: proveedor.nombre,
      tipoDocumento: proveedor.tipoDocumento,
      telefono: proveedor.telefono ?? '',
      correo: proveedor.correo ?? '',
    };
    this.modalEditarProveedorAbierto = true;
  }

  cerrarModalEditarProveedor(): void {
    this.modalEditarProveedorAbierto = false;
    this.proveedorEditando = null;
    this.errorEditarProveedor = null;
    this.edicionProveedor = this.crearFormularioInicialEdicion();
    this.cdr.detectChanges();
  }

  guardarEdicionProveedor(form: NgForm): void {
    if (!this.proveedorEditando) {
      return;
    }

    this.errorEditarProveedor = null;

    if (form.invalid) {
      this.errorEditarProveedor = 'Completa los campos obligatorios para actualizar el proveedor.';
      return;
    }

    const nombre = this.edicionProveedor.nombre.trim();
    if (nombre.length < 2) {
      this.errorEditarProveedor = 'El nombre del proveedor debe tener al menos 2 caracteres.';
      return;
    }

    const request: ActualizarProveedorRequest = {
      nombre,
      tipoDocumento: this.edicionProveedor.tipoDocumento,
      telefono: this.edicionProveedor.telefono.trim() || null,
      correo: this.edicionProveedor.correo.trim() || null,
    };

    this.guardandoEdicionProveedor = true;
    this.proveedorService
      .editarProveedor(this.proveedorEditando.id, request)
      .pipe(
        finalize(() => {
          this.guardandoEdicionProveedor = false;
          this.cdr.detectChanges();
        })
      )
      .subscribe({
        next: () => {
          this.cerrarModalEditarProveedor();
          this.cargarProveedores();
          this.cdr.detectChanges();
        },
        error: (error) => {
          this.mostrarError(
            'No se pudo editar el proveedor',
            'Verifica los datos e intenta de nuevo.',
            error
          );
          this.errorEditarProveedor =
            'No se pudo actualizar el proveedor. Verifica los datos e intenta de nuevo.';
          this.cdr.detectChanges();
        },
      });
  }

  irAListado(): void {
    this.router.navigate(['/proveedores/listado']);
  }

  private obtenerTabDesdeRuta(): TabProveedores {
    const tab = this.route.snapshot.data['proveedoresTab'];
    if (tab === 'nuevo') {
      return 'nuevo';
    }
    return 'listado';
  }

  private crearFormularioInicial(): NuevoProveedorFormState {
    return {
      id: null,
      nombre: '',
      tipoDocumento: 'NIT',
      telefono: '',
      correo: '',
    };
  }

  private crearFormularioInicialEdicion(): EditarProveedorFormState {
    return {
      nombre: '',
      tipoDocumento: 'NIT',
      telefono: '',
      correo: '',
    };
  }

  private restablecerFormulario(form: NgForm): void {
    this.reiniciarFormularioProveedor();
    form.resetForm(this.nuevoProveedor);
  }

  private reiniciarFormularioProveedor(): void {
    this.nuevoProveedor = this.crearFormularioInicial();
  }

  private compararProveedores(a: Proveedor, b: Proveedor): number {
    if (this.campoOrden === 'id') {
      return a.id - b.id;
    }

    return a.nombre.localeCompare(b.nombre, 'es', { sensitivity: 'base' });
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
