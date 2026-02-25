import { Injectable } from "@angular/core";
import { HttpClient, HttpParams } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from 'src/environments/environment';
import { CompraRequest } from "../models/crearCompra.model";
import { CompraResponse } from "../models/compraResponse.model";
import { CompraResumenResponse } from "../models/compraResumenResponse.model";
import { CompraListadoParams } from "../models/compraListadoParams.model";
import { PageResponse } from "../models/pageResponse.model";

@Injectable({
    providedIn: 'root'
})
export class CompraService {

    private baseUrl = environment.apiUrl + '/compra';

    constructor(private http: HttpClient) {}

    crearCompra(compraRequest: CompraRequest): Observable<CompraResponse> {
        return this.http.post<CompraResponse>(this.baseUrl, compraRequest);
    }

    obtenerCompra(id: number): Observable<CompraResponse> {
        return this.http.get<CompraResponse>(this.baseUrl + '/' + id);
    }

    obtenerCompras(params: CompraListadoParams = {}): Observable<PageResponse<CompraResumenResponse>> {
        let httpParams = new HttpParams()
            .set('page', params.page ?? 0)
            .set('size', params.size ?? 20)
            .set('ordenarPor', params.ordenarPor ?? 'id')
            .set('direccion', params.direccion ?? 'desc');

        if (params.estado) {
            httpParams = httpParams.set('estado', params.estado);
        }

        if (params.desde) {
            httpParams = httpParams.set('desde', params.desde);
        }
        if (params.hasta) {
            httpParams = httpParams.set('hasta', params.hasta);
        }

        return this.http.get<PageResponse<CompraResumenResponse>>(this.baseUrl, { params: httpParams });
    }

    confirmarCompra(id: number): Observable<CompraResponse> {
        return this.http.post<CompraResponse>(this.baseUrl + '/' + id + '/confirmar', {});
    }

    anularCompra(id: number): Observable<CompraResponse> {
        return this.http.post<CompraResponse>(this.baseUrl + '/' + id + '/anular', {});
    }
}

