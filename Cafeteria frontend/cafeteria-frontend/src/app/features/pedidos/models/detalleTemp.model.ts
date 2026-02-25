import { Producto } from '../../../core/models/producto.model';

export interface DetalleTemp {
  producto: Producto;
  cantidad: number;
}
