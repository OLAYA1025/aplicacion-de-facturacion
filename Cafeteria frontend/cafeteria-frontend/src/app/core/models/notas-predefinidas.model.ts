export type TipoSeleccionNota = 'UNICO' | 'MULTIPLE';

export interface NotaPredefinida {
  id: number;
  descripcion: string;
}

export interface GrupoNotaCategoria {
  id: number;
  nombre: string;
  obligatorio: boolean;
  tipoSeleccion: TipoSeleccionNota;
  categoriaId: number;
  notas: NotaPredefinida[];
}

export interface DetallePedidoNota {
  id: number;
  notaPredefinidaId: number;
  descripcion: string;
  grupoId: number;
  grupoNombre: string;
}
