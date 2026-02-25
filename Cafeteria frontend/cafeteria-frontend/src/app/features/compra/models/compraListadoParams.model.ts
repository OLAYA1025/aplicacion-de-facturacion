export interface CompraListadoParams {
    page?: number;
    size?: number;
    estado?: 'BORRADOR' | 'CONFIRMADA' | 'ANULADA';
    ordenarPor?: 'id' | 'fechaCreacion' | 'total' | 'estado' | 'numeroFactura';
    direccion?: 'asc' | 'desc';
    desde?: string;
    hasta?: string;
}
