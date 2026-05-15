import { Component, EventEmitter, Input, OnInit, Output, inject } from '@angular/core';
import { NavigationEnd, Router, RouterLink, RouterLinkActive } from '@angular/router';
import { CommonModule } from '@angular/common';
import { filter } from 'rxjs/operators';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-sidebar',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, CommonModule],
  templateUrl: './sidebar.component.html',
})
export class SidebarComponent implements OnInit {
  private readonly router = inject(Router);
  private readonly authService = inject(AuthService);

  @Input() collapsed = false;
  @Input() mobileOpen = false;

  @Output() toggleSidebar = new EventEmitter<void>();
  @Output() closeMobile = new EventEmitter<void>();

  inventarioExpanded = false;
  comprasExpanded = false;
  proveedoresExpanded = false;
  esCajero = false;
  esAdministrador = false;
  esMesero = false;
  esCocinero = false;

  toggleInventario() {
    this.inventarioExpanded = !this.inventarioExpanded;
  }

  toggleCompras() {
    this.comprasExpanded = !this.comprasExpanded;
  }

  toggleProveedores() {
    this.proveedoresExpanded = !this.proveedoresExpanded;
  }

  ngOnInit(): void {
    const rolUsuario = this.authService.getUsuario()?.rol?.toUpperCase();
    this.esCajero = rolUsuario === 'CAJERO';
    this.esAdministrador = rolUsuario === 'ADMINISTRADOR';
    this.esMesero = rolUsuario === 'MESERO';
    this.esCocinero = rolUsuario === 'COCINERO';

    this.actualizarExpansionSegunRuta(this.router.url);
    this.router.events.pipe(filter((event) => event instanceof NavigationEnd)).subscribe(() => {
      this.actualizarExpansionSegunRuta(this.router.url);
    });
  }

  private actualizarExpansionSegunRuta(url: string): void {
    this.inventarioExpanded = url.startsWith('/productos');
    this.comprasExpanded = url.startsWith('/compras');
    this.proveedoresExpanded = url.startsWith('/proveedores');
  }
}
