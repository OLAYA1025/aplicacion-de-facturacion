export interface CrearUsuarioRequest {
  nombre: string;
  username: string;
  password: string;
  email: string | null;
  rol: string;
}
