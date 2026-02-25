export interface CompraResumenResponse {
    compraId: number;
    proveedorId: number;
    proveedorNombre: string;
    numeroFactura: string;
    estado: "BORRADOR" | "CONFIRMADA" | "ANULADA";
    fechaCreacion: string;
    fechaConfirmacion: string | null;
    fechaAnulacion: string | null;
    subtotal: number;
    total: number;
    observacion: string | null;
    anuladoPor: string | null;
}
