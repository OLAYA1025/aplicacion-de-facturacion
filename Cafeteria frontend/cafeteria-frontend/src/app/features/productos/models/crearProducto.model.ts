export interface CrearProducto {
    nombre: string;
    precio: number;
    tipoGestion: 'INVENTARIABLE' | 'PREPARACION';
    tipoImpuesto?: 'IVA' | 'INC';
    impuestoPorcentaje?: number;
    stockInicial: number;
    stockMinimo: number;
    categoriaId: number;
}
