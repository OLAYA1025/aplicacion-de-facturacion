export interface CrearProducto {
    nombre: string;
    precio: number;
    tipoGestion: 'INVENTARIABLE' | 'PREPARACION';
    ivaPorcentaje?: number;
    stockInicial: number;
    stockMinimo: number;
    categoriaId: number;
}
