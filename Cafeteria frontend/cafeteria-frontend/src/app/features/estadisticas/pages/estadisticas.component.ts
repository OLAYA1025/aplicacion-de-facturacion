import { CommonModule } from '@angular/common';
import { ApplicationRef, ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Observable, finalize } from 'rxjs';
import {
  EstadisticasDashboard,
  HistorialCajaCerrada,
  HistorialRetiro,
  PaginacionMetadata,
  ResumenEstadisticas,
  TopProductosVendidos,
} from '../models/resumen-estadisticas.model';
import { EstadisticasService } from '../services/estadisticas.service';
import { ModalMensajeService } from '../../../shared/services/modal-mensaje.service';
import { obtenerMensajeError } from '../../../shared/utils/error-message.util';

type ModoFiltro = 'semana' | 'mes' | 'anio';

interface BarraUtilidad {
  label: string;
  value: number;
  heightPercent: number;
}

interface BarraTopProducto {
  label: string;
  value: number;
  widthPercent: number;
}

interface EjeTick {
  value: number;
  label: string;
  bottomPercent: number;
}

interface ValidacionFiltro {
  esValido: boolean;
  mensaje: string | null;
}

@Component({
  selector: 'app-estadisticas',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './estadisticas.component.html',
})
export class EstadisticasComponent implements OnInit {
  private readonly cdr = inject(ChangeDetectorRef);
  private readonly appRef = inject(ApplicationRef);
  private readonly estadisticasService = inject(EstadisticasService);
  private readonly modalMensajeService = inject(ModalMensajeService);

  readonly opcionesModo: ReadonlyArray<{ value: ModoFiltro; label: string }> = [
    { value: 'semana', label: 'Semana' },
    { value: 'mes', label: 'Mes' },
    { value: 'anio', label: 'Año' },
  ];

  readonly anioActual: number = new Date().getFullYear();

  modoSeleccionado: ModoFiltro = 'semana';
  fechaSemana: string = this.obtenerFechaHoyISO();
  anioMes: number = this.anioActual;
  anioDesde: number = this.anioActual - 4;
  anioHasta: number = this.anioActual;
  paginaCajas: number = 0;
  tamanioCajas: number = 10;
  paginaRetiros: number = 0;
  tamanioRetiros: number = 10;

  loading: boolean = false;
  error: string | null = null;

  dashboard: EstadisticasDashboard | null = null;
  barrasUtilidades: BarraUtilidad[] = [];
  barrasTopProductos: BarraTopProducto[] = [];
  ticksEjeUtilidades: EjeTick[] = [];
  maximoEjeUtilidades: number = 0;

  ngOnInit(): void {
    this.cargarDashboard();
  }

  seleccionarModo(modo: ModoFiltro): void {
    if (this.modoSeleccionado === modo) {
      return;
    }

    this.modoSeleccionado = modo;
    this.error = null;
    this.reiniciarPaginacion();
    this.cargarDashboard();
  }

  aplicarFiltro(): void {
    this.reiniciarPaginacion();
    this.cargarDashboard();
  }

  reintentar(): void {
    this.cargarDashboard();
  }

  get rangoPeriodo(): string {
    if (!this.dashboard) {
      return '';
    }

    const desde = this.formatearFecha(this.dashboard.desde);
    const hasta = this.formatearFecha(this.dashboard.hasta);
    return `${desde} - ${hasta}`;
  }

  get sinDatosDashboard(): boolean {
    if (!this.dashboard) {
      return false;
    }

    return (
      this.dashboard.totalUtilidades === 0 &&
      this.dashboard.totalCajasCerradas === 0 &&
      this.dashboard.totalRetiros === 0 &&
      this.barrasUtilidades.length === 0 &&
      this.barrasTopProductos.length === 0 &&
      this.dashboard.historialCajasCerradas.length === 0 &&
      this.dashboard.historialRetiros.length === 0
    );
  }

  formatearMoneda(valor: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(valor);
  }

  formatearFecha(fechaIso: string): string {
    const fecha = this.parseFecha(fechaIso);
    if (!fecha) {
      return '-';
    }

    return fecha.toLocaleDateString('es-CO', {
      day: '2-digit',
      month: 'short',
      year: 'numeric',
    });
  }

  formatearFechaDiaAnio(fechaIso: string): string {
    const fecha = this.parseFecha(fechaIso);
    if (!fecha) {
      return '-';
    }

    return fecha.toLocaleDateString('es-CO', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    });
  }

  formatearFechaHora(fechaIso: string): string {
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

  textoCaja(cajaId: number | null): string {
    if (cajaId === null) {
      return 'Sin caja';
    }
    return `#${cajaId}`;
  }

  trackByCajaId(_: number, caja: HistorialCajaCerrada): number {
    return caja.cajaId;
  }

  trackByRetiroId(_: number, retiro: HistorialRetiro): number {
    return retiro.retiroId;
  }

  irPaginaAnteriorCajas(): void {
    if (this.loading || !this.dashboard?.paginacionCajasCerradas?.hasPrevious) {
      return;
    }
    this.paginaCajas = Math.max(0, this.paginaCajas - 1);
    this.cargarDashboard();
  }

  irPaginaSiguienteCajas(): void {
    if (this.loading || !this.dashboard?.paginacionCajasCerradas?.hasNext) {
      return;
    }
    this.paginaCajas += 1;
    this.cargarDashboard();
  }

  irPaginaAnteriorRetiros(): void {
    if (this.loading || !this.dashboard?.paginacionRetiros?.hasPrevious) {
      return;
    }
    this.paginaRetiros = Math.max(0, this.paginaRetiros - 1);
    this.cargarDashboard();
  }

  irPaginaSiguienteRetiros(): void {
    if (this.loading || !this.dashboard?.paginacionRetiros?.hasNext) {
      return;
    }
    this.paginaRetiros += 1;
    this.cargarDashboard();
  }

  get resumenPaginacionCajas(): string {
    const paginacion = this.dashboard?.paginacionCajasCerradas;
    if (!paginacion || paginacion.totalElements === 0) {
      return '0 resultados';
    }

    const inicio = paginacion.page * paginacion.size + 1;
    const fin = Math.min((paginacion.page + 1) * paginacion.size, paginacion.totalElements);
    return `${inicio}-${fin} de ${paginacion.totalElements}`;
  }

  get resumenPaginacionRetiros(): string {
    const paginacion = this.dashboard?.paginacionRetiros;
    if (!paginacion || paginacion.totalElements === 0) {
      return '0 resultados';
    }

    const inicio = paginacion.page * paginacion.size + 1;
    const fin = Math.min((paginacion.page + 1) * paginacion.size, paginacion.totalElements);
    return `${inicio}-${fin} de ${paginacion.totalElements}`;
  }

  private cargarDashboard(): void {
    const validacion = this.validarFiltro();
    if (!validacion.esValido) {
      this.error = validacion.mensaje;
      this.dashboard = null;
      this.barrasUtilidades = [];
      this.barrasTopProductos = [];
      this.ticksEjeUtilidades = [];
      this.maximoEjeUtilidades = 0;
      this.refrescarVista();
      return;
    }

    this.loading = true;
    this.error = null;
    this.refrescarVista();

    this.obtenerDashboardSegunModo()
      .pipe(
        finalize(() => {
          this.loading = false;
          this.refrescarVista();
        })
      )
      .subscribe({
        next: (response) => {
          this.dashboard = this.normalizarDashboard(response);
          this.actualizarEjeUtilidades(this.dashboard.graficoUtilidades.values ?? []);
          this.barrasUtilidades = this.construirBarrasUtilidades(this.dashboard);
          this.barrasTopProductos = this.construirBarrasTopProductos(this.dashboard);
          this.refrescarVista();
        },
        error: (error: unknown) => {
          this.mostrarError(
            'No se pudieron cargar las estadísticas del dashboard',
            'Verifica tu conexión e intenta nuevamente.',
            error
          );
          this.error =
            'No fue posible cargar las estadisticas. Verifica tu conexion e intenta nuevamente.';
          this.dashboard = null;
          this.barrasUtilidades = [];
          this.barrasTopProductos = [];
          this.ticksEjeUtilidades = [];
          this.maximoEjeUtilidades = 0;
          this.refrescarVista();
        },
      });
  }

  private obtenerDashboardSegunModo(): Observable<EstadisticasDashboard> {
    if (this.modoSeleccionado === 'semana') {
      return this.estadisticasService.obtenerDashboardSemana(
        this.fechaSemana,
        5,
        this.tamanioRetiros,
        this.paginaCajas,
        this.tamanioCajas,
        this.paginaRetiros,
        this.tamanioRetiros
      );
    }

    if (this.modoSeleccionado === 'mes') {
      return this.estadisticasService.obtenerDashboardMes(
        this.anioMes,
        5,
        this.tamanioRetiros,
        this.paginaCajas,
        this.tamanioCajas,
        this.paginaRetiros,
        this.tamanioRetiros
      );
    }

    return this.estadisticasService.obtenerDashboardAnio(
      this.anioDesde,
      this.anioHasta,
      5,
      this.tamanioRetiros,
      this.paginaCajas,
      this.tamanioCajas,
      this.paginaRetiros,
      this.tamanioRetiros
    );
  }

  private validarFiltro(): ValidacionFiltro {
    if (this.modoSeleccionado === 'semana') {
      if (!this.fechaSemana) {
        return { esValido: false, mensaje: 'Selecciona una fecha para consultar la semana.' };
      }
      return { esValido: true, mensaje: null };
    }

    if (this.modoSeleccionado === 'mes') {
      if (!this.esAnioValido(this.anioMes)) {
        return { esValido: false, mensaje: 'Ingresa un año valido para la consulta mensual.' };
      }
      return { esValido: true, mensaje: null };
    }

    if (!this.esAnioValido(this.anioDesde) || !this.esAnioValido(this.anioHasta)) {
      return { esValido: false, mensaje: 'Ingresa años validos para el rango anual.' };
    }

    if (this.anioDesde > this.anioHasta) {
      return {
        esValido: false,
        mensaje: 'El año desde no puede ser mayor al año hasta.',
      };
    }

    return { esValido: true, mensaje: null };
  }

  private construirBarrasUtilidades(data: EstadisticasDashboard): BarraUtilidad[] {
    const labels = data.graficoUtilidades.labels ?? [];
    const values = data.graficoUtilidades.values ?? [];
    const totalItems = Math.max(labels.length, values.length);

    if (totalItems === 0) {
      return [];
    }

    const maximo = this.maximoEjeUtilidades > 0 ? this.maximoEjeUtilidades : 1;

    return Array.from({ length: totalItems }, (_, index) => {
      const value = values[index] ?? 0;
      const porcentajeBase = maximo > 0 ? (value / maximo) * 100 : 0;
      const heightPercent = value > 0 ? Math.max(porcentajeBase, 4) : 0;

      return {
        label: labels[index] ?? `P${index + 1}`,
        value,
        heightPercent,
      };
    });
  }

  private construirBarrasTopProductos(data: EstadisticasDashboard): BarraTopProducto[] {
    const labels = data.graficoTopProductos.labels ?? [];
    const values = data.graficoTopProductos.values ?? [];
    const totalItems = Math.min(Math.max(labels.length, values.length), 5);

    if (totalItems === 0) {
      return [];
    }

    const maximo = values.reduce((acumulado, valor) => Math.max(acumulado, valor), 0);

    return Array.from({ length: totalItems }, (_, index) => {
      const value = values[index] ?? 0;
      const porcentajeBase = maximo > 0 ? (value / maximo) * 100 : 0;
      const widthPercent = value > 0 ? Math.max(porcentajeBase, 6) : 0;

      return {
        label: labels[index] ?? `Producto ${index + 1}`,
        value,
        widthPercent,
      };
    });
  }

  private normalizarDashboard(data: EstadisticasDashboard): EstadisticasDashboard {
    const graficoUtilidadesSource = data.graficoUtilidades;
    const graficoUtilidades: ResumenEstadisticas = {
      modo: graficoUtilidadesSource?.modo ?? data.modo,
      desde: graficoUtilidadesSource?.desde ?? data.desde,
      hasta: graficoUtilidadesSource?.hasta ?? data.hasta,
      labels: graficoUtilidadesSource?.labels ?? [],
      values: graficoUtilidadesSource?.values ?? [],
      total: graficoUtilidadesSource?.total ?? 0,
    };

    const graficoTopProductosSource = data.graficoTopProductos;
    const graficoTopProductos: TopProductosVendidos = {
      modo: graficoTopProductosSource?.modo ?? data.modo,
      desde: graficoTopProductosSource?.desde ?? data.desde,
      hasta: graficoTopProductosSource?.hasta ?? data.hasta,
      labels: graficoTopProductosSource?.labels ?? [],
      values: graficoTopProductosSource?.values ?? [],
      totalVendidos: graficoTopProductosSource?.totalVendidos ?? 0,
      productos: graficoTopProductosSource?.productos ?? [],
    };

    const paginacionCajasCerradas = this.normalizarPaginacion(
      data.paginacionCajasCerradas,
      this.tamanioCajas,
      data.historialCajasCerradas?.length ?? 0
    );

    const paginacionRetiros = this.normalizarPaginacion(
      data.paginacionRetiros,
      this.tamanioRetiros,
      data.historialRetiros?.length ?? 0
    );

    const historialCajasCerradas = (data.historialCajasCerradas ?? []).map((caja) => ({
      ...caja,
      fechaApertura: caja.fechaApertura ?? caja.horaApertura ?? '',
      fechaCierre: caja.fechaCierre ?? caja.horaCierre ?? '',
    }));

    return {
      ...data,
      graficoUtilidades,
      graficoTopProductos,
      historialCajasCerradas,
      historialRetiros: data.historialRetiros ?? [],
      paginacionCajasCerradas,
      paginacionRetiros,
    };
  }

  private normalizarPaginacion(
    metadata: PaginacionMetadata | null | undefined,
    tamanioDefault: number,
    totalElementosFallback: number
  ): PaginacionMetadata {
    if (metadata) {
      return {
        page: metadata.page ?? 0,
        size: metadata.size ?? tamanioDefault,
        totalElements: metadata.totalElements ?? totalElementosFallback,
        totalPages: metadata.totalPages ?? 1,
        first: metadata.first ?? true,
        last: metadata.last ?? true,
        hasNext: metadata.hasNext ?? false,
        hasPrevious: metadata.hasPrevious ?? false,
      };
    }

    return {
      page: 0,
      size: tamanioDefault,
      totalElements: totalElementosFallback,
      totalPages: totalElementosFallback > 0 ? 1 : 0,
      first: true,
      last: true,
      hasNext: false,
      hasPrevious: false,
    };
  }

  private reiniciarPaginacion(): void {
    this.paginaCajas = 0;
    this.paginaRetiros = 0;
  }

  private esAnioValido(anio: number): boolean {
    return Number.isInteger(anio) && anio >= 2000 && anio <= this.anioActual + 1;
  }

  private actualizarEjeUtilidades(values: number[]): void {
    const maximoSerie = values.reduce((acumulado, valor) => Math.max(acumulado, valor), 0);
    if (maximoSerie <= 0) {
      this.maximoEjeUtilidades = 400;
      this.ticksEjeUtilidades = this.construirTicksEje(100, this.maximoEjeUtilidades);
      return;
    }

    const intervalo = this.calcularIntervaloBonito(maximoSerie);
    const cantidadIntervalos = Math.max(4, Math.ceil(maximoSerie / intervalo));
    this.maximoEjeUtilidades = cantidadIntervalos * intervalo;
    this.ticksEjeUtilidades = this.construirTicksEje(intervalo, this.maximoEjeUtilidades);
  }

  private construirTicksEje(intervalo: number, maximoEje: number): EjeTick[] {
    const ticks: EjeTick[] = [];

    for (let valor = 0; valor <= maximoEje; valor += intervalo) {
      ticks.push({
        value: valor,
        label: this.formatearNumeroEje(valor),
        bottomPercent: maximoEje > 0 ? (valor / maximoEje) * 100 : 0,
      });
    }

    return ticks;
  }

  private formatearNumeroEje(valor: number): string {
    const esEntero = Number.isInteger(valor);
    return new Intl.NumberFormat('es-CO', {
      maximumFractionDigits: esEntero ? 0 : 1,
    }).format(valor);
  }

  private calcularIntervaloBonito(maximo: number): number {
    const intervaloCrudo = maximo / 4;
    const magnitud = 10 ** Math.floor(Math.log10(intervaloCrudo));
    const normalizado = intervaloCrudo / magnitud;

    let factor = 1;
    if (normalizado > 1 && normalizado <= 2) {
      factor = 2;
    } else if (normalizado > 2 && normalizado <= 2.5) {
      factor = 2.5;
    } else if (normalizado > 2.5 && normalizado <= 5) {
      factor = 5;
    } else if (normalizado > 5) {
      factor = 10;
    }

    return factor * magnitud;
  }

  private obtenerFechaHoyISO(): string {
    const hoy = new Date();
    const anio = hoy.getFullYear();
    const mes = String(hoy.getMonth() + 1).padStart(2, '0');
    const dia = String(hoy.getDate()).padStart(2, '0');
    return `${anio}-${mes}-${dia}`;
  }

  private refrescarVista(): void {
    queueMicrotask(() => {
      this.cdr.detectChanges();
      this.appRef.tick();
    });
  }

  private parseFecha(valor: string): Date | null {
    if (!valor) {
      return null;
    }

    // Para valores "YYYY-MM-DD" (LocalDate) evitar conversión UTC que resta un día en GMT-5.
    const matchSoloFecha = /^(\d{4})-(\d{2})-(\d{2})$/.exec(valor);
    if (matchSoloFecha) {
      const anio = Number(matchSoloFecha[1]);
      const mes = Number(matchSoloFecha[2]);
      const dia = Number(matchSoloFecha[3]);
      const fechaLocal = new Date(anio, mes - 1, dia);
      return Number.isNaN(fechaLocal.getTime()) ? null : fechaLocal;
    }

    const fecha = new Date(valor);
    return Number.isNaN(fecha.getTime()) ? null : fecha;
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
