import { Injectable } from "@angular/core";
import { Movimiento } from "../models/movimiento.model";
import { CajaResumen } from "../models/cajaResumen.model";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from 'src/environments/environment';
import { CrearEgresoRequest } from "../models/crearEgresoRequest.model";

@Injectable({ providedIn: 'root' })

export class MovimientosService {
    private baseUrl = `${environment.apiUrl}/movimientos`;
    private cajaUrl = `${environment.apiUrl}/caja`;

    constructor(private http: HttpClient) {}

     obtenerMovimientos(): Observable<Movimiento[]> { 
        return this.http.get<Movimiento[]>(`${this.baseUrl}/abierta`);
    }

    obtenerResumenCaja(): Observable<CajaResumen> {
        return this.http.get<CajaResumen>(`${this.cajaUrl}/resumen`);
    }

    crearEgreso(crearEgresoRequest: CrearEgresoRequest): Observable<Movimiento> {
        return this.http.post<Movimiento>(`${this.baseUrl}/egreso`, crearEgresoRequest);
    }



}

