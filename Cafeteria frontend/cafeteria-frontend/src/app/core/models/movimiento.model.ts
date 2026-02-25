export interface Movimiento {
    id: number;
    tipo: 'INGRESO' | 'EGRESO' | 'RETIRO';
    monto: number;
    motivo: string;
    fecha: string;
    username: string;
}