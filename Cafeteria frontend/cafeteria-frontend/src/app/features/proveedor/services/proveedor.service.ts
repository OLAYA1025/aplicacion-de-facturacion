import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";
import { environment } from "src/environments/environment";
import { Proveedor } from "../models/proveedor.model";
import { CrearProveedorRequest } from "../models/crearProveedor.model";
import { ActualizarProveedorRequest } from "../models/actualizarProveedor.model";

@Injectable({
    providedIn: 'root'
})
export class ProveedorService {
    private baseURL = environment.apiUrl + '/proveedor';

    constructor(private http: HttpClient) {}

    crearProveedor(crearProveedorRequest: CrearProveedorRequest): Observable<Proveedor> {
        return this.http.post<Proveedor>(this.baseURL, crearProveedorRequest);
    }

    editarProveedor(id: number, actualizarProveedorRequest: ActualizarProveedorRequest): Observable<Proveedor> {
        return this.http.put<Proveedor>(this.baseURL + '/' + id, actualizarProveedorRequest);
    }

    obtenerProveedor(id: number): Observable<Proveedor> {
        return this.http.get<Proveedor>(this.baseURL + '/' + id);
    }

    obtenerProveedores(): Observable<Proveedor[]> {
        return this.http.get<Proveedor[]>(this.baseURL);
    }
}
