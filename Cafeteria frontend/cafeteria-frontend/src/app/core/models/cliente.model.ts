export interface Cliente {
    tipoIdentificacion: 'CC' | 'NIT' | 'CE' | 'PAS';
    numeroIdentificacion: string;
    nombre: string;
    email: string | null;
}
