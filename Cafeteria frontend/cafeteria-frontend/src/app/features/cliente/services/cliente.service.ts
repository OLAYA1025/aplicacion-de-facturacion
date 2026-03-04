import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Cliente } from 'src/app/core/models/cliente.model';
import { environment } from 'src/environments/environment';
import { ActualizarClienteRequest } from '../models/actualizarCliente.model';

type TipoIdentificacionCliente = Cliente['tipoIdentificacion'];

@Injectable({
  providedIn: 'root',
})
export class ClienteService {
  private readonly baseURL = `${environment.apiUrl}/cliente`;

  constructor(private readonly http: HttpClient) {}

  obtenerPorIdentificacion(
    tipoIdentificacion: TipoIdentificacionCliente,
    numeroIdentificacion: string
  ): Observable<Cliente> {
    return this.http.get<Cliente>(
      `${this.baseURL}/${encodeURIComponent(tipoIdentificacion)}/${encodeURIComponent(numeroIdentificacion)}`
    );
  }

  editarPorIdentificacion(
    tipoIdentificacion: TipoIdentificacionCliente,
    numeroIdentificacion: string,
    actualizarCliente: ActualizarClienteRequest
  ): Observable<Cliente> {
    return this.http.put<Cliente>(
      `${this.baseURL}/${encodeURIComponent(tipoIdentificacion)}/${encodeURIComponent(numeroIdentificacion)}`,
      actualizarCliente
    );
  }
}
