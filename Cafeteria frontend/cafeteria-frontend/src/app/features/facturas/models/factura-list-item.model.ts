import { FacturaEstado } from './factura-estado.model';

export interface FacturaListItem {
  id: number;
  pedidoId: number;
  tipoDocumentoFiscal: string;
  estado: FacturaEstado;
  numeroDocumento: string | null;
  cufe: string | null;
  referenceCode: string | null;
  createdAt: string;
  updatedAt: string | null;
  aceptadoAt: string | null;
  clienteNombre: string | null;
  clienteIdentificacion: string | null;
  total: number | string | null;
}
