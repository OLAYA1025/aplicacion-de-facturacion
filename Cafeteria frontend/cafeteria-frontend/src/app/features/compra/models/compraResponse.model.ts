export interface CompraDetalleResponse {
    productoId: number;
    nombreProducto: string;
    cantidad: number;
    costoUnitario: number;
    subtotal: number;
}

export interface CompraResponse {
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
    detalles: CompraDetalleResponse[];
}
