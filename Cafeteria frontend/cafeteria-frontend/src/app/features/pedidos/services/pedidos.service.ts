import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { CrearPedidoRequest } from "../models/crearPedido.model";
import { environment } from "src/environments/environment";
import { Pedido } from "../../../core/models/pedido.model";
import { Observable } from "rxjs";
import { ActualizarPedidoRequest } from "../models/actualizarPedido.model";
import { PedidoUsuarioRequest } from "../models/pedidoUsuario.model";

@Injectable({ providedIn: 'root' })
export class PedidosService {
    constructor(private http: HttpClient) { }

    private baseURL = environment.apiUrl + '/pedido';
    //aasds
    crearPedido(crearPedido :CrearPedidoRequest): Observable<Pedido> {
        return this.http.post<Pedido>(this.baseURL, crearPedido);
    }

    obtenerPedido(id: number) : Observable<Pedido>{
        return this.http.get<Pedido>(this.baseURL + '/' + id);
    }

    obtenerPedidosCajaAbierta() : Observable<Pedido[]>{
        return this.http.get<Pedido[]>(this.baseURL + '/caja/abierta');
    }

    obtenerPedidosCaja(idCaja: number) : Observable<Pedido[]>{
        return this.http.get<Pedido[]>(this.baseURL + '/caja/' + idCaja);
    }

    actualizarPedido(id: number, actualizarPedido: ActualizarPedidoRequest): Observable<Pedido> {
        return this.http.put<Pedido>(this.baseURL + '/' + id, actualizarPedido);
    }

    cobrarPedido(id: number, pedidoUsuario: PedidoUsuarioRequest): Observable<Pedido> {
        return this.http.post<Pedido>(this.baseURL + '/' + id + '/cobrar', pedidoUsuario);
    }

    cancelarPedido(id: number, pedidoUsuario: PedidoUsuarioRequest): Observable<Pedido> {
        return this.http.post<Pedido>(this.baseURL + '/' + id + '/cancelar', pedidoUsuario);
    }
}