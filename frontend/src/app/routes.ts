import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { Login } from './features/auth/pages/login/login';
import { Register } from './features/auth/pages/register/register';
import { MainLayout } from './layouts/main-layout/main-layout';
import { Dashboard } from './features/dashboard/pages/dashboard/dashboard';
import { InventarioComponent } from './features/inventario/pages/inventario/inventario';
import { PedidosComponent } from './features/pedidos/pages/pedidos/pedidos';
import { EnvioComponent } from './features/envio/pages/envio/envio';

export const routes: Routes = [
  { path: 'login', component: Login },
  { path: 'register', component: Register },
  {
    path: 'dashboard',
    component: MainLayout,
    canActivate: [authGuard],
    children: [
      { path: '',           component: Dashboard },
      { path: 'inventario', component: InventarioComponent },
      { path: 'pedidos',    component: PedidosComponent },
      { path: 'envio',      component: EnvioComponent },
    ]
  },
  { path: '',   redirectTo: '/login', pathMatch: 'full' },
      // ← AGREGAR ANTES del path '**'
  // { path: 'swagger',           redirectTo: '/swagger' },
  // { path: 'swagger/auth',      redirectTo: '/swagger/auth' },
  // { path: 'swagger/inventario', redirectTo: '/swagger/inventario' },
  // { path: 'swagger/pedido',    redirectTo: '/swagger/pedido' },
  // { path: 'swagger/envio',     redirectTo: '/swagger/envio' },
  { path: '**', redirectTo: '/login' }
  
];

