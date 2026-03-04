export interface actualizarProducto {
    nombre: string;
    precio: number;
    tipoGestion: 'INVENTARIABLE' | 'PREPARACION';
    ivaPorcentaje?: number;
    stockMinimo: number;
    categoriaId: number;

}
