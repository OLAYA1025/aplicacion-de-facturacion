import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { ModalMensajeService } from '../../../../shared/services/modal-mensaje.service';
import { obtenerMensajeError } from '../../../../shared/utils/error-message.util';
import { NegocioPlataformaResponseDTO } from '../models/negocio-plataforma-response.dto';
import { PlatformNegocioService } from '../services/platform-negocio.service';

@Component({
  selector: 'app-admin-negocios-list',
  imports: [CommonModule, RouterLink],
  templateUrl: './admin-negocios-list.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminNegociosListComponent implements OnInit {
  private readonly platformNegocioService = inject(PlatformNegocioService);
  private readonly modalMensajeService = inject(ModalMensajeService);
  private readonly cdr = inject(ChangeDetectorRef);

  loading = false;
  errorListado: string | null = null;
  negocios: NegocioPlataformaResponseDTO[] = [];
  procesandoNegocios: Record<string, boolean> = {};

  ngOnInit(): void {
    this.cargarNegocios();
  }

  cargarNegocios(): void {
    this.loading = true;
    this.errorListado = null;

    this.platformNegocioService
      .listarNegocios()
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe({
        next: (negocios) => {
          this.negocios = negocios;
          this.cdr.markForCheck();
        },
        error: (error) => {
          this.negocios = [];
          this.errorListado = obtenerMensajeError(
            error,
            'No se pudieron cargar los negocios. Intenta nuevamente.'
          );
          void this.modalMensajeService.error({
            titulo: 'No se pudo cargar el listado',
            mensaje: this.errorListado,
          });
          this.cdr.markForCheck();
        },
      });
  }

  async activarNegocio(negocio: NegocioPlataformaResponseDTO): Promise<void> {
    const confirmado = await this.modalMensajeService.confirmar({
      titulo: 'Activar negocio',
      mensaje: `¿Deseas activar "${negocio.nombre}"?`,
      textoConfirmar: 'Activar',
    });

    if (!confirmado) {
      return;
    }

    const id = String(negocio.id);
    this.procesandoNegocios[id] = true;
    this.cdr.markForCheck();

    this.platformNegocioService
      .activarNegocio(negocio.id)
      .pipe(
        finalize(() => {
          this.procesandoNegocios[id] = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe({
        next: (negocioActualizado) => {
          this.reemplazarNegocio(negocioActualizado);
          void this.modalMensajeService.abrir({
            tipo: 'confirmacion',
            titulo: 'Negocio activado',
            mensaje: `Se activó "${negocio.nombre}" correctamente.`,
            textoConfirmar: 'Entendido',
          });
          this.cdr.markForCheck();
        },
        error: (error) => {
          const mensaje = obtenerMensajeError(error, 'No se pudo activar el negocio.');
          void this.modalMensajeService.error({
            titulo: 'Error al activar',
            mensaje,
          });
          this.cdr.markForCheck();
        },
      });
  }

  async desactivarNegocio(negocio: NegocioPlataformaResponseDTO): Promise<void> {
    const confirmado = await this.modalMensajeService.confirmar({
      titulo: 'Desactivar negocio',
      mensaje: `¿Seguro que deseas desactivar "${negocio.nombre}"?`,
      textoConfirmar: 'Desactivar',
    });

    if (!confirmado) {
      return;
    }

    const id = String(negocio.id);
    this.procesandoNegocios[id] = true;
    this.cdr.markForCheck();

    this.platformNegocioService
      .desactivarNegocio(negocio.id)
      .pipe(
        finalize(() => {
          this.procesandoNegocios[id] = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe({
        next: (negocioActualizado) => {
          this.reemplazarNegocio(negocioActualizado);
          void this.modalMensajeService.abrir({
            tipo: 'confirmacion',
            titulo: 'Negocio desactivado',
            mensaje: `Se desactivó "${negocio.nombre}" correctamente.`,
            textoConfirmar: 'Entendido',
          });
          this.cdr.markForCheck();
        },
        error: (error) => {
          const mensaje = obtenerMensajeError(error, 'No se pudo desactivar el negocio.');
          void this.modalMensajeService.error({
            titulo: 'Error al desactivar',
            mensaje,
          });
          this.cdr.markForCheck();
        },
      });
  }

  estaProcesando(id: number | string): boolean {
    return !!this.procesandoNegocios[String(id)];
  }

  obtenerAdminNombre(negocio: NegocioPlataformaResponseDTO): string {
    return (
      negocio.adminNombre ||
      negocio.administradorPrincipal?.nombre ||
      negocio.admin?.nombre ||
      '-'
    );
  }

  obtenerAdminUsername(negocio: NegocioPlataformaResponseDTO): string {
    return (
      negocio.adminUsername ||
      negocio.administradorPrincipal?.username ||
      negocio.admin?.username ||
      '-'
    );
  }

  obtenerAdminEmail(negocio: NegocioPlataformaResponseDTO): string {
    return negocio.adminEmail || negocio.administradorPrincipal?.email || negocio.admin?.email || '-';
  }

  obtenerFechaCreacion(negocio: NegocioPlataformaResponseDTO): string {
    const fecha = negocio.fechaCreacion || negocio.createdAt;
    if (!fecha) {
      return '-';
    }

    const fechaParseada = new Date(fecha);
    if (Number.isNaN(fechaParseada.getTime())) {
      return fecha;
    }

    return fechaParseada.toLocaleString('es-CO');
  }

  private reemplazarNegocio(negocioActualizado: NegocioPlataformaResponseDTO): void {
    this.negocios = this.negocios.map((negocio) =>
      negocio.id === negocioActualizado.id
        ? {
            ...negocio,
            ...negocioActualizado,
          }
        : negocio
    );
  }
}
