import { Injectable, signal } from '@angular/core';

export type ModalMensajeTipo = 'error' | 'confirmacion';

export interface ModalMensajeState {
  visible: boolean;
  tipo: ModalMensajeTipo;
  titulo: string;
  mensaje: string;
  textoConfirmar: string;
  textoCancelar: string;
  mostrarCancelar: boolean;
  cerrarAlClickFondo: boolean;
}

export interface ModalMensajeConfig {
  titulo: string;
  mensaje?: string;
  tipo?: ModalMensajeTipo;
  textoConfirmar?: string;
  textoCancelar?: string;
  mostrarCancelar?: boolean;
  cerrarAlClickFondo?: boolean;
}

@Injectable({ providedIn: 'root' })
export class ModalMensajeService {
  private resolverActual: ((value: boolean) => void) | null = null;
  private readonly _estado = signal<ModalMensajeState>(this.crearEstadoInicial());

  readonly estado = this._estado.asReadonly();

  abrir(config: ModalMensajeConfig): Promise<boolean> {
    if (this.resolverActual) {
      this.resolverActual(false);
      this.resolverActual = null;
    }

    const estadoActualizado: ModalMensajeState = {
      visible: true,
      tipo: config.tipo ?? 'error',
      titulo: config.titulo,
      mensaje: config.mensaje ?? '',
      textoConfirmar: config.textoConfirmar ?? 'OK',
      textoCancelar: config.textoCancelar ?? 'Cancelar',
      mostrarCancelar: config.mostrarCancelar ?? false,
      cerrarAlClickFondo: config.cerrarAlClickFondo ?? true,
    };

    this._estado.set(estadoActualizado);

    return new Promise<boolean>((resolve) => {
      this.resolverActual = resolve;
    });
  }

  confirmar(
    config: Omit<ModalMensajeConfig, 'tipo' | 'mostrarCancelar' | 'textoCancelar'>
  ): Promise<boolean> {
    return this.abrir({
      ...config,
      tipo: 'confirmacion',
      mostrarCancelar: true,
      textoConfirmar: config.textoConfirmar ?? 'Confirmar',
      textoCancelar: 'Cancelar',
    });
  }

  error(config: Omit<ModalMensajeConfig, 'tipo' | 'mostrarCancelar' | 'textoCancelar'>): Promise<boolean> {
    return this.abrir({
      ...config,
      tipo: 'error',
      mostrarCancelar: false,
      textoConfirmar: config.textoConfirmar ?? 'OK',
    });
  }

  confirmarAccion(): void {
    this.cerrar(true);
  }

  cancelar(): void {
    this.cerrar(false);
  }

  cerrarPorFondo(): void {
    if (!this._estado().cerrarAlClickFondo) {
      return;
    }

    this.cerrar(false);
  }

  private cerrar(resultado: boolean): void {
    const resolver = this.resolverActual;
    this.resolverActual = null;
    this._estado.set(this.crearEstadoInicial());
    resolver?.(resultado);
  }

  private crearEstadoInicial(): ModalMensajeState {
    return {
      visible: false,
      tipo: 'error',
      titulo: '',
      mensaje: '',
      textoConfirmar: 'OK',
      textoCancelar: 'Cancelar',
      mostrarCancelar: false,
      cerrarAlClickFondo: true,
    };
  }
}
