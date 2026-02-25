export interface CrearProducto {
    nombre: string;
    precio: number;
    tipoGestion: 'INVENTARIABLE' | 'PREPARACION';
    stockInicial: number;
    stockMinimo: number;
    categoriaId: number;
}
