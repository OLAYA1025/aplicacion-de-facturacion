import { CommonModule } from '@angular/common';
import {
  ChangeDetectorRef,
  Component,
  EventEmitter,
  Input,
  NgZone,
  OnChanges,
  OnDestroy,
  Output,
  SimpleChanges,
  inject,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Subscription, finalize, timeout } from 'rxjs';
import { GrupoNotaCategoria, NotaPredefinida, TipoSeleccionNota } from 'src/app/core/models/notas-predefinidas.model';
import { ModalMensajeService } from 'src/app/shared/services/modal-mensaje.service';
import { obtenerMensajeError } from 'src/app/shared/utils/error-message.util';
import { Categoria } from '../../models/categoria.model';
import { CategoriaService } from '../../services/categoria.service';

@Component({
  selector: 'app-productos-categorias',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './productos-categorias.component.html',
})
export class ProductosCategoriasComponent implements OnChanges, OnDestroy {
  @Input() categorias: Categoria[] = [];
  @Output() categoriasActualizadas = new EventEmitter<Categoria[]>();

  categoriaSeleccionadaId: number | null = null;
  gruposNotas: GrupoNotaCategoria[] = [];

  loadingGrupos = false;
  guardandoCategoria = false;
  guardandoGrupo = false;
  guardandoNota = false;

  modalCategoriaAbierto = false;
  categoriaEditando: Categoria | null = null;
  categoriaNombre = '';

  modalGrupoAbierto = false;
  grupoEditando: GrupoNotaCategoria | null = null;
  grupoNombre = '';
  grupoObligatorio = false;
  grupoTipoSeleccion: TipoSeleccionNota = 'UNICO';

  modalNotaAbierto = false;
  grupoDestinoNota: GrupoNotaCategoria | null = null;
  notaEditando: NotaPredefinida | null = null;
  notaDescripcion = '';

  private readonly categoriaService = inject(CategoriaService);
  private readonly modalMensajeService = inject(ModalMensajeService);
  private readonly ngZone = inject(NgZone);
  private readonly cdr = inject(ChangeDetectorRef);
  private cargaGruposSubscription: Subscription | null = null;
  private cargaGruposToken = 0;
  private destruido = false;

  ngOnChanges(_: SimpleChanges): void {
    if (!this.categorias.length) {
      this.cancelarCargaGrupos();
      this.categoriaSeleccionadaId = null;
      this.gruposNotas = [];
      this.loadingGrupos = false;
      return;
    }

    const categoriaVigente = this.categorias.some((categoria) => categoria.id === this.categoriaSeleccionadaId);
    if (!categoriaVigente) {
      this.categoriaSeleccionadaId = this.categorias[0].id;
      this.cargarGruposNotas();
    }
  }

  ngOnDestroy(): void {
    this.destruido = true;
    if (this.cargaGruposSubscription) {
      this.cargaGruposSubscription.unsubscribe();
      this.cargaGruposSubscription = null;
    }
  }

  seleccionarCategoria(categoriaId: number): void {
    if (this.categoriaSeleccionadaId === categoriaId && !this.loadingGrupos) {
      return;
    }
    this.categoriaSeleccionadaId = categoriaId;
    this.cargarGruposNotas();
  }

  abrirCrearCategoria(): void {
    this.categoriaEditando = null;
    this.categoriaNombre = '';
    this.modalCategoriaAbierto = true;
  }

  abrirEditarCategoria(categoria: Categoria): void {
    this.categoriaEditando = categoria;
    this.categoriaNombre = categoria.nombre;
    this.modalCategoriaAbierto = true;
  }

  editarCategoriaSeleccionada(): void {
    const categoria = this.obtenerCategoriaSeleccionada();
    if (!categoria) {
      return;
    }
    this.abrirEditarCategoria(categoria);
  }

  cerrarModalCategoria(): void {
    if (this.guardandoCategoria) {
      return;
    }
    this.modalCategoriaAbierto = false;
    this.categoriaEditando = null;
    this.categoriaNombre = '';
  }

  guardarCategoria(): void {
    const nombre = this.categoriaNombre.trim();
    if (nombre.length < 2) {
      void this.mostrarError('El nombre de la categoría debe tener mínimo 2 caracteres.');
      return;
    }

    this.guardandoCategoria = true;
    const request$ = this.categoriaEditando
      ? this.categoriaService.actualizarCategoria(this.categoriaEditando.id, { nombre })
      : this.categoriaService.crearCategoria({ nombre });

    request$
      .pipe(
        finalize(() => {
          this.ejecutarEnZona(() => {
            this.guardandoCategoria = false;
          });
        })
      )
      .subscribe({
        next: (categoriaGuardada) => {
          this.ejecutarEnZona(() => {
            const categoriasActualizadas = this.normalizarCategorias(
              this.categoriaEditando
                ? this.categorias.map((categoria) =>
                    categoria.id === categoriaGuardada.id ? categoriaGuardada : categoria
                  )
                : [...this.categorias, categoriaGuardada]
            );
            this.categoriasActualizadas.emit(categoriasActualizadas);
            this.categoriaSeleccionadaId = categoriaGuardada.id;
            this.cerrarModalCategoria();
            this.cargarGruposNotas();
          });
        },
        error: (error) => {
          void this.mostrarError(
            this.categoriaEditando
              ? 'No se pudo actualizar la categoría.'
              : 'No se pudo crear la categoría.',
            error
          );
        },
      });
  }

  abrirCrearGrupo(): void {
    if (!this.categoriaSeleccionadaId) {
      return;
    }
    this.grupoEditando = null;
    this.grupoNombre = '';
    this.grupoObligatorio = false;
    this.grupoTipoSeleccion = 'UNICO';
    this.modalGrupoAbierto = true;
  }

  abrirEditarGrupo(grupo: GrupoNotaCategoria): void {
    this.grupoEditando = grupo;
    this.grupoNombre = grupo.nombre;
    this.grupoObligatorio = grupo.obligatorio;
    this.grupoTipoSeleccion = grupo.tipoSeleccion;
    this.modalGrupoAbierto = true;
  }

  cerrarModalGrupo(): void {
    if (this.guardandoGrupo) {
      return;
    }
    this.modalGrupoAbierto = false;
    this.grupoEditando = null;
    this.grupoNombre = '';
    this.grupoObligatorio = false;
    this.grupoTipoSeleccion = 'UNICO';
  }

  guardarGrupo(): void {
    if (!this.categoriaSeleccionadaId) {
      return;
    }

    const nombre = this.grupoNombre.trim();
    if (nombre.length < 2) {
      void this.mostrarError('El nombre del grupo debe tener mínimo 2 caracteres.');
      return;
    }

    this.guardandoGrupo = true;
    const payload = {
      nombre,
      obligatorio: this.grupoObligatorio,
      tipoSeleccion: this.grupoTipoSeleccion,
    };
    const categoriaIdGrupo = this.grupoEditando?.categoriaId ?? this.categoriaSeleccionadaId;
    if (!categoriaIdGrupo) {
      this.guardandoGrupo = false;
      return;
    }
    const request$ = this.grupoEditando
      ? this.categoriaService.actualizarGrupoNotaCategoria(
          categoriaIdGrupo,
          this.grupoEditando.id,
          payload
        )
      : this.categoriaService.crearGrupoNotaCategoria(categoriaIdGrupo, payload);

    request$
      .pipe(
        finalize(() => {
          this.ejecutarEnZona(() => {
            this.guardandoGrupo = false;
          });
        })
      )
      .subscribe({
        next: () => {
          this.ejecutarEnZona(() => {
            this.cerrarModalGrupo();
            this.cargarGruposNotas();
          });
        },
        error: (error) => {
          void this.mostrarError(
            this.grupoEditando ? 'No se pudo actualizar el grupo.' : 'No se pudo crear el grupo.',
            error
          );
        },
      });
  }

  async eliminarGrupo(grupo: GrupoNotaCategoria): Promise<void> {
    const categoriaIdGrupo = grupo.categoriaId || this.categoriaSeleccionadaId;
    if (!categoriaIdGrupo) {
      return;
    }
    const confirmado = await this.modalMensajeService.confirmar({
      titulo: `¿Eliminar grupo "${grupo.nombre}"?`,
      mensaje: 'Se eliminarán también sus notas predefinidas.',
      textoConfirmar: 'Eliminar',
    });
    if (!confirmado) {
      return;
    }
    this.categoriaService
      .eliminarGrupoNotaCategoria(categoriaIdGrupo, grupo.id)
      .subscribe({
        next: () =>
          this.ejecutarEnZona(() => {
            this.cargarGruposNotas();
          }),
        error: (error) => {
          void this.mostrarError('No se pudo eliminar el grupo.', error);
        },
      });
  }

  abrirCrearNota(grupo: GrupoNotaCategoria): void {
    this.grupoDestinoNota = grupo;
    this.notaEditando = null;
    this.notaDescripcion = '';
    this.modalNotaAbierto = true;
  }

  abrirEditarNota(grupo: GrupoNotaCategoria, nota: NotaPredefinida): void {
    this.grupoDestinoNota = grupo;
    this.notaEditando = nota;
    this.notaDescripcion = nota.descripcion;
    this.modalNotaAbierto = true;
  }

  cerrarModalNota(forzarCierre = false): void {
    if (this.guardandoNota && !forzarCierre) {
      return;
    }
    this.modalNotaAbierto = false;
    this.grupoDestinoNota = null;
    this.notaEditando = null;
    this.notaDescripcion = '';
  }

  guardarNota(): void {
    if (!this.grupoDestinoNota) {
      return;
    }
    const categoriaIdGrupo = this.grupoDestinoNota.categoriaId || this.categoriaSeleccionadaId;
    if (!categoriaIdGrupo) {
      return;
    }

    const descripcion = this.notaDescripcion.trim();
    if (descripcion.length < 2) {
      void this.mostrarError('La descripción de la nota debe tener mínimo 2 caracteres.');
      return;
    }

    this.guardandoNota = true;
    const payload = { descripcion };
    const request$ = this.notaEditando
      ? this.categoriaService.actualizarNotaPredefinida(
          categoriaIdGrupo,
          this.grupoDestinoNota.id,
          this.notaEditando.id,
          payload
        )
      : this.categoriaService.crearNotaPredefinida(
          categoriaIdGrupo,
          this.grupoDestinoNota.id,
          payload
        );

    request$
      .pipe(
        finalize(() => {
          this.ejecutarEnZona(() => {
            this.guardandoNota = false;
          });
        })
      )
      .subscribe({
        next: () => {
          this.ejecutarEnZona(() => {
            this.cerrarModalNota(true);
            this.cargarGruposNotas();
          });
        },
        error: (error) => {
          void this.mostrarError(
            this.notaEditando ? 'No se pudo actualizar la nota.' : 'No se pudo crear la nota.',
            error
          );
        },
      });
  }

  async eliminarNota(grupo: GrupoNotaCategoria, nota: NotaPredefinida): Promise<void> {
    const categoriaIdGrupo = grupo.categoriaId || this.categoriaSeleccionadaId;
    if (!categoriaIdGrupo) {
      return;
    }
    const confirmado = await this.modalMensajeService.confirmar({
      titulo: `¿Eliminar nota "${nota.descripcion}"?`,
      mensaje: `Esta acción quitará la nota del grupo "${grupo.nombre}".`,
      textoConfirmar: 'Eliminar',
    });
    if (!confirmado) {
      return;
    }

    this.categoriaService
      .eliminarNotaPredefinida(categoriaIdGrupo, grupo.id, nota.id)
      .subscribe({
        next: () =>
          this.ejecutarEnZona(() => {
            this.cargarGruposNotas();
          }),
        error: (error) => {
          void this.mostrarError('No se pudo eliminar la nota.', error);
        },
      });
  }

  private cargarGruposNotas(): void {
    if (!this.categoriaSeleccionadaId) {
      this.cargaGruposToken++;
      this.cancelarCargaGrupos();
      this.ejecutarEnZona(() => {
        this.gruposNotas = [];
        this.loadingGrupos = false;
      });
      return;
    }

    const categoriaId = this.categoriaSeleccionadaId;
    const token = ++this.cargaGruposToken;
    this.cancelarCargaGrupos();
    this.ejecutarEnZona(() => {
      this.gruposNotas = [];
      this.loadingGrupos = true;
    });
    this.cargaGruposSubscription = this.categoriaService
      .obtenerGruposNotasPorCategoria(categoriaId)
      .pipe(
        timeout(15000),
        finalize(() => {
          if (token !== this.cargaGruposToken || categoriaId !== this.categoriaSeleccionadaId) {
            return;
          }
          this.ejecutarEnZona(() => {
            this.loadingGrupos = false;
            this.cargaGruposSubscription = null;
          });
        })
      )
      .subscribe({
        next: (grupos) => {
          if (token !== this.cargaGruposToken || categoriaId !== this.categoriaSeleccionadaId) {
            return;
          }
          try {
            const gruposSeguros = Array.isArray(grupos) ? grupos : [];
            this.ejecutarEnZona(() => {
              this.gruposNotas = this.normalizarGrupos(gruposSeguros, categoriaId);
            });
          } catch (error) {
            this.ejecutarEnZona(() => {
              this.gruposNotas = [];
            });
            void this.mostrarError('No se pudieron procesar los grupos de notas.', error);
          }
        },
        error: (error) => {
          if (token !== this.cargaGruposToken || categoriaId !== this.categoriaSeleccionadaId) {
            return;
          }
          this.ejecutarEnZona(() => {
            this.gruposNotas = [];
          });
          void this.mostrarError('No se pudieron cargar los grupos de notas.', error);
        },
      });
  }

  private normalizarCategorias(categorias: Categoria[]): Categoria[] {
    const categoriasMap = new Map<number, Categoria>();
    categorias.forEach((categoria) => {
      categoriasMap.set(categoria.id, categoria);
    });
    return Array.from(categoriasMap.values()).sort((a, b) =>
      a.nombre.localeCompare(b.nombre, 'es', { sensitivity: 'base' })
    );
  }

  obtenerCategoriaSeleccionada(): Categoria | null {
    if (!this.categoriaSeleccionadaId) {
      return null;
    }
    return this.categorias.find((categoria) => categoria.id === this.categoriaSeleccionadaId) ?? null;
  }

  private normalizarGrupos(grupos: GrupoNotaCategoria[], categoriaId: number): GrupoNotaCategoria[] {
    return [...grupos]
      .sort((a, b) => a.nombre.localeCompare(b.nombre, 'es', { sensitivity: 'base' }))
      .map((grupo) => ({
        ...grupo,
        categoriaId: grupo.categoriaId || categoriaId,
        notas: [...(grupo.notas ?? [])].sort((a, b) =>
          a.descripcion.localeCompare(b.descripcion, 'es', { sensitivity: 'base' })
        ),
      }));
  }

  private cancelarCargaGrupos(): void {
    if (this.cargaGruposSubscription) {
      this.cargaGruposSubscription.unsubscribe();
      this.cargaGruposSubscription = null;
    }
  }

  private ejecutarEnZona(accion: () => void): void {
    if (this.destruido) {
      return;
    }
    if (NgZone.isInAngularZone()) {
      accion();
      this.refrescarVista();
      return;
    }
    this.ngZone.run(() => {
      accion();
      this.refrescarVista();
    });
  }

  private refrescarVista(): void {
    if (this.destruido) {
      return;
    }
    try {
      this.cdr.detectChanges();
    } catch {
      // Ignorar errores si la vista ya fue destruida entre ciclos.
    }
  }

  private mostrarError(mensaje: string, error?: unknown): Promise<boolean> {
    return this.modalMensajeService.error({
      titulo: 'Error',
      mensaje: error ? obtenerMensajeError(error, mensaje) : mensaje,
      textoConfirmar: 'Entendido',
    });
  }
}
