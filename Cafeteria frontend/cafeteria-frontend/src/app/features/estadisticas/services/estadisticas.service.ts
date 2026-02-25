import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import {
  EstadisticasDashboard,
  ResumenEstadisticas,
  TopProductosVendidos,
  UtilidadAnioQuery,
  UtilidadMesQuery,
  UtilidadQuery,
  UtilidadSemanaQuery,
} from '../models/resumen-estadisticas.model';

@Injectable({ providedIn: 'root' })
export class EstadisticasService {
  private readonly utilidadUrl = `${environment.apiUrl}/stats/utilidad`;
  private readonly topProductosUrl = `${environment.apiUrl}/stats/top-productos`;
  private readonly dashboardUrl = `${environment.apiUrl}/stats/dashboard`;

  constructor(private readonly http: HttpClient) {}

  obtenerUtilidadSemana(fecha: string): Observable<ResumenEstadisticas> {
    return this.obtenerUtilidad({ modo: 'semana', fecha });
  }

  obtenerUtilidadMes(anio: number): Observable<ResumenEstadisticas> {
    return this.obtenerUtilidad({ modo: 'mes', anio });
  }

  obtenerUtilidadAnio(desde: number, hasta: number): Observable<ResumenEstadisticas> {
    return this.obtenerUtilidad({ modo: 'anio', desde, hasta });
  }

  obtenerUtilidad(query: UtilidadQuery): Observable<ResumenEstadisticas> {
    let params = new HttpParams().set('modo', query.modo);

    switch (query.modo) {
      case 'semana':
        params = this.agregarParamsSemana(params, query);
        break;
      case 'mes':
        params = this.agregarParamsMes(params, query);
        break;
      case 'anio':
        params = this.agregarParamsAnio(params, query);
        break;
    }

    return this.http.get<ResumenEstadisticas>(this.utilidadUrl, { params });
  }

  obtenerTopProductosSemana(fecha: string, limite = 5): Observable<TopProductosVendidos> {
    return this.obtenerTopProductos({ modo: 'semana', fecha }, limite);
  }

  obtenerTopProductosMes(anio: number, limite = 5): Observable<TopProductosVendidos> {
    return this.obtenerTopProductos({ modo: 'mes', anio }, limite);
  }

  obtenerTopProductosAnio(desde: number, hasta: number, limite = 5): Observable<TopProductosVendidos> {
    return this.obtenerTopProductos({ modo: 'anio', desde, hasta }, limite);
  }

  obtenerTopProductos(query: UtilidadQuery, limite = 5): Observable<TopProductosVendidos> {
    let params = new HttpParams().set('modo', query.modo).set('limite', limite);

    switch (query.modo) {
      case 'semana':
        params = this.agregarParamsSemana(params, query);
        break;
      case 'mes':
        params = this.agregarParamsMes(params, query);
        break;
      case 'anio':
        params = this.agregarParamsAnio(params, query);
        break;
    }

    return this.http.get<TopProductosVendidos>(this.topProductosUrl, { params });
  }

  obtenerDashboardSemana(
    fecha: string,
    limiteTopProductos = 5,
    limiteRetiros = 20,
    paginaCajas = 0,
    tamanioCajas = 20,
    paginaRetiros = 0,
    tamanioRetiros = limiteRetiros
  ): Observable<EstadisticasDashboard> {
    return this.obtenerDashboard(
      { modo: 'semana', fecha },
      limiteTopProductos,
      limiteRetiros,
      paginaCajas,
      tamanioCajas,
      paginaRetiros,
      tamanioRetiros
    );
  }

  obtenerDashboardMes(
    anio: number,
    limiteTopProductos = 5,
    limiteRetiros = 20,
    paginaCajas = 0,
    tamanioCajas = 20,
    paginaRetiros = 0,
    tamanioRetiros = limiteRetiros
  ): Observable<EstadisticasDashboard> {
    return this.obtenerDashboard(
      { modo: 'mes', anio },
      limiteTopProductos,
      limiteRetiros,
      paginaCajas,
      tamanioCajas,
      paginaRetiros,
      tamanioRetiros
    );
  }

  obtenerDashboardAnio(
    desde: number,
    hasta: number,
    limiteTopProductos = 5,
    limiteRetiros = 20,
    paginaCajas = 0,
    tamanioCajas = 20,
    paginaRetiros = 0,
    tamanioRetiros = limiteRetiros
  ): Observable<EstadisticasDashboard> {
    return this.obtenerDashboard(
      { modo: 'anio', desde, hasta },
      limiteTopProductos,
      limiteRetiros,
      paginaCajas,
      tamanioCajas,
      paginaRetiros,
      tamanioRetiros
    );
  }

  obtenerDashboard(
    query: UtilidadQuery,
    limiteTopProductos = 5,
    limiteRetiros = 20,
    paginaCajas = 0,
    tamanioCajas = 20,
    paginaRetiros = 0,
    tamanioRetiros = limiteRetiros
  ): Observable<EstadisticasDashboard> {
    let params = new HttpParams()
      .set('modo', query.modo)
      .set('limiteTopProductos', limiteTopProductos)
      .set('limiteRetiros', limiteRetiros)
      .set('paginaCajas', paginaCajas)
      .set('tamanioCajas', tamanioCajas)
      .set('paginaRetiros', paginaRetiros)
      .set('tamanioRetiros', tamanioRetiros);

    switch (query.modo) {
      case 'semana':
        params = this.agregarParamsSemana(params, query);
        break;
      case 'mes':
        params = this.agregarParamsMes(params, query);
        break;
      case 'anio':
        params = this.agregarParamsAnio(params, query);
        break;
    }

    return this.http.get<EstadisticasDashboard>(this.dashboardUrl, { params });
  }

  private agregarParamsSemana(params: HttpParams, query: UtilidadSemanaQuery): HttpParams {
    return params.set('fecha', query.fecha);
  }

  private agregarParamsMes(params: HttpParams, query: UtilidadMesQuery): HttpParams {
    return params.set('anio', query.anio);
  }

  private agregarParamsAnio(params: HttpParams, query: UtilidadAnioQuery): HttpParams {
    return params.set('desde', query.desde).set('hasta', query.hasta);
  }
}

