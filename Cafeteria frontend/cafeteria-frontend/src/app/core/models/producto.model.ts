export interface Producto{
    codigo: number;
    nombre: string;
    precio: number;
    activo: boolean;
    tipoGestion: 'INVENTARIABLE' | 'PREPARACION';
    stockActual: number;
    stockMinimo: number;
    stockReservado: number;
    descripcion: string;
    categoria: {
        id: number;
        nombre: string;
    };
}