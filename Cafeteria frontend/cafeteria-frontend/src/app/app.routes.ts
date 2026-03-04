import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/pages/login/login.component';
import { CajaComponent } from './features/caja/pages/caja.component';
import { LayoutComponent } from './layout/layout.component';
import { PedidosComponent } from './features/pedidos/pages/pedidos.component';
import { ProductosComponent } from './features/productos/pages/productos.component';
import { ComprasComponent } from './features/compra/pages/compras.component';
import { ProveedoresComponent } from './features/proveedor/pages/proveedores.component';
import { ClientesComponent } from './features/cliente/pages/clientes.component';
import { EstadisticasComponent } from './features/estadisticas/pages/estadisticas.component';
import { FacturasComponent } from './features/facturas/pages/facturas.component';
import { PerfilComponent } from './features/perfil/pages/perfil.component';
import { UsuariosComponent } from './features/usuarios/pages/usuarios.component';
import { authChildGuard, authGuard } from './core/guards/auth-guard';
//import { ConfiguracionComponent } from './features/configuracion/pages/configuracion.component';

export const routes: Routes = [
  { path: '', component: LoginComponent },

  {
    path: '',
    component: LayoutComponent,
    canActivate:[authGuard],
    canActivateChild:[authChildGuard],
    children: [
      { path: 'caja',
        component: CajaComponent,
        data: { title: 'Caja' }
      },

      { path: 'pedidos', 
        component: PedidosComponent,
        data: { title: 'Pedidos' }
      },
      {
        path: 'compras',
        children: [
          {
            path: 'listado',
            component: ComprasComponent,
            data: { title: 'Compras', comprasTab: 'listado' },
          },
          {
            path: 'nueva',
            component: ComprasComponent,
            data: { title: 'Registrar compra', comprasTab: 'nueva' },
          },
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'listado',
          },
        ],
      },
      {
        path: 'proveedores',
        children: [
          {
            path: 'listado',
            component: ProveedoresComponent,
            data: { title: 'Proveedores', proveedoresTab: 'listado' },
          },
          {
            path: 'nuevo',
            component: ProveedoresComponent,
            data: { title: 'Registrar proveedor', proveedoresTab: 'nuevo' },
          },
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'listado',
          },
        ],
      },
      {
        path: 'productos',
        children: [
          {
            path: 'inventariables',
            component: ProductosComponent,
            data: { title: 'Inventario', productosTab: 'inventariables' },
          },
          {
            path: 'preparaciones',
            component: ProductosComponent,
            data: { title: 'Preparaciones', productosTab: 'preparaciones' },
          },
          {
            path: 'crear-producto',
            component: ProductosComponent,
            data: { title: 'Crear producto', productosTab: 'crear-producto' },
          },
          {
            path: 'nuevo',
            pathMatch: 'full',
            redirectTo: 'crear-producto',
          },
          {
            path: '',
            pathMatch: 'full',
            redirectTo: 'inventariables',
          },
        ],
      },
      {
        path: 'estadisticas',
        component: EstadisticasComponent,
        data: { title: 'Estadisticas' },
      },
      {
        path: 'clientes',
        component: ClientesComponent,
        data: { title: 'Clientes' },
      },
      {
        path: 'facturas',
        component: FacturasComponent,
        data: { title: 'Facturas' },
      },
      {
        path: 'perfil',
        component: PerfilComponent,
        data: { title: 'Perfil' },
      },

      { 
        path: 'usuarios', 
        component: UsuariosComponent, 
        data: { title: 'Usuarios' } 
      },
      //{ path: 'configuracion', component: ConfiguracionComponent, data: { title: 'Configuración' } },
    ],
  },
];
