import { Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Caja } from '../models/caja.model';
import { CajaResumen } from '../models/cajaResumen.model';
import { environment } from 'src/environments/environment';



@Injectable({ providedIn: 'root' })
export class CajaService {

  private baseUrl = `${environment.apiUrl}/caja`;

  private cajaActivaSignal = signal<Caja | null>(null);

  constructor(private http: HttpClient) {}

  obtenerCajaAbierta() {
    return this.http.get<Caja>(`${this.baseUrl}/abierta`);
  }

  setCajaActiva(caja: Caja | null) {
    this.cajaActivaSignal.set(caja);
  }

  get cajaActiva() {
    return this.cajaActivaSignal.asReadonly();
  }

  abrirCaja(usuarioAperturaId: number) {
    return this.http.post<Caja>(`${this.baseUrl}/abrir`, {
      usuarioAperturaId
    });
  }

  cerrarCaja(idUsuarioCierre: number, retiro: number) {
    return this.http.post<Caja>(`${this.baseUrl}/cerrar`, {
      idUsuarioCierre,
      retiro
    });
  }

  resumenCaja(){
    return this.http.get<CajaResumen>(`${this.baseUrl}/resumen`);
  }

}
