import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { CrearNegocioPlataformaRequestDTO } from '../models/crear-negocio-plataforma-request.dto';
import {
  ConfiguracionFacturacionElectronicaResponseDTO,
  CrearConfiguracionFacturacionElectronicaRequestDTO,
  CrearNumberingRangeRequestDTO,
  EditarConfiguracionFacturacionElectronicaRequestDTO,
  EditarNumberingRangeRequestDTO,
} from '../models/facturacion-electronica-plataforma.dto';
import { NegocioPlataformaResponseDTO } from '../models/negocio-plataforma-response.dto';
import type { ConfigurarLogoRequestDTO } from '../models/configurar-logo-plataforma-request.dto';

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

  obtenerConfiguracionFacturacionElectronica(
    negocioId: number | string
  ): Observable<ConfiguracionFacturacionElectronicaResponseDTO | null> {
    return this.http.get<ConfiguracionFacturacionElectronicaResponseDTO | null>(
      `${this.baseUrl}/${negocioId}/facturacion-electronica`
    );
  }

  crearConfiguracionFacturacionElectronica(
    data: CrearConfiguracionFacturacionElectronicaRequestDTO
  ): Observable<number> {
    return this.http.post<number>(`${this.baseUrl}/facturacion-electronica`, data);
  }

  editarConfiguracionFacturacionElectronica(
    configuracionId: number | string,
    data: EditarConfiguracionFacturacionElectronicaRequestDTO
  ): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/facturacion-electronica/${configuracionId}`, data);
  }

  crearNumberingRange(data: CrearNumberingRangeRequestDTO): Observable<number> {
    return this.http.post<number>(`${this.baseUrl}/numbering-ranges`, data);
  }

  editarNumberingRange(
    rangeId: number | string,
    data: EditarNumberingRangeRequestDTO
  ): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/numbering-ranges/${rangeId}`, data);
  }

  configurarLogoNegocio(
    negocioId: number | string,
    data: ConfigurarLogoRequestDTO
  ): Observable<void> {
    return this.http.put<void>(`${this.baseUrl}/${negocioId}/logo`, data);
  }
}
