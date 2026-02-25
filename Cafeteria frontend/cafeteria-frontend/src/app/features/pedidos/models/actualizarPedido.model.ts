import { DetallePedidoRequest } from "./detallePedido.model";

export interface ActualizarPedidoRequest {
    nombre: string;
    detalles: DetallePedidoRequest[];
}