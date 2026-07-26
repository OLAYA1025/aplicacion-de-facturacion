export type TabProductos = 'inventariables' | 'preparaciones' | 'categorias' | 'crear-producto';

export type TipoGestion = 'INVENTARIABLE' | 'PREPARACION';
export type TipoImpuesto = 'IVA' | 'INC';

export type EstadoFiltro = 'TODOS' | 'ACTIVOS' | 'DESACTIVADOS';

export type CampoOrden = 'nombre' | 'precio' | 'stockDisponible';

export type DireccionOrden = 'asc' | 'desc';

export type SelectorCategoriaCrear = number | 'crear' | null;

export interface ProductoCrearFormState {
  codigo: string;
  nombre: string;
  precio: number | null;
  costo: number | null;
  tipoGestion: TipoGestion;
  tipoImpuesto: TipoImpuesto;
  impuestoPorcentaje: number | null;
  stockInicial: number | null;
  stockMinimo: number | null;
  categoriaId: number | null;
}

export interface ProductoEditarFormState {
  codigo: string;
  nombre: string;
  precio: number | null;
  costo: number | null;
  tipoGestion: TipoGestion;
  tipoImpuesto: TipoImpuesto;
  impuestoPorcentaje: number | null;
  stockMinimo: number | null;
  categoriaId: number | null;
}
