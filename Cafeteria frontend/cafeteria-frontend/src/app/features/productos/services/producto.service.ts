import { Injectable } from "@angular/core";
import { environment } from 'src/environments/environment';
import { HttpClient } from "@angular/common/http";
import { CrearProducto } from "../models/crearProducto.model";
import { Producto }  from "../../../core/models/producto.model";
import { actualizarProducto } from "../models/actualizarProducto.model";
import { Observable } from "rxjs";

@Injectable({
    providedIn: 'root'
})
export class ProductoService {
    private baseURL = environment.apiUrl + '/producto';

    constructor(private http: HttpClient) { }

    crearProducto(crearProducto: CrearProducto): Observable<Producto> {
        return this.http.post<Producto>(this.baseURL, crearProducto);
    }

    obtenerProducto(id: number): Observable<Producto> {
        return this.http.get<Producto>(this.baseURL + '/' + id);
    }

    obtenerProductos(): Observable<Producto[]> {
        return this.http.get<Producto[]>(this.baseURL);
    }

    obtenerProductosInventariables(): Observable<Producto[]> {
        return this.http.get<Producto[]>(this.baseURL + '/inventariable');
    }

    obtenerProductosIntentables(): Observable<Producto[]> {
        return this.obtenerProductosInventariables();
    }

    obtenerProductosPreparacion(): Observable<Producto[]> {
        return this.http.get<Producto[]>(this.baseURL + '/preparacion');
    }

    obtenerProductosPorCategoria(idCategoria: number): Observable<Producto[]> {
        return this.http.get<Producto[]>(this.baseURL + '/categoria/' + idCategoria);
    }

    activarProducto(id: number): Observable<Producto> {
        return this.http.post<Producto>(this.baseURL + '/' + id + '/activar', {});
    }

    desactivarProducto(id: number): Observable<Producto> {
        return this.http.post<Producto>(this.baseURL + '/' + id + '/desactivar', {});
    }

    actualizarProducto(id: number, actualizarProducto: actualizarProducto): Observable<Producto> {
        return this.http.put<Producto>(this.baseURL + '/' + id, actualizarProducto);
    }

}

