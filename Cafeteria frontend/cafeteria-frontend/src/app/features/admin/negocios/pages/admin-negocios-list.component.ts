import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Observable, finalize, map, of, switchMap } from 'rxjs';
import { ModalMensajeService } from '../../../../shared/services/modal-mensaje.service';
import { obtenerMensajeError } from '../../../../shared/utils/error-message.util';
import {
  ConfiguracionFacturacionElectronicaResponseDTO,
  CrearConfiguracionFacturacionElectronicaRequestDTO,
  EditarConfiguracionFacturacionElectronicaRequestDTO,
  EditarNumberingRangeRequestDTO,
  NumberingRangeResponseDTO,
  ProveedorFacturacion
} from '../models/facturacion-electronica-plataforma.dto';
import { NegocioPlataformaResponseDTO } from '../models/negocio-plataforma-response.dto';
import { PlatformNegocioService } from '../services/platform-negocio.service';

@Component({
  selector: 'app-admin-negocios-list',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './admin-negocios-list.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminNegociosListComponent implements  OnInit {
  private readonly formBuilder = inject(FormBuilder);
  private readonly platformNegocioService = inject(PlatformNegocioService);
  private readonly modalMensajeService = inject(ModalMensajeService);
  private readonly cdr = inject(ChangeDetectorRef);

  loading = false;
  errorListado: string | null = null;
  negocios: NegocioPlataformaResponseDTO[] = [];
  procesandoNegocios: Record<string, boolean> = {};
  modalFacturacionAbierto = false;
  cargandoFacturacion = false;
  guardandoFacturacion = false;
  negocioFacturacionSeleccionado: NegocioPlataformaResponseDTO | null = null;
  configuracionFacturacionActual: ConfiguracionFacturacionElectronicaResponseDTO | null = null;
  numberingRangeActual: NumberingRangeResponseDTO | null = null;
  errorFacturacion: string | null = null;

  modalConfigurarLogoAbierto = false;
  negocioLogoSeleccionado: NegocioPlataformaResponseDTO | null = null;
  guardandoLogo = false;

  readonly proveedoresFacturacion: ProveedorFacturacion[] = ['FACTUS', 'DIAN'];

  readonly facturacionForm = this.formBuilder.nonNullable.group({
    proveedor: this.formBuilder.nonNullable.control<ProveedorFacturacion | ''>('', Validators.required),
    baseUrl: ['', Validators.required],
    username: ['', Validators.required],
    password: ['', Validators.required],
    clientId: ['', Validators.required],
    clientSecret: ['', Validators.required],
    prefijoCode: ['', Validators.required],
  });

  readonly numberingRangeForm = this.formBuilder.group({
    providerRangeId: this.formBuilder.control<number | null>(null),
    prefix: this.formBuilder.nonNullable.control(''),
    fromNumber: this.formBuilder.control<number | null>(null),
    toNumber: this.formBuilder.control<number | null>(null),
    currentNumber: this.formBuilder.control<number | null>(null),
    resolutionNumber: this.formBuilder.nonNullable.control(''),
    technicalKey: this.formBuilder.nonNullable.control(''),
    active: this.formBuilder.nonNullable.control(true),
  });

  readonly logoForm = this.formBuilder.nonNullable.group({
    nombreLogo: this.formBuilder.nonNullable.control('', Validators.required),
  });
  

  ngOnInit(): void {
    this.cargarNegocios();
  }

  cargarNegocios(): void {
    this.loading = true;
    this.errorListado = null;

    this.platformNegocioService
      .listarNegocios()
      .pipe(
        finalize(() => {
          this.loading = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe({
        next: (negocios) => {
          this.negocios = negocios;
          this.cdr.markForCheck();
        },
        error: (error) => {
          this.negocios = [];
          this.errorListado = obtenerMensajeError(
            error,
            'No se pudieron cargar los negocios. Intenta nuevamente.'
          );
          void this.modalMensajeService.error({
            titulo: 'No se pudo cargar el listado',
            mensaje: this.errorListado,
          });
          this.cdr.markForCheck();
        },
      });
  }

  abrirModalConfigurarLogo(negocio: NegocioPlataformaResponseDTO): void {
    this.negocioLogoSeleccionado = negocio;
    this.logoForm.reset({
      nombreLogo: negocio.logoArchivo ?? '',
    });
    this.modalConfigurarLogoAbierto = true;
    this.cdr.markForCheck();
  }

  cerrarModalConfigurarLogo(): void {
    this.modalConfigurarLogoAbierto = false;
    this.negocioLogoSeleccionado = null;
    this.logoForm.reset({ nombreLogo: '' });
    this.guardandoLogo = false;
    this.cdr.markForCheck();
  }

  guardarLogo(): void {
    if (!this.negocioLogoSeleccionado) {
      return;
    }

    this.logoForm.markAllAsTouched();

    if (this.logoForm.invalid) {
      return;
    }

    this.guardandoLogo = true;
    const nombreLogo = this.logoForm.controls.nombreLogo.value.trim();

    const negocioId = String(this.negocioLogoSeleccionado.id);

    this.platformNegocioService.configurarLogoNegocio(negocioId, { nombreLogo })
      .pipe(
        finalize(() => {  
          this.guardandoLogo = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe({
        next: () => {
          if (this.negocioLogoSeleccionado) {
            this.negocioLogoSeleccionado.logoArchivo = nombreLogo;
          }
          this.modalConfigurarLogoAbierto = false;
          this.logoForm.reset({ nombreLogo: '' });
          this.cdr.markForCheck();
        },
        error: (error) => {
          const mensaje = obtenerMensajeError(error, 'No se pudo configurar el logo del negocio.');
          void this.modalMensajeService.error({
            titulo: 'Error al configurar logo',
            mensaje,
          });
          this.cdr.markForCheck();
        }
      });

  }

  async activarNegocio(negocio: NegocioPlataformaResponseDTO): Promise<void> {
    const confirmado = await this.modalMensajeService.confirmar({
      titulo: 'Activar negocio',
      mensaje: `¿Deseas activar "${negocio.nombre}"?`,
      textoConfirmar: 'Activar',
    });

    if (!confirmado) {
      return;
    }

    const id = String(negocio.id);
    this.procesandoNegocios[id] = true;
    this.cdr.markForCheck();

    this.platformNegocioService
      .activarNegocio(negocio.id)
      .pipe(
        finalize(() => {
          this.procesandoNegocios[id] = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe({
        next: (negocioActualizado) => {
          this.reemplazarNegocio(negocioActualizado);
          void this.modalMensajeService.abrir({
            tipo: 'confirmacion',
            titulo: 'Negocio activado',
            mensaje: `Se activó "${negocio.nombre}" correctamente.`,
            textoConfirmar: 'Entendido',
          });
          this.cdr.markForCheck();
        },
        error: (error) => {
          const mensaje = obtenerMensajeError(error, 'No se pudo activar el negocio.');
          void this.modalMensajeService.error({
            titulo: 'Error al activar',
            mensaje,
          });
          this.cdr.markForCheck();
        },
      });
  }

  async desactivarNegocio(negocio: NegocioPlataformaResponseDTO): Promise<void> {
    const confirmado = await this.modalMensajeService.confirmar({
      titulo: 'Desactivar negocio',
      mensaje: `¿Seguro que deseas desactivar "${negocio.nombre}"?`,
      textoConfirmar: 'Desactivar',
    });

    if (!confirmado) {
      return;
    }

    const id = String(negocio.id);
    this.procesandoNegocios[id] = true;
    this.cdr.markForCheck();

    this.platformNegocioService
      .desactivarNegocio(negocio.id)
      .pipe(
        finalize(() => {
          this.procesandoNegocios[id] = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe({
        next: (negocioActualizado) => {
          this.reemplazarNegocio(negocioActualizado);
          void this.modalMensajeService.abrir({
            tipo: 'confirmacion',
            titulo: 'Negocio desactivado',
            mensaje: `Se desactivó "${negocio.nombre}" correctamente.`,
            textoConfirmar: 'Entendido',
          });
          this.cdr.markForCheck();
        },
        error: (error) => {
          const mensaje = obtenerMensajeError(error, 'No se pudo desactivar el negocio.');
          void this.modalMensajeService.error({
            titulo: 'Error al desactivar',
            mensaje,
          });
          this.cdr.markForCheck();
        },
      });
  }

  estaProcesando(id: number | string): boolean {
    return !!this.procesandoNegocios[String(id)];
  }

  abrirModalFacturacionElectronica(negocio: NegocioPlataformaResponseDTO): void {
    this.negocioFacturacionSeleccionado = negocio;
    this.configuracionFacturacionActual = this.obtenerConfiguracionFacturacion(negocio);
    this.numberingRangeActual = this.obtenerNumberingRange(negocio, this.configuracionFacturacionActual);
    this.errorFacturacion = null;
    this.rellenarFormularioFacturacion();
    this.modalFacturacionAbierto = true;
    this.cdr.markForCheck();
    this.cargarFacturacionElectronicaNegocio(negocio);
  }

  cerrarModalFacturacionElectronica(): void {
    this.modalFacturacionAbierto = false;
    this.cargandoFacturacion = false;
    this.guardandoFacturacion = false;
    this.negocioFacturacionSeleccionado = null;
    this.configuracionFacturacionActual = null;
    this.numberingRangeActual = null;
    this.errorFacturacion = null;
    this.facturacionForm.reset();
    this.numberingRangeForm.reset({ active: true });
    this.cdr.markForCheck();
  }

  guardarFacturacionElectronica(): void {
    if (!this.negocioFacturacionSeleccionado) {
      return;
    }

    this.errorFacturacion = null;
    this.facturacionForm.markAllAsTouched();

    if (this.facturacionForm.invalid) {
      this.errorFacturacion = 'Completa los campos obligatorios de facturacion electronica.';
      this.cdr.markForCheck();
      return;
    }

    if (!this.numberingRangeEsValido()) {
      this.errorFacturacion =
        'Completa prefix, from number, to number y current number para guardar el Numbering Range.';
      this.cdr.markForCheck();
      return;
    }

    this.guardandoFacturacion = true;
    this.construirOperacionGuardadoFacturacion(this.negocioFacturacionSeleccionado)
      .pipe(
        finalize(() => {
          this.guardandoFacturacion = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe({
        next: () => {
          const nombreNegocio = this.negocioFacturacionSeleccionado?.nombre ?? 'el negocio';
          this.cerrarModalFacturacionElectronica();
          this.cargarNegocios();
          void this.modalMensajeService.abrir({
            tipo: 'confirmacion',
            titulo: 'Facturacion electronica guardada',
            mensaje: `La configuracion de "${nombreNegocio}" se guardo correctamente.`,
            textoConfirmar: 'Entendido',
          });
          this.cdr.markForCheck();
        },
        error: (error) => {
          this.errorFacturacion = obtenerMensajeError(
            error,
            'No se pudo guardar la configuracion de facturacion electronica.'
          );
          void this.modalMensajeService.error({
            titulo: 'Error al guardar',
            mensaje: this.errorFacturacion,
          });
          this.cdr.markForCheck();
        },
      });
  }

  get tituloBotonFacturacion(): string {
    if (this.guardandoFacturacion) {
      return 'Guardando...';
    }

    return this.configuracionFacturacionActual ? 'Guardar cambios' : 'Crear';
  }

  get tieneConfiguracionFacturacion(): boolean {
    return this.configuracionFacturacionActual !== null;
  }

  textoAccionFacturacion(negocio: NegocioPlataformaResponseDTO): string {
    return negocio.electronicaHabilitada ? 'Editar configuración' : 'Crear configuración';
  }

  get tieneNumberingRange(): boolean {
    return this.numberingRangeActual !== null;
  }

  obtenerAdminNombre(negocio: NegocioPlataformaResponseDTO): string {
    return (
      negocio.adminNombre ||
      negocio.administradorPrincipal?.nombre ||
      negocio.admin?.nombre ||
      '-'
    );
  }

  obtenerAdminUsername(negocio: NegocioPlataformaResponseDTO): string {
    return (
      negocio.adminUsername ||
      negocio.administradorPrincipal?.username ||
      negocio.admin?.username ||
      '-'
    );
  }

  obtenerAdminEmail(negocio: NegocioPlataformaResponseDTO): string {
    return negocio.adminEmail || negocio.administradorPrincipal?.email || negocio.admin?.email || '-';
  }

  obtenerFechaCreacion(negocio: NegocioPlataformaResponseDTO): string {
    const fecha = negocio.fechaCreacion || negocio.createdAt;
    if (!fecha) {
      return '-';
    }

    const fechaParseada = new Date(fecha);
    if (Number.isNaN(fechaParseada.getTime())) {
      return fecha;
    }

    return fechaParseada.toLocaleString('es-CO');
  }

  private reemplazarNegocio(negocioActualizado: NegocioPlataformaResponseDTO): void {
    this.negocios = this.negocios.map((negocio) =>
      negocio.id === negocioActualizado.id
        ? {
            ...negocio,
            ...negocioActualizado,
          }
        : negocio
    );
  }

  private rellenarFormularioFacturacion(): void {
    const configuracion = this.configuracionFacturacionActual;
    const range = this.numberingRangeActual;

    this.facturacionForm.reset({
      proveedor: configuracion?.proveedor ?? '',
      baseUrl: configuracion?.baseUrl ?? '',
      username: configuracion?.username ?? '',
      password: configuracion?.password ?? '',
      clientId: configuracion?.clientId ?? '',
      clientSecret: configuracion?.clientSecret ?? '',
      prefijoCode: configuracion?.prefijoCode ?? '',
    });

    this.numberingRangeForm.reset({
      providerRangeId: range?.providerRangeId ?? null,
      prefix: range?.prefix ?? '',
      fromNumber: range?.fromNumber ?? null,
      toNumber: range?.toNumber ?? null,
      currentNumber: range?.currentNumber ?? null,
      resolutionNumber: range?.resolutionNumber ?? '',
      technicalKey: range?.technicalKey ?? '',
      active: range?.active ?? true,
    });
  }

  private cargarFacturacionElectronicaNegocio(negocio: NegocioPlataformaResponseDTO): void {
    this.cargandoFacturacion = true;
    this.cdr.markForCheck();

    this.platformNegocioService
      .obtenerConfiguracionFacturacionElectronica(negocio.id)
      .pipe(
        finalize(() => {
          this.cargandoFacturacion = false;
          this.cdr.markForCheck();
        })
      )
      .subscribe({
        next: (configuracion) => {
          if (this.negocioFacturacionSeleccionado?.id !== negocio.id) {
            return;
          }

          this.configuracionFacturacionActual = configuracion;
          this.numberingRangeActual = this.obtenerNumberingRange(negocio, configuracion);
          this.rellenarFormularioFacturacion();
          this.cdr.markForCheck();
        },
        error: (error) => {
          this.errorFacturacion = obtenerMensajeError(
            error,
            'No se pudo cargar la configuracion de facturacion electronica.'
          );
          void this.modalMensajeService.error({
            titulo: 'Error al cargar facturacion',
            mensaje: this.errorFacturacion,
          });
          this.cdr.markForCheck();
        },
      });
  }

  private obtenerConfiguracionFacturacion(
    negocio: NegocioPlataformaResponseDTO
  ): ConfiguracionFacturacionElectronicaResponseDTO | null {
    return negocio.configuracionFacturacionElectronica ?? negocio.facturacionElectronica ?? null;
  }

  private obtenerNumberingRange(
    negocio: NegocioPlataformaResponseDTO,
    configuracion: ConfiguracionFacturacionElectronicaResponseDTO | null
  ): NumberingRangeResponseDTO | null {
    if (configuracion?.numberingRange) {
      return configuracion.numberingRange;
    }

    if (configuracion?.numberingRanges?.length) {
      return configuracion.numberingRanges[0] ?? null;
    }

    if (negocio.numberingRange) {
      return negocio.numberingRange;
    }

    return negocio.numberingRanges?.[0] ?? null;
  }

  private construirOperacionGuardadoFacturacion(
    negocio: NegocioPlataformaResponseDTO
  ): Observable<void | number | null> {
    const configuracionPayload = this.construirPayloadConfiguracion(negocio);
    const configuracionId = this.configuracionFacturacionActual?.id ?? null;
    const operacionConfiguracion$: Observable<number | string> = configuracionId
      ? this.platformNegocioService
          .editarConfiguracionFacturacionElectronica(configuracionId, configuracionPayload)
          .pipe(map(() => configuracionId))
      : this.platformNegocioService.crearConfiguracionFacturacionElectronica({
          negocioId: Number(negocio.id),
          ...configuracionPayload,
        });

    return operacionConfiguracion$.pipe(
      switchMap((idConfiguracionGuardada) => {
        const numberingRangePayload = this.construirPayloadNumberingRange();

        if (!numberingRangePayload) {
          return of(null);
        }

        if (this.numberingRangeActual) {
          return this.platformNegocioService.editarNumberingRange(
            this.numberingRangeActual.id,
            numberingRangePayload
          );
        }

        return this.platformNegocioService.crearNumberingRange({
          configuracionId: Number(idConfiguracionGuardada),
          ...numberingRangePayload,
        });
      })
    );
  }

  private construirPayloadConfiguracion(
    negocio: NegocioPlataformaResponseDTO
  ): EditarConfiguracionFacturacionElectronicaRequestDTO &
    Pick<CrearConfiguracionFacturacionElectronicaRequestDTO, 'proveedor'> {
    const proveedor = this.facturacionForm.controls.proveedor.value;

    if (!this.esProveedorFacturacion(proveedor)) {
      throw new Error(`Proveedor invalido para el negocio ${negocio.id}`);
    }

    return {
      proveedor,
      baseUrl: this.facturacionForm.controls.baseUrl.value.trim(),
      username: this.facturacionForm.controls.username.value.trim(),
      password: this.facturacionForm.controls.password.value,
      clientId: this.facturacionForm.controls.clientId.value.trim(),
      clientSecret: this.facturacionForm.controls.clientSecret.value,
      prefijoCode: this.facturacionForm.controls.prefijoCode.value.trim(),
    };
  }

  private construirPayloadNumberingRange(): EditarNumberingRangeRequestDTO | null {
    if (!this.numberingRangeTieneDatos() && !this.numberingRangeActual) {
      return null;
    }

    const fromNumber = this.numberingRangeForm.controls.fromNumber.value;
    const toNumber = this.numberingRangeForm.controls.toNumber.value;
    const currentNumber = this.numberingRangeForm.controls.currentNumber.value;

    if (fromNumber === null || toNumber === null || currentNumber === null) {
      return null;
    }

    return {
      providerRangeId: this.numberingRangeForm.controls.providerRangeId.value,
      prefix: this.numberingRangeForm.controls.prefix.value.trim(),
      fromNumber,
      toNumber,
      currentNumber,
      resolutionNumber: this.normalizarTextoOpcional(
        this.numberingRangeForm.controls.resolutionNumber.value
      ),
      technicalKey: this.normalizarTextoOpcional(this.numberingRangeForm.controls.technicalKey.value),
      active: this.numberingRangeForm.controls.active.value,
    };
  }

  private numberingRangeEsValido(): boolean {
    if (!this.numberingRangeTieneDatos() && !this.numberingRangeActual) {
      return true;
    }

    const prefix = this.numberingRangeForm.controls.prefix.value.trim();
    const fromNumber = this.numberingRangeForm.controls.fromNumber.value;
    const toNumber = this.numberingRangeForm.controls.toNumber.value;
    const currentNumber = this.numberingRangeForm.controls.currentNumber.value;

    return (
      prefix.length > 0 &&
      fromNumber !== null &&
      toNumber !== null &&
      currentNumber !== null &&
      fromNumber >= 0 &&
      toNumber >= fromNumber &&
      currentNumber >= fromNumber &&
      currentNumber <= toNumber
    );
  }

  private numberingRangeTieneDatos(): boolean {
    const valores = this.numberingRangeForm.getRawValue();

    return (
      valores.providerRangeId !== null ||
      valores.prefix.trim().length > 0 ||
      valores.fromNumber !== null ||
      valores.toNumber !== null ||
      valores.currentNumber !== null ||
      valores.resolutionNumber.trim().length > 0 ||
      valores.technicalKey.trim().length > 0
    );
  }

  private normalizarTextoOpcional(valor: string): string | null {
    const texto = valor.trim();
    return texto.length > 0 ? texto : null;
  }

  private esProveedorFacturacion(valor: string): valor is ProveedorFacturacion {
    return this.proveedoresFacturacion.includes(valor as ProveedorFacturacion);
  }
}
