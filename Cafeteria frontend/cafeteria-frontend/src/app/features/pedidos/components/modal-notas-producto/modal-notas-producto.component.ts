import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, Output, SimpleChanges } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { GrupoNotaCategoria } from 'src/app/core/models/notas-predefinidas.model';

@Component({
  selector: 'app-modal-notas-producto',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './modal-notas-producto.component.html',
})
export class ModalNotasProductoComponent implements OnChanges {
  @Input() visible = false;
  @Input() productoNombre = '';
  @Input() grupos: GrupoNotaCategoria[] = [];
  @Input() selectedNoteIds: number[] = [];
  @Input() cantidadInicial = 1;
  @Input() cantidadMaxima: number | null = null;

  @Output() cerrar = new EventEmitter<void>();
  @Output() confirmar = new EventEmitter<{ notaIdsSeleccionadas: number[]; cantidad: number }>();

  seleccionPorGrupo: Record<number, number[]> = {};
  cantidad = 1;
  mostrarErrores = false;

  ngOnChanges(changes: SimpleChanges): void {
    if (!changes['visible'] && !changes['grupos'] && !changes['selectedNoteIds']) {
      return;
    }
    if (!this.visible) {
      this.mostrarErrores = false;
      return;
    }

    this.inicializarSeleccion();
    this.cantidad = this.normalizarCantidad(this.cantidadInicial);
  }

  seleccionarNotaUnica(grupoId: number, notaId: number): void {
    this.seleccionPorGrupo[grupoId] = [notaId];
  }

  toggleNotaMultiple(grupoId: number, notaId: number, checked: boolean): void {
    const seleccionActual = this.seleccionPorGrupo[grupoId] ?? [];
    if (checked) {
      if (!seleccionActual.includes(notaId)) {
        this.seleccionPorGrupo[grupoId] = [...seleccionActual, notaId];
      }
      return;
    }

    this.seleccionPorGrupo[grupoId] = seleccionActual.filter((id) => id !== notaId);
  }

  estaNotaSeleccionada(grupoId: number, notaId: number): boolean {
    return (this.seleccionPorGrupo[grupoId] ?? []).includes(notaId);
  }

  confirmarSeleccion(): void {
    if (!this.esSeleccionValida()) {
      this.mostrarErrores = true;
      return;
    }
    this.confirmar.emit({
      notaIdsSeleccionadas: this.obtenerIdsSeleccionados(),
      cantidad: this.normalizarCantidad(this.cantidad),
    });
  }

  intentarCerrar(): void {
    this.mostrarErrores = false;
    this.cerrar.emit();
  }

  aumentarCantidad(): void {
    this.cantidad = this.normalizarCantidad(this.cantidad + 1);
  }

  disminuirCantidad(): void {
    this.cantidad = this.normalizarCantidad(this.cantidad - 1);
  }

  onCantidadChange(valor: string | number): void {
    this.cantidad = this.normalizarCantidad(valor);
  }

  esGrupoInvalido(grupoId: number): boolean {
    if (!this.mostrarErrores) {
      return false;
    }
    const grupo = this.grupos.find((grupoActual) => grupoActual.id === grupoId);
    if (!grupo || !grupo.obligatorio) {
      return false;
    }
    return (this.seleccionPorGrupo[grupo.id] ?? []).length === 0;
  }

  private inicializarSeleccion(): void {
    this.mostrarErrores = false;
    const seleccionSet = new Set(this.selectedNoteIds);
    const nuevaSeleccion: Record<number, number[]> = {};

    for (const grupo of this.grupos) {
      const notasSeleccionadasGrupo = grupo.notas
        .filter((nota) => seleccionSet.has(nota.id))
        .map((nota) => nota.id);
      if (grupo.tipoSeleccion === 'UNICO') {
        nuevaSeleccion[grupo.id] = notasSeleccionadasGrupo.length ? [notasSeleccionadasGrupo[0]] : [];
      } else {
        nuevaSeleccion[grupo.id] = notasSeleccionadasGrupo;
      }
    }

    this.seleccionPorGrupo = nuevaSeleccion;
  }

  private esSeleccionValida(): boolean {
    if (this.faltanGruposObligatorios()) {
      return false;
    }
    for (const grupo of this.grupos) {
      const seleccionGrupo = this.seleccionPorGrupo[grupo.id] ?? [];
      if (grupo.tipoSeleccion === 'UNICO' && seleccionGrupo.length > 1) {
        return false;
      }
    }
    return true;
  }

  private faltanGruposObligatorios(): boolean {
    for (const grupo of this.grupos) {
      const seleccionGrupo = this.seleccionPorGrupo[grupo.id] ?? [];
      if (grupo.obligatorio && seleccionGrupo.length === 0) {
        return true;
      }
    }
    return false;
  }

  private obtenerIdsSeleccionados(): number[] {
    const ids = Object.values(this.seleccionPorGrupo).flat();
    return Array.from(new Set(ids));
  }

  private normalizarCantidad(valor: string | number): number {
    const numero = typeof valor === 'number' ? valor : Number(valor);
    const entero = Number.isFinite(numero) ? Math.floor(numero) : 1;
    const minimo = Math.max(1, entero);

    if (this.cantidadMaxima === null || this.cantidadMaxima === undefined) {
      return minimo;
    }

    return Math.min(minimo, Math.max(1, Math.floor(this.cantidadMaxima)));
  }
}
