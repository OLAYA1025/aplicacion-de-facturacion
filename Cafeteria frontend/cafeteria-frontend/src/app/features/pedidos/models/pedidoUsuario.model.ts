import { Cliente } from "src/app/core/models/cliente.model";

export type MetodoPago = 'EFECTIVO' | 'TARJETA' | 'TRANSFERENCIA';
export type TipoDocumentoFiscal = 'FEV' | 'FEV_CONS_FINAL';

export interface PedidoUsuarioRequest{
    usuarioId: number;
    metodoPago: MetodoPago;
    tipoDocumentoFiscal?: TipoDocumentoFiscal;
    cliente?: Cliente | null;
}
