import { TipoDocumento } from "./tipoDocumento.model";

export interface ActualizarProveedorRequest {
    nombre: string;
    telefono?: string | null;
    correo?: string | null;
    tipoDocumento: TipoDocumento;
}
