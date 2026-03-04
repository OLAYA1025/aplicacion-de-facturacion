import { CommonModule } from '@angular/common';
import { HttpErrorResponse } from '@angular/common/http';
import { ChangeDetectorRef, Component, OnInit, ViewRef, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { finalize } from 'rxjs';
import { ModalMensajeService } from 'src/app/shared/services/modal-mensaje.service';
import { obtenerMensajeError } from 'src/app/shared/utils/error-message.util';
import { FacturaEstado } from '../models/factura-estado.model';
import { FacturaListItem } from '../models/factura-list-item.model';
import { PageResponse } from '../models/page-response.model';
import { FacturasService } from '../services/facturas.service';

interface EstadoFiltroOption {
  value: FacturaEstado;
  label: string;
}

@Component({
  selector: 'app-facturas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './facturas.component.html',
})
export class FacturasComponent implements OnInit {
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly facturasService = inject(FacturasService);
  private readonly modalMensajeService = inject(ModalMensajeService);

  readonly estadosDisponibles: EstadoFiltroOption[] = [
    { value: 'BORRADOR', label: 'Borrador' },
    { value: 'ENVIADO_PT', label: 'Enviado PT' },
    { value: 'ACEPTADO_DIAN', label: 'Aceptado DIAN' },
    { value: 'RECHAZADO', label: 'Rechazado' },
    { value: 'PENDIENTE_CONTINGENCIA', label: 'Pendiente contingencia' },
    { value: 'PENDIENTE_ENVIO', label: 'Pendiente envio' },
  ];

  readonly tamanosPagina: number[] = [10, 20, 50];

  loadingFacturas = false;
  errorListado: string | null = null;
  errorFiltros: string | null = null;
  imprimiendoFacturaId: number | null = null;
  consultandoErrorFacturaId: number | null = null;
  reenviandoFacturaId: number | null = null;

  busqueda = '';
  fechaDesde = '';
  fechaHasta = '';
  estadosSeleccionados: FacturaEstado[] = [];
  campoSort = 'createdAt,desc';

  facturas: FacturaListItem[] = [];

  paginaActual = 0;
  tamanoPagina = 10;
  totalPaginas = 0;
  totalElementos = 0;

  ngOnInit(): void {
    this.cargarFacturas();
  }

  get paginasVisibles(): number[] {
    if (this.totalPaginas <= 0) {
      return [];
    }

    const paginas: number[] = [];
    const maxPaginasVisibles = 5;
    let inicio = Math.max(0, this.paginaActual - Math.floor(maxPaginasVisibles / 2));
    let fin = Math.min(this.totalPaginas - 1, inicio + maxPaginasVisibles - 1);

    if (fin - inicio < maxPaginasVisibles - 1) {
      inicio = Math.max(0, fin - maxPaginasVisibles + 1);
    }

    for (let i = inicio; i <= fin; i += 1) {
      paginas.push(i);
    }

    return paginas;
  }

  onBuscar(): void {
    if (this.fechaDesde && this.fechaHasta && this.fechaDesde > this.fechaHasta) {
      this.errorFiltros = 'La fecha inicial no puede ser mayor que la fecha final.';
      return;
    }

    this.errorFiltros = null;
    this.paginaActual = 0;
    this.cargarFacturas();
  }

  onLimpiar(): void {
    this.busqueda = '';
    this.fechaDesde = '';
    this.fechaHasta = '';
    this.estadosSeleccionados = [];
    this.campoSort = 'createdAt,desc';
    this.errorFiltros = null;
    this.paginaActual = 0;
    this.tamanoPagina = 20;
    this.cargarFacturas();
  }

  onTamanoPaginaChange(value: number | string): void {
    const size = Number(value);
    if (!Number.isFinite(size) || size <= 0) {
      return;
    }

    this.tamanoPagina = size;
    this.paginaActual = 0;
    this.cargarFacturas();
  }

  onSortChange(sort: string): void {
    this.campoSort = sort;
    this.paginaActual = 0;
    this.cargarFacturas();
  }

  onEstadoToggle(estado: FacturaEstado, checked: boolean): void {
    if (checked) {
      if (!this.estadosSeleccionados.includes(estado)) {
        this.estadosSeleccionados = [...this.estadosSeleccionados, estado];
      }
      return;
    }

    this.estadosSeleccionados = this.estadosSeleccionados.filter(
      (estadoSeleccionado) => estadoSeleccionado !== estado
    );
  }

  estaEstadoSeleccionado(estado: FacturaEstado): boolean {
    return this.estadosSeleccionados.includes(estado);
  }

  puedeImprimir(factura: FacturaListItem): boolean {
    return factura.estado === 'ACEPTADO_DIAN';
  }

  puedeVerError(factura: FacturaListItem): boolean {
    return factura.estado === 'PENDIENTE_CONTINGENCIA' || factura.estado === 'RECHAZADO';
  }

  puedeReenviar(factura: FacturaListItem): boolean {
    return factura.estado === 'PENDIENTE_CONTINGENCIA';
  }

  imprimirFactura(factura: FacturaListItem): void {
    if (!this.puedeImprimir(factura) || this.imprimiendoFacturaId !== null) {
      return;
    }

    this.imprimiendoFacturaId = factura.id;

    this.facturasService
      .imprimirFactura(factura.id)
      .pipe(
        finalize(() => {
          this.imprimiendoFacturaId = null;
          this.refrescarVista();
        })
      )
      .subscribe({
        next: () => {
          this.refrescarVista();
        },
        error: (error: unknown) => {
          if (this.esRespuesta409(error)) {
            void this.modalMensajeService.error({
              titulo: 'Factura aún no disponible',
              mensaje: 'Factura aún no disponible.',
              textoConfirmar: 'Entendido',
            });
            this.refrescarVista();
            return;
          }

          void this.modalMensajeService.error({
            titulo: 'No se pudo imprimir la factura',
            mensaje: obtenerMensajeError(
              error,
              'No se pudo generar la vista previa del PDF. Intenta nuevamente.'
            ),
            textoConfirmar: 'Entendido',
          });
          this.refrescarVista();
        },
      });
  }

  verErrorFactura(factura: FacturaListItem): void {
    if (!this.puedeVerError(factura) || this.consultandoErrorFacturaId !== null) {
      return;
    }

    this.consultandoErrorFacturaId = factura.id;

    this.facturasService
      .obtenerMensajeErrorFactura(factura.id)
      .pipe(
        finalize(() => {
          this.consultandoErrorFacturaId = null;
          this.refrescarVista();
        })
      )
      .subscribe({
        next: (mensaje) => {
          const mensajeVisible = mensaje?.trim() ? mensaje.trim() : 'No hay mensaje de error disponible.';
          void this.modalMensajeService.error({
            titulo: 'Detalle del error de la factura',
            mensaje: mensajeVisible,
            textoConfirmar: 'Cerrar',
          });
          this.refrescarVista();
        },
        error: (error: unknown) => {
          void this.modalMensajeService.error({
            titulo: 'No se pudo obtener el error',
            mensaje: obtenerMensajeError(
              error,
              'No se pudo obtener el mensaje de error de la factura. Intenta nuevamente.'
            ),
            textoConfirmar: 'Entendido',
          });
          this.refrescarVista();
        },
      });
  }

  reenviarFactura(factura: FacturaListItem): void {
    if (!this.puedeReenviar(factura) || this.reenviandoFacturaId !== null) {
      return;
    }

    this.reenviandoFacturaId = factura.id;

    this.facturasService
      .reenviarFactura(factura.id)
      .pipe(
        finalize(() => {
          this.reenviandoFacturaId = null;
          this.refrescarVista();
        })
      )
      .subscribe({
        next: () => {
          void this.modalMensajeService.abrir({
            tipo: 'confirmacion',
            titulo: 'Factura reenviada',
            mensaje: 'La factura fue marcada para reenvío correctamente.',
            textoConfirmar: 'Entendido',
          });
          this.cargarFacturas();
          this.refrescarVista();
        },
        error: (error: unknown) => {
          void this.modalMensajeService.error({
            titulo: 'No se pudo reenviar la factura',
            mensaje: obtenerMensajeError(error, 'No se pudo reenviar la factura. Intenta nuevamente.'),
            textoConfirmar: 'Entendido',
          });
          this.refrescarVista();
        },
      });
  }

  irAPagina(pagina: number): void {
    if (pagina < 0 || pagina >= this.totalPaginas || pagina === this.paginaActual) {
      return;
    }

    this.paginaActual = pagina;
    this.cargarFacturas();
  }

  paginaAnterior(): void {
    if (this.paginaActual <= 0) {
      return;
    }
    this.irAPagina(this.paginaActual - 1);
  }

  paginaSiguiente(): void {
    if (this.paginaActual >= this.totalPaginas - 1) {
      return;
    }
    this.irAPagina(this.paginaActual + 1);
  }

  numeroFacturaVisible(factura: FacturaListItem): string {
    return factura.numeroDocumento ?? factura.referenceCode ?? '-';
  }

  nombreClienteVisible(factura: FacturaListItem): string {
    return factura.clienteNombre ?? 'Consumidor final';
  }

  identificacionClienteVisible(factura: FacturaListItem): string | null {
    return factura.clienteIdentificacion;
  }

  textoEstado(estado: FacturaEstado): string {
    switch (estado) {
      case 'BORRADOR':
        return 'Borrador';
      case 'ENVIADO_PT':
        return 'Enviado PT';
      case 'ACEPTADO_DIAN':
        return 'Aceptado DIAN';
      case 'RECHAZADO':
        return 'Rechazado';
      case 'PENDIENTE_CONTINGENCIA':
        return 'Pendiente contingencia';
      case 'PENDIENTE_ENVIO':
        return 'Pendiente envio';
      default:
        return estado;
    }
  }

  claseEstado(estado: FacturaEstado): string {
    switch (estado) {
      case 'ACEPTADO_DIAN':
        return 'bg-emerald-100 text-emerald-700';
      case 'RECHAZADO':
        return 'bg-red-100 text-red-700';
      case 'ENVIADO_PT':
        return 'bg-blue-100 text-blue-700';
      case 'BORRADOR':
        return 'bg-gray-100 text-gray-700';
      case 'PENDIENTE_CONTINGENCIA':
        return 'bg-amber-100 text-amber-700';
      case 'PENDIENTE_ENVIO':
        return 'bg-slate-100 text-slate-700';
      default:
        return 'bg-gray-100 text-gray-700';
    }
  }

  formatearFechaHora(fechaIso: string | null): string {
    if (!fechaIso) {
      return '-';
    }

    const fecha = new Date(fechaIso);
    if (Number.isNaN(fecha.getTime())) {
      return '-';
    }

    return fecha.toLocaleString('es-CO', {
      dateStyle: 'medium',
      timeStyle: 'short',
    });
  }

  formatearTotal(total: FacturaListItem['total']): string {
    if (total === null || total === undefined) {
      return '-';
    }

    const totalNormalizado = typeof total === 'string' ? Number(total) : total;
    if (!Number.isFinite(totalNormalizado)) {
      return '-';
    }

    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
    }).format(totalNormalizado);
  }

  private cargarFacturas(): void {
    this.loadingFacturas = true;
    this.errorListado = null;

    this.facturasService
      .listFacturas({
        page: this.paginaActual,
        size: this.tamanoPagina,
        q: this.busqueda.trim(),
        estados: this.estadosSeleccionados,
        from: this.fechaDesde || undefined,
        to: this.fechaHasta || undefined,
        sort: this.campoSort,
      })
      .pipe(
        finalize(() => {
          this.loadingFacturas = false;
          this.refrescarVista();
        })
      )
      .subscribe({
        next: (response) => {
          const responseConFallback = response as PageResponse<FacturaListItem> & {
            total_pages?: number;
            total_elements?: number;
            page?: {
              number?: number;
              size?: number;
              totalPages?: number;
              total_pages?: number;
              totalElements?: number;
              total_elements?: number;
            };
          };

          const contenido = responseConFallback.content ?? [];
          const totalElementsRaw =
            responseConFallback.totalElements ??
            responseConFallback.total_elements ??
            responseConFallback.page?.totalElements ??
            responseConFallback.page?.total_elements;

          const sizeRaw = responseConFallback.size ?? responseConFallback.page?.size ?? this.tamanoPagina;

          const totalPagesRaw =
            responseConFallback.totalPages ??
            responseConFallback.total_pages ??
            responseConFallback.page?.totalPages ??
            responseConFallback.page?.total_pages;

          const paginaRaw = responseConFallback.number ?? responseConFallback.page?.number;

          const totalElementos = Number.isFinite(Number(totalElementsRaw))
            ? Number(totalElementsRaw)
            : contenido.length;

          const size = Number.isFinite(Number(sizeRaw)) && Number(sizeRaw) > 0
            ? Number(sizeRaw)
            : this.tamanoPagina;

          const totalPaginasCalculadas = size > 0 ? Math.ceil(totalElementos / size) : 0;
          const totalPaginasRespuesta = Number.isFinite(Number(totalPagesRaw))
            ? Number(totalPagesRaw)
            : 0;

          const totalPaginas = Math.max(totalPaginasRespuesta, totalPaginasCalculadas);
          const paginaActual = Number.isFinite(Number(paginaRaw)) ? Number(paginaRaw) : 0;

          this.facturas = contenido;
          this.totalElementos = totalElementos;
          this.totalPaginas = totalPaginas;
          this.paginaActual = Math.min(Math.max(paginaActual, 0), Math.max(totalPaginas - 1, 0));
          this.refrescarVista();
        },
        error: (error: unknown) => {
          this.errorListado = obtenerMensajeError(
            error,
            'No se pudo cargar el listado de facturas. Intenta nuevamente.'
          );
          this.facturas = [];
          this.totalPaginas = 0;
          this.totalElementos = 0;
          this.paginaActual = 0;
          this.refrescarVista();
        },
      });
  }

  private esRespuesta409(error: unknown): boolean {
    if (error instanceof HttpErrorResponse) {
      if (error.status === 409) {
        return true;
      }

      const payloadStatus = error.error?.status;
      if (payloadStatus === 409 || payloadStatus === '409') {
        return true;
      }
    }

    const errorObject = error as { status?: unknown; error?: { status?: unknown } } | null;
    if (errorObject?.status === 409 || errorObject?.status === '409') {
      return true;
    }

    const payloadStatus = errorObject?.error?.status;
    if (payloadStatus === 409 || payloadStatus === '409') {
      return true;
    }

    return false;
  }

  private refrescarVista(): void {
    const viewRef = this.cdr as ViewRef;
    if (!viewRef.destroyed) {
      this.cdr.detectChanges();
    }
  }
}
