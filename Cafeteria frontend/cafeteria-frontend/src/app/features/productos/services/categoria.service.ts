import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Categoria, CrearCategoriaRequest } from '../models/categoria.model';
import { GrupoNotaCategoria, NotaPredefinida, TipoSeleccionNota } from '../../../core/models/notas-predefinidas.model';

export interface GuardarGrupoNotaCategoriaRequest {
  nombre: string;
  obligatorio: boolean;
  tipoSeleccion: TipoSeleccionNota;
}

export interface GuardarNotaPredefinidaRequest {
  descripcion: string;
}

@Injectable({
  providedIn: 'root',
})
export class CategoriaService {
  private readonly baseURL = `${environment.apiUrl}/categoria`;

  constructor(private readonly http: HttpClient) {}

  obtenerCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.baseURL);
  }

  crearCategoria(crearCategoria: CrearCategoriaRequest): Observable<Categoria> {
    return this.http.post<Categoria>(this.baseURL, crearCategoria);
  }

  actualizarCategoria(categoriaId: number, request: CrearCategoriaRequest): Observable<Categoria> {
    return this.http.put<Categoria>(`${this.baseURL}/${categoriaId}`, request);
  }

  obtenerGruposNotasPorCategoria(categoriaId: number): Observable<GrupoNotaCategoria[]> {
    return this.http.get<GrupoNotaCategoria[]>(`${this.baseURL}/${categoriaId}/grupos-notas/detalle`);
  }

  crearGrupoNotaCategoria(categoriaId: number, request: GuardarGrupoNotaCategoriaRequest): Observable<GrupoNotaCategoria> {
    return this.http.post<GrupoNotaCategoria>(`${this.baseURL}/${categoriaId}/grupos-notas`, request);
  }

  actualizarGrupoNotaCategoria(
    categoriaId: number,
    grupoId: number,
    request: GuardarGrupoNotaCategoriaRequest
  ): Observable<GrupoNotaCategoria> {
    return this.http.put<GrupoNotaCategoria>(`${this.baseURL}/${categoriaId}/grupos-notas/${grupoId}`, request);
  }

  eliminarGrupoNotaCategoria(categoriaId: number, grupoId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseURL}/${categoriaId}/grupos-notas/${grupoId}`);
  }

  crearNotaPredefinida(
    categoriaId: number,
    grupoId: number,
    request: GuardarNotaPredefinidaRequest
  ): Observable<NotaPredefinida> {
    return this.http.post<NotaPredefinida>(`${this.baseURL}/${categoriaId}/grupos-notas/${grupoId}/notas`, request);
  }

  actualizarNotaPredefinida(
    categoriaId: number,
    grupoId: number,
    notaId: number,
    request: GuardarNotaPredefinidaRequest
  ): Observable<NotaPredefinida> {
    return this.http.put<NotaPredefinida>(
      `${this.baseURL}/${categoriaId}/grupos-notas/${grupoId}/notas/${notaId}`,
      request
    );
  }

  eliminarNotaPredefinida(categoriaId: number, grupoId: number, notaId: number): Observable<void> {
    return this.http.delete<void>(`${this.baseURL}/${categoriaId}/grupos-notas/${grupoId}/notas/${notaId}`);
  }
}
