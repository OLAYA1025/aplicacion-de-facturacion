import { DetallePedidoNota } from './notas-predefinidas.model';

export interface DetallePedido {
  id: number;
  productoId: number;
  productoNombre: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
  notas: DetallePedidoNota[];
}
