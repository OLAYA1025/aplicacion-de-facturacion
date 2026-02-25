import { DetallePedido } from "./detallePedido.model";

export interface Pedido{
    id: number;
    nombre: string;
    fecha: string;
    estado: 'PENDIENTE' | 'PAGO' | 'CANCELADO';
    metodoPago: 'EFECTIVO' | 'TARJETA' | 'TRANSFERENCIA' | null;
    usuarioId: number;
    usuarioUsername: string;
    total: number;
    detalles: DetallePedido[]

}