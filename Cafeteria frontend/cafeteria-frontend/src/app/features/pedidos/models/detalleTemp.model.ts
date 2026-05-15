import { Producto } from '../../../core/models/producto.model';
import { DetallePedidoNota } from '../../../core/models/notas-predefinidas.model';

export interface DetalleTemp {
  producto: Producto;
  cantidad: number;
  notas: DetallePedidoNota[];
}
