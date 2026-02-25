import { TipoDocumento } from "./tipoDocumento.model";

export interface Proveedor {
    id: number;
    nombre: string;
    telefono: string | null;
    correo: string | null;
    tipoDocumento: TipoDocumento;
}
