import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MovimientosService } from '../../../core/services/movimientos.service';
import { Movimiento } from '../../../core/models/movimiento.model';
import { ChangeDetectorRef } from '@angular/core';
import { CajaService } from '../services/caja.service';
import { Caja } from '../models/caja.model';
import { CajaResumen } from '../../../core/models/cajaResumen.model';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { CrearEgresoRequest } from '../../../core/models/crearEgresoRequest.model';
import { ModalMensajeService } from '../../../shared/services/modal-mensaje.service';
import { obtenerMensajeError } from '../../../shared/utils/error-message.util';


@Component({
  selector: 'app-caja',
  standalone: true,
  imports: [CommonModule, FormsModule], // 👈 Agregar FormsModule
  templateUrl: './caja.component.html'
})
export class CajaComponent implements OnInit {
  // Datos de movimientos
  movimientos: Movimiento[] = [];
  movimientosFiltrados: Movimiento[] = [];
  loading: boolean = true;

  // Datos de caja
  cajaActual: Caja | null = null;
  cajaResumen: CajaResumen | null = null;
  loadingCaja: boolean = true;

  // Modales
  mostrarModalCaja: boolean = false;
  mostrarModalCerrarCaja: boolean = false;
  mostrarModalNuevoEgreso: boolean = false;

  // Nuevo egreso
  montoNuevoEgreso: number | null = null;
  motivoNuevoEgreso: string = '';
  guardandoNuevoEgreso: boolean = false;
  
  // Opciones de cierre
  opcionRetiro: 'nada' | 'todo' | 'personalizado' = 'nada';
  montoRetiroPersonalizado: number = 0;

  // Paginación
  paginaActual: number = 1;
  itemsPorPagina: number = 10;
  totalPaginas: number = 0;

  // Stats
  totalMovimientos: number = 0;
  totalIngresos: number = 0;
  totalEgresos: number = 0;

  constructor(
    private movimientosService: MovimientosService,
    private cajaService: CajaService, // 👈 Inyectar
    private authService: AuthService, // 👈 Inyectar
    private modalMensajeService: ModalMensajeService,
    private cdr: ChangeDetectorRef
  ) {}

  ngOnInit(): void {
    this.cargarCajaActual();
    this.cargarMovimientos();
  }

  cargarCajaActual(): void {
    this.loadingCaja = true;
    this.cajaService.obtenerCajaAbierta().subscribe({
      next: (caja) => {
        this.cajaActual = caja;
        this.cajaService.setCajaActiva(caja);
        this.loadingCaja = false;
        
        // Si la caja está abierta, cargar resumen
        if (caja.estado === 'ABIERTA') {
          this.cargarResumenCaja();
        }
        
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error al cargar caja:', error);
        this.cajaActual = null;
        this.cajaService.setCajaActiva(null);
        this.cajaResumen = null;
        this.loadingCaja = false;
        this.cdr.detectChanges();
      }
    });
  }

  cargarResumenCaja(): void {
    this.movimientosService.obtenerResumenCaja().subscribe({
      next: (resumen) => {
        this.cajaResumen = resumen;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error al cargar resumen:', error);
        this.cajaResumen = null;
        this.cdr.detectChanges();
      }
    });
  }

  abrirModalCaja(): void {
    this.mostrarModalCaja = true;
  }

  cerrarModalCaja(): void {
    this.mostrarModalCaja = false;
  }

  abrirCaja(): void {
    const usuario = this.authService.getUsuario();
    if (!usuario?.id) {
      console.error('No se encontró el usuario');
      this.mostrarError('No se encontró el usuario', 'Vuelve a iniciar sesión para continuar.');
      return;
    }

    this.cajaService.abrirCaja(usuario.id).subscribe({
      next: (caja) => {
        this.cajaActual = caja;
        this.cajaService.setCajaActiva(caja);
        this.cajaResumen = {
          montoInicial: caja.montoInicial ?? 0,
          ingresos: 0,
          ingresosEfectivo: 0,
          ingresosTransferencia: 0,
          ingresosTarjeta: 0,
          egresos: 0,
          retiros: 0,
          saldo: caja.montoInicial ?? 0,
        };
        this.cerrarModalCaja();
        this.cargarResumenCaja();
        this.cargarMovimientos(); // Recargar movimientos
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.mostrarError('No se pudo abrir la caja', 'Intenta nuevamente.', error);
      }
    });
  }

  abrirModalCerrarCaja(): void {
    this.cerrarModalCaja(); // Cerrar el modal anterior
    this.mostrarModalCerrarCaja = true;
    this.opcionRetiro = 'nada'; // Reset opción
    this.montoRetiroPersonalizado = 0;
  }

  cerrarModalCerrarCaja(): void {
    this.mostrarModalCerrarCaja = false;
  }

  confirmarCerrarCaja(): void {
    if (!this.cajaActual) return;

    const usuario = this.authService.getUsuario();
    if (!usuario?.id) {
      console.error('No se encontró el usuario');
      this.mostrarError('No se encontró el usuario', 'Vuelve a iniciar sesión para continuar.');
      return;
    }

    let montoRetiro = 0;

    // Calcular monto según opción
    switch (this.opcionRetiro) {
      case 'nada':
        montoRetiro = 0;
        break;
      case 'todo':
        montoRetiro = this.cajaResumen?.saldo || 0;
        break;
      case 'personalizado':
        montoRetiro = this.montoRetiroPersonalizado;
        // Validar que no retire más del saldo
        if (montoRetiro > (this.cajaResumen?.saldo || 0)) {
          this.mostrarError('No puedes retirar más del saldo disponible');
          return;
        }
        break;
    }

    this.cajaService.cerrarCaja(usuario.id, montoRetiro).subscribe({
      next: (caja) => {
        this.cajaActual = caja;
        this.cajaService.setCajaActiva(null);
        this.cerrarModalCerrarCaja();
        this.cargarCajaActual(); // Recargar estado
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.mostrarError('No se pudo cerrar la caja', 'Intenta nuevamente.', error);
      }
    });
  }

  // Resto de métodos anteriores (cargarMovimientos, paginación, etc.)
  cargarMovimientos(): void {
    this.loading = true;
    this.movimientosService.obtenerMovimientos().subscribe({
      next: (data) => {
        this.movimientos = data;
        this.movimientosFiltrados = [...data];
        this.calcularEstadisticas();
        this.calcularPaginacion();
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: (error) => {
        console.error('Error al cargar movimientos:', error);
        this.movimientos = [];
        this.movimientosFiltrados = [];
        this.calcularEstadisticas();
        this.calcularPaginacion();
        this.loading = false;
        this.cdr.detectChanges();
      }
    });
  }

  private calcularEstadisticas(): void {
    this.totalMovimientos = this.movimientos.length;
    
    this.totalIngresos = this.movimientos
      .filter(m => m.tipo === 'INGRESO')
      .reduce((sum, m) => sum + m.monto, 0);
    
    this.totalEgresos = this.movimientos
      .filter(m => m.tipo === 'EGRESO' || m.tipo === 'RETIRO')
      .reduce((sum, m) => sum + m.monto, 0);
  }

  private calcularPaginacion(): void {
    this.totalPaginas = Math.ceil(this.movimientosFiltrados.length / this.itemsPorPagina);
  }

  get movimientosPaginados(): Movimiento[] {
    const inicio = (this.paginaActual - 1) * this.itemsPorPagina;
    const fin = inicio + this.itemsPorPagina;
    return this.movimientosFiltrados.slice(inicio, fin);
  }

  irAPagina(pagina: number): void {
    if (pagina >= 1 && pagina <= this.totalPaginas) {
      this.paginaActual = pagina;
    }
  }

  paginaAnterior(): void {
    if (this.paginaActual > 1) {
      this.paginaActual--;
    }
  }

  paginaSiguiente(): void {
    if (this.paginaActual < this.totalPaginas) {
      this.paginaActual++;
    }
  }

  get rangoMostrado(): string {
    const inicio = (this.paginaActual - 1) * this.itemsPorPagina + 1;
    const fin = Math.min(this.paginaActual * this.itemsPorPagina, this.movimientosFiltrados.length);
    return `${inicio}-${fin} de ${this.movimientosFiltrados.length}`;
  }

  get numerosPage(): number[] {
    const paginas: number[] = [];
    const maxPaginasVisibles = 5;
    
    let inicio = Math.max(1, this.paginaActual - Math.floor(maxPaginasVisibles / 2));
    let fin = Math.min(this.totalPaginas, inicio + maxPaginasVisibles - 1);
    
    if (fin - inicio < maxPaginasVisibles - 1) {
      inicio = Math.max(1, fin - maxPaginasVisibles + 1);
    }
    
    for (let i = inicio; i <= fin; i++) {
      paginas.push(i);
    }
    
    return paginas;
  }

  getTipoClass(tipo: string): string {
    switch(tipo) {
      case 'INGRESO':
        return 'bg-green-100 text-green-800';
      case 'EGRESO':
        return 'bg-red-100 text-red-800';
      case 'RETIRO':
        return 'bg-orange-100 text-orange-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  }

  formatearFecha(fecha: string): string {
    const date = new Date(fecha);
    return date.toLocaleDateString('es-ES', { 
      day: 'numeric', 
      month: 'short', 
      year: 'numeric' 
    });
  }

  formatearMonto(monto: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0
    }).format(monto);
  }

  crearMovimiento(): void {
    // Lógica para crear un nuevo movimiento (puede ser un formulario o redirección)
    this.mostrarError('Funcionalidad de crear movimiento no implementada');
  }

  abrirModalNuevoEgreso(): void {
    if (!this.cajaActual || this.cajaActual.estado !== 'ABIERTA') {
      this.mostrarError('Debes abrir la caja para registrar un egreso');
      return;
    }

    this.montoNuevoEgreso = null;
    this.motivoNuevoEgreso = '';
    this.mostrarModalNuevoEgreso = true;
  }

  cerrarModalNuevoEgreso(): void {
    if (this.guardandoNuevoEgreso) return;

    this.mostrarModalNuevoEgreso = false;
    this.montoNuevoEgreso = null;
    this.motivoNuevoEgreso = '';
  }

  confirmarNuevoEgreso(): void {
    if (this.guardandoNuevoEgreso) return;

    if (!this.cajaActual || this.cajaActual.estado !== 'ABIERTA') {
      this.mostrarError('La caja debe estar abierta para crear un egreso');
      return;
    }

    const monto = Number(this.montoNuevoEgreso);
    const motivo = this.motivoNuevoEgreso.trim();

    if (!Number.isFinite(monto) || monto <= 0) {
      this.mostrarError('Ingresa un monto válido mayor a 0');
      return;
    }

    if (!motivo) {
      this.mostrarError('Ingresa un motivo para el egreso');
      return;
    }

    const usuario = this.authService.getUsuario();
    if (!usuario?.id) {
      this.mostrarError('No se encontró el usuario logueado', 'Vuelve a iniciar sesión.');
      return;
    }

    const request: CrearEgresoRequest = {
      monto,
      motivo,
      usuarioId: usuario.id
    };

    this.guardandoNuevoEgreso = true;
    this.movimientosService.crearEgreso(request).subscribe({
      next: () => {
        this.guardandoNuevoEgreso = false;
        this.cerrarModalNuevoEgreso();
        this.cargarMovimientos();
        this.cargarResumenCaja();
        this.cdr.detectChanges();
      },
      error: (error) => {
        this.mostrarError('No se pudo crear el egreso', 'Intenta nuevamente.', error);
        this.guardandoNuevoEgreso = false;
        this.cdr.detectChanges();
      }
    });
  }

  private mostrarError(titulo: string, mensaje: string = '', error?: unknown): void {
    const mensajeModal = error !== undefined ? obtenerMensajeError(error, mensaje) : mensaje;

    if (error !== undefined) {
      console.error(titulo, error);
    }

    void this.modalMensajeService.error({
      titulo,
      mensaje: mensajeModal,
      textoConfirmar: 'Entendido',
    });
  }

}
