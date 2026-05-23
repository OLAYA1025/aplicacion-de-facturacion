import { Routes } from '@angular/router';
import { authChildGuard, authGuard, businessAreaGuard, superAdminGuard } from './core/guards/auth-guard';
//import { ConfiguracionComponent } from './features/configuracion/pages/configuracion.component';

export const routes: Routes = [
  {
    path: '',
    loadComponent: () =>
      import('./features/auth/pages/login/login.component').then((m) => m.LoginComponent),
  },
  {
    path: 'admin',
    canActivate: [authGuard, superAdminGuard],
    loadComponent: () =>
      import('./features/admin/layout/admin-layout.component').then((m) => m.AdminLayoutComponent),
    children: [
      {
        path: '',
        pathMatch: 'full',
        redirectTo: 'negocios',
      },
      {
        path: 'negocios',
        loadComponent: () =>
          import('./features/admin/negocios/pages/admin-negocios-list.component').then(
            (m) => m.AdminNegociosListComponent
          ),
        data: { title: 'Negocios' },
      },
      {
        path: 'negocios/nuevo',
        loadComponent: () =>
          import('./features/admin/negocios/pages/admin-negocios-create.component').then(
            (m) => m.AdminNegociosCreateComponent
          ),
        data: { title: 'Crear negocio' },
      },
      {
        path: '**',
        redirectTo: 'negocios',
      },
    ],
  },

  {
    path: '',
    loadComponent: () => import('./layout/layout.component').then((m) => m.LayoutComponent),
    canActivate:[authGuard, businessAreaGuard],
    canActivateChild:[authChildGuard],
    children: [
      { path: 'caja',
        loadComponent: () =>
          import('./features/caja/pages/caja.component').then((m) => m.CajaComponent),
        data: { title: 'Caja' }
      },

      { path: 'pedidos', 
        loadComponent: () =>
          import('./features/pedidos/pages/pedidos.component').then((m) => m.PedidosComponent),
        data: { title: 'Pedidos' }
      },
      {
        path: 'compras',
        children: [
          {
            path: 'listado',
            loadComponent: () =>
              import('./features/compra/pages/compras.component').then((m) => m.ComprasComponent),
            data: { title: 'Compras', comprasTab: 'listado' },
          },
          {
            path: 'nueva',
            loadComponent: () =>
              import('./features/compra/pages/compras.component').then((m) => m.ComprasComponent),
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
            loadComponent: () =>
              import('./features/proveedor/pages/proveedores.component').then(
                (m) => m.ProveedoresComponent
              ),
            data: { title: 'Proveedores', proveedoresTab: 'listado' },
          },
          {
            path: 'nuevo',
            loadComponent: () =>
              import('./features/proveedor/pages/proveedores.component').then(
                (m) => m.ProveedoresComponent
              ),
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
            loadComponent: () =>
              import('./features/productos/pages/productos.component').then(
                (m) => m.ProductosComponent
              ),
            data: { title: 'Inventario', productosTab: 'inventariables' },
          },
          {
            path: 'preparaciones',
            loadComponent: () =>
              import('./features/productos/pages/productos.component').then(
                (m) => m.ProductosComponent
              ),
            data: { title: 'Preparaciones', productosTab: 'preparaciones' },
          },
          {
            path: 'crear-producto',
            loadComponent: () =>
              import('./features/productos/pages/productos.component').then(
                (m) => m.ProductosComponent
              ),
            data: { title: 'Crear producto', productosTab: 'crear-producto' },
          },
          {
            path: 'categorias',
            loadComponent: () =>
              import('./features/productos/pages/productos.component').then(
                (m) => m.ProductosComponent
              ),
            data: { title: 'Categorías', productosTab: 'categorias' },
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
        loadComponent: () =>
          import('./features/estadisticas/pages/estadisticas.component').then(
            (m) => m.EstadisticasComponent
          ),
        data: { title: 'Estadisticas' },
      },
      {
        path: 'clientes',
        loadComponent: () =>
          import('./features/cliente/pages/clientes.component').then((m) => m.ClientesComponent),
        data: { title: 'Clientes' },
      },
      {
        path: 'facturas',
        loadComponent: () =>
          import('./features/facturas/pages/facturas.component').then((m) => m.FacturasComponent),
        data: { title: 'Facturas' },
      },
      {
        path: 'perfil',
        loadComponent: () =>
          import('./features/perfil/pages/perfil.component').then((m) => m.PerfilComponent),
        data: { title: 'Perfil' },
      },

      { 
        path: 'usuarios', 
        loadComponent: () =>
          import('./features/usuarios/pages/usuarios.component').then((m) => m.UsuariosComponent),
        data: { title: 'Usuarios' } 
      },
      //{ path: 'configuracion', component: ConfiguracionComponent, data: { title: 'Configuración' } },
    ],
  },
];
