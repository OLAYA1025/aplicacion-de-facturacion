import { FacturaEstado } from './factura-estado.model';

export interface FacturaListadoParams {
  page?: number;
  size?: number;
  q?: string;
  estados?: FacturaEstado[];
  from?: string;
  to?: string;
  sort?: string;
}
