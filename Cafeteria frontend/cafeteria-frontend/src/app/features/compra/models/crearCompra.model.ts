import { DetalleCompraRequest } from "./compraDetalle.model";

export interface CompraRequest {
    proveedorId: number;
    numeroFactura: string;
    observacion: string;
    detalles: DetalleCompraRequest[];
}
