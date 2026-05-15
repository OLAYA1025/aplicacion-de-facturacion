import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output, inject } from '@angular/core';
import { Producto } from '../../../../core/models/producto.model';
import { AuthService } from 'src/app/core/services/auth.service';

@Component({
  selector: 'app-productos-listado',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './productos-listado.component.html',
})
export class ProductosListadoComponent implements OnInit {
  @Input() loadingProductos: boolean = false;
  @Input() productosFiltrados: Producto[] = [];
  @Input() esInventariable: boolean = false;
  @Input() guardandoEstadoIds: ReadonlySet<number> = new Set<number>();

  @Output() editar = new EventEmitter<Producto>();
  @Output() cambiarEstado = new EventEmitter<Producto>();

  //esCajero = false;
  esAdministrador = false;
  //esMesero = false;
  //esCocinero = false;

  private readonly authService = inject(AuthService);


  ngOnInit(): void {
    const rolUsuario = this.authService.getUsuario()?.rol?.toUpperCase();
    //this.esCajero = rolUsuario === 'CAJERO';
    this.esAdministrador = rolUsuario === 'ADMINISTRADOR';
    //this.esMesero = rolUsuario === 'MESERO';
    //this.esCocinero = rolUsuario === 'COCINERO';
  }

  onEditar(producto: Producto): void {
    this.editar.emit(producto);
  }

  onCambiarEstado(producto: Producto): void {
    this.cambiarEstado.emit(producto);
  }

  stockDisponible(producto: Producto): number {
    return producto.stockActual - producto.stockReservado;
  }

  estaProcesandoEstado(productoId: number): boolean {
    return this.guardandoEstadoIds.has(productoId);
  }

  formatearMoneda(valor: number): string {
    return new Intl.NumberFormat('es-CO', {
      style: 'currency',
      currency: 'COP',
      minimumFractionDigits: 0,
    }).format(valor);
  }
}
