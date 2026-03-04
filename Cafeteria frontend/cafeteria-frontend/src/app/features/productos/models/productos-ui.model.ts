export type TabProductos = 'inventariables' | 'preparaciones' | 'crear-producto';

export type TipoGestion = 'INVENTARIABLE' | 'PREPARACION';

export type EstadoFiltro = 'TODOS' | 'ACTIVOS' | 'DESACTIVADOS';

export type CampoOrden = 'nombre' | 'precio' | 'stockDisponible';

export type DireccionOrden = 'asc' | 'desc';

export type SelectorCategoriaCrear = number | 'crear' | null;

export interface ProductoCrearFormState {
  nombre: string;
  precio: number | null;
  tipoGestion: TipoGestion;
  ivaPorcentaje: number | null;
  stockInicial: number | null;
  stockMinimo: number | null;
  categoriaId: number | null;
}

export interface ProductoEditarFormState {
  nombre: string;
  precio: number | null;
  tipoGestion: TipoGestion;
  ivaPorcentaje: number | null;
  stockMinimo: number | null;
  categoriaId: number | null;
}
