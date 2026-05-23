export interface actualizarProducto {
    codigo?: string | null;
    nombre: string;
    precio: number;
    tipoGestion: 'INVENTARIABLE' | 'PREPARACION';
    tipoImpuesto?: 'IVA' | 'INC';
    impuestoPorcentaje?: number;
    stockMinimo: number;
    categoriaId: number;

}
