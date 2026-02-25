import { DetallePedidoRequest } from "./detallePedido.model";

export interface CrearPedidoRequest {
    nombre: string;
    detalles: DetallePedidoRequest[]; 
}