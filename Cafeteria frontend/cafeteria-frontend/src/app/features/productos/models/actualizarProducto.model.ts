export interface actualizarProducto {
    nombre: string;
    precio: number;
    tipoGestion: 'INVENTARIABLE' | 'PREPARACION';
    stockMinimo: number;
    categoriaId: number;

}