import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { UsuarioResponse } from '../models/usuario.model';
import { ActualizarUsuarioRequest } from '../models/actualizarUsuarioRequest.model';
import { CambiarPasswordUsuarioRequest } from '../models/cambiarPasswordUsuarioRequest.model';
import { CrearUsuarioRequest } from '../models/crearUsuarioRequest.model';

@Injectable({
  providedIn: 'root',
})
export class UsuarioService {
  private readonly baseUrl = `${environment.apiUrl}/usuario`;

  constructor(private readonly http: HttpClient) {}

  obtenerUsuario(id: number): Observable<UsuarioResponse> {
    return this.http.get<UsuarioResponse>(`${this.baseUrl}/${id}`);
  }

  listarUsuarios(): Observable<UsuarioResponse[]> {
    return this.http.get<UsuarioResponse[]>(this.baseUrl);
  }

  crearUsuario(request: CrearUsuarioRequest): Observable<UsuarioResponse> {
    return this.http.post<UsuarioResponse>(this.baseUrl, request);
  }

  actualizarUsuario(
    id: number,
    actualizarUsuarioRequest: ActualizarUsuarioRequest,
  ): Observable<UsuarioResponse> {
    return this.http.put<UsuarioResponse>(
      `${this.baseUrl}/${id}`,
      actualizarUsuarioRequest,
    );
  }

  cambiarPassword(
    id: number,
    cambiarPasswordRequest: CambiarPasswordUsuarioRequest,
  ): Observable<UsuarioResponse> {
    return this.http.put<UsuarioResponse>(
      `${this.baseUrl}/${id}/password`,
      cambiarPasswordRequest,
    );
  }

  activarUsuario(id: number): Observable<UsuarioResponse> {
    return this.http.post<UsuarioResponse>(`${this.baseUrl}/${id}/activar`, {});
  }

  desactivarUsuario(id: number): Observable<UsuarioResponse> {
    return this.http.post<UsuarioResponse>(`${this.baseUrl}/${id}/desactivar`, {});
  }
}

