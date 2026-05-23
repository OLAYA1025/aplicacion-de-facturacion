import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { CrearNegocioPlataformaRequestDTO } from '../models/crear-negocio-plataforma-request.dto';
import { NegocioPlataformaResponseDTO } from '../models/negocio-plataforma-response.dto';

@Injectable({
  providedIn: 'root',
})
export class PlatformNegocioService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiUrl}/platform/negocios`;

  listarNegocios(): Observable<NegocioPlataformaResponseDTO[]> {
    return this.http.get<NegocioPlataformaResponseDTO[]>(this.baseUrl);
  }

  crearNegocio(
    data: CrearNegocioPlataformaRequestDTO
  ): Observable<NegocioPlataformaResponseDTO> {
    return this.http.post<NegocioPlataformaResponseDTO>(this.baseUrl, data);
  }

  activarNegocio(id: number | string): Observable<NegocioPlataformaResponseDTO> {
    return this.http.put<NegocioPlataformaResponseDTO>(`${this.baseUrl}/${id}/activar`, {});
  }

  desactivarNegocio(id: number | string): Observable<NegocioPlataformaResponseDTO> {
    return this.http.put<NegocioPlataformaResponseDTO>(`${this.baseUrl}/${id}/desactivar`, {});
  }
}
