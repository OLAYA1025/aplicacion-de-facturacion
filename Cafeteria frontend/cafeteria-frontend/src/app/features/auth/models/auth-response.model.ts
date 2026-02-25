import { UsuarioResponse } from '../../../core/models/usuario.model';

export interface AuthResponse {
    expiracionEpochMillis: number;
    tipo: string;
    token: string;
    usuario: UsuarioResponse;
}
