export interface Caja {
  id: number;
  fechaApertura: string;
  fechaCierre: string | null;
  montoInicial: number;
  montoFinal: number | null;
  estado: 'ABIERTA' | 'CERRADA';
  usuarioAperturaId: number;
  usuarioCierreId: number | null;
  utilidad: number | null;
}