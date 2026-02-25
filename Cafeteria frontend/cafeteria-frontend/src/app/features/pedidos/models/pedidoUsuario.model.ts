export interface PedidoUsuarioRequest{
    usuarioId: number;
    metodoPago: 'EFECTIVO' | 'TARJETA' | 'TRANSFERENCIA';
}