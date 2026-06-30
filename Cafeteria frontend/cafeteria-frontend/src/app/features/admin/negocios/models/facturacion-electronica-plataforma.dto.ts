export type ProveedorFacturacion = 'FACTUS' | 'DIAN';

export interface ConfiguracionFacturacionElectronicaResponseDTO {
  id: number | string;
  negocioId?: number | string | null;
  proveedor: ProveedorFacturacion;
  baseUrl: string;
  username: string;
  password?: string | null;
  clientId: string;
  clientSecret?: string | null;
  prefijoCode: string;
  numberingRange?: NumberingRangeResponseDTO | null;
  numberingRanges?: NumberingRangeResponseDTO[] | null;
}

export interface CrearConfiguracionFacturacionElectronicaRequestDTO {
  negocioId: number;
  proveedor: ProveedorFacturacion;
  baseUrl: string;
  username: string;
  password: string;
  clientId: string;
  clientSecret: string;
  prefijoCode: string;
}

export interface EditarConfiguracionFacturacionElectronicaRequestDTO {
  proveedor: ProveedorFacturacion;
  baseUrl: string;
  username: string;
  password: string;
  clientId: string;
  clientSecret: string;
  prefijoCode: string;
}

export interface NumberingRangeResponseDTO {
  id: number | string;
  configuracionId?: number | string | null;
  providerRangeId: number | null;
  prefix: string;
  fromNumber: number;
  toNumber: number;
  currentNumber: number;
  resolutionNumber: string | null;
  technicalKey: string | null;
  active: boolean;
}

export interface CrearNumberingRangeRequestDTO {
  configuracionId: number;
  providerRangeId: number | null;
  prefix: string;
  fromNumber: number;
  toNumber: number;
  currentNumber: number;
  resolutionNumber: string | null;
  technicalKey: string | null;
}

export interface EditarNumberingRangeRequestDTO {
  providerRangeId: number | null;
  prefix: string;
  fromNumber: number;
  toNumber: number;
  currentNumber: number;
  resolutionNumber: string | null;
  technicalKey: string | null;
  active: boolean;
}
