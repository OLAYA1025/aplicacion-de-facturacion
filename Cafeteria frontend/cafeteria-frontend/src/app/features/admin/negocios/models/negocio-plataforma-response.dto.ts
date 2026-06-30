import {
  ConfiguracionFacturacionElectronicaResponseDTO,
  NumberingRangeResponseDTO,
} from './facturacion-electronica-plataforma.dto';

export interface AdminNegocioResumenDTO {
  id?: number | string;
  nombre?: string | null;
  username?: string | null;
  email?: string | null;
}

export interface NegocioPlataformaResponseDTO {
  id: number | string;
  nombre: string;
  nit: string;
  direccion?: string | null;
  telefono?: string | null;
  activo: boolean;
  electronicaHabilitada?: boolean | null;
  logoArchivo?: string | null;
  fechaCreacion?: string | null;
  createdAt?: string | null;
  administradorPrincipal?: AdminNegocioResumenDTO | null;
  admin?: AdminNegocioResumenDTO | null;
  adminNombre?: string | null;
  adminUsername?: string | null;
  adminEmail?: string | null;
  configuracionFacturacionElectronica?: ConfiguracionFacturacionElectronicaResponseDTO | null;
  facturacionElectronica?: ConfiguracionFacturacionElectronicaResponseDTO | null;
  numberingRange?: NumberingRangeResponseDTO | null;
  numberingRanges?: NumberingRangeResponseDTO[] | null;
}
