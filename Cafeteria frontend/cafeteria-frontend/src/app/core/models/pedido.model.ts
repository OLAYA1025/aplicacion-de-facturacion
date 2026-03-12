import { DetallePedido } from "./detallePedido.model";

export interface Pedido{
    id: number;
    nombre: string;
    fecha: string;
    estado: 'PENDIENTE' | 'PAGO' | 'CANCELADO';
    metodoPago: 'EFECTIVO' | 'TRANSFERENCIA' | 'TARJETA_CREDITO' | 'TARJETA_DEBITO' | null;
    usuarioId: number;
    usuarioUsername: string;
    total: number;
    detalles: DetallePedido[]

}
