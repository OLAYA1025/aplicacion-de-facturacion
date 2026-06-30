export type ModoUtilidad = 'semana' | 'mes' | 'anio' | 'personalizado';

export interface ResumenEstadisticas {
  modo: ModoUtilidad;
  desde: string;
  hasta: string;
  labels: string[];
  values: number[];
  total: number;
}

export interface UtilidadSemanaQuery {
  modo: 'semana';
  fecha: string;
}

export interface UtilidadMesQuery {
  modo: 'mes';
  anio: number;
}

export interface UtilidadAnioQuery {
  modo: 'anio';
  desde: number;
  hasta: number;
}

export interface UtilidadPersonalizadaQuery {
  modo: 'personalizado';
  fechaDesde: string;
  fechaHasta: string;
}

export type UtilidadQuery = UtilidadSemanaQuery | UtilidadMesQuery | UtilidadAnioQuery | UtilidadPersonalizadaQuery;

export interface TopProductoVendido {
  productoId: number;
  productoNombre: string;
  cantidadVendida: number;
}

export interface TopProductosVendidos {
  modo: ModoUtilidad;
  desde: string;
  hasta: string;
  labels: string[];
  values: number[];
  totalVendidos: number;
  productos: TopProductoVendido[];
}

export interface HistorialCajaCerrada {
  cajaId: number;
  usuarioAperturaNombre: string;
  usuarioCierreNombre: string;
  fechaApertura: string;
  fechaCierre: string;
  horaApertura?: string;
  horaCierre?: string;
  fecha?: string;
  utilidad: number;
}

export interface HistorialRetiro {
  retiroId: number;
  cajaId: number | null;
  usuarioNombre: string;
  motivo: string;
  fecha: string;
  monto: number;
}

export interface PaginacionMetadata {
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  first: boolean;
  last: boolean;
  hasNext: boolean;
  hasPrevious: boolean;
}

export interface EstadisticasDashboard {
  modo: ModoUtilidad;
  desde: string;
  hasta: string;
  totalUtilidades: number;
  totalCajasCerradas: number;
  totalRetiros: number;
  totalEgresos: number;
  totalGastos: number;
  ingresosTransferencia: number;
  ingresosTarjeta: number;
  graficoUtilidades: ResumenEstadisticas;
  graficoTopProductos: TopProductosVendidos;
  historialCajasCerradas: HistorialCajaCerrada[];
  paginacionCajasCerradas: PaginacionMetadata;
  historialRetiros: HistorialRetiro[];
  paginacionRetiros: PaginacionMetadata;
}
