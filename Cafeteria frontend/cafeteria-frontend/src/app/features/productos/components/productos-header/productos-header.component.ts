import { CommonModule} from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { TabProductos } from '../../models/productos-ui.model';
import { AuthService } from 'src/app/core/services/auth.service';

@Component({
  selector: 'app-productos-header',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './productos-header.component.html',
})
export class ProductosHeaderComponent implements OnInit {
  @Input({ required: true }) tabActual!: TabProductos;
  @Input() esVistaListado: boolean = false;

  @Output() crearProducto = new EventEmitter<void>();

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
  get titulo(): string {
    if (this.tabActual === 'inventariables') {
      return 'Inventario de productos';
    }

    if (this.tabActual === 'preparaciones') {
      return 'Preparaciones';
    }

    if (this.tabActual === 'categorias') {
      return 'Categorías y notas';
    }

    return 'Crear producto';
  }

  get descripcion(): string {
    if (this.tabActual === 'crear-producto') {
      return 'Registra nuevos productos y define su tipo de gestión.';
    }

    if (this.tabActual === 'categorias') {
      return 'Configura grupos de notas predefinidas por categoría.';
    }

    return 'Administra disponibilidad, estado y datos comerciales.';
  }

  onCrearProducto(): void {
    this.crearProducto.emit();
  }
}
