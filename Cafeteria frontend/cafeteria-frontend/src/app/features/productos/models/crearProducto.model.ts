export interface CrearProducto {
    codigo?: string | null;
    nombre: string;
    precio: number;
    costo: number;
    tipoGestion: 'INVENTARIABLE' | 'PREPARACION';
    tipoImpuesto?: 'IVA' | 'INC';
    impuestoPorcentaje?: number;
    stockInicial: number;
    stockMinimo: number;
    categoriaId: number;
}
