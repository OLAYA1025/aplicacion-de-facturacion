export interface Producto{
    id: number;
    codigo?: string | null;
    nombre: string;
    precio: number;
    costo: number;
    activo: boolean;
    tipoGestion: 'INVENTARIABLE' | 'PREPARACION';
    tipoProducto?: 'INVENTARIABLE' | 'PREPARACION';
    tipoImpuesto: 'IVA' | 'INC';
    impuestoPorcentaje: number;
    ivaPorcentaje?: number;
    isExcluded: boolean;
    stockActual: number;
    stockMinimo: number;
    stockReservado: number;
    descripcion: string;
    categoria: {
        id: number;
        nombre: string;
    };
}
