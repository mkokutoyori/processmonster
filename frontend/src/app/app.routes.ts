/**
 * Application Routes Configuration
 *
 * Defines all application routes with lazy loading for optimal performance.
 * Routes are protected by AuthGuard where authentication is required.
 */
import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';

export const routes: Routes = [
  {
    path: '',
    redirectTo: '/dashboard',
    pathMatch: 'full'
  },
  {
    path: 'auth',
    loadChildren: () => import('./features/auth/auth.routes').then(m => m.AUTH_ROUTES)
  },
  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () => import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent)
  },
  {
    path: 'users',
    canActivate: [authGuard],
    loadChildren: () => import('./features/users/users.routes').then(m => m.USER_ROUTES)
  },
  {
    path: 'processes',
    canActivate: [authGuard],
    loadChildren: () => import('./features/processes/processes.routes').then(m => m.PROCESS_ROUTES)
  },
  {
    path: 'instances',
    canActivate: [authGuard],
    loadChildren: () => import('./features/instances/instances.routes').then(m => m.INSTANCE_ROUTES)
  },
  {
    path: 'tasks',
    canActivate: [authGuard],
    loadChildren: () => import('./features/tasks/tasks.routes').then(m => m.TASK_ROUTES)
  },
  {
    path: 'forms',
    canActivate: [authGuard],
    loadChildren: () => import('./features/forms/forms.routes').then(m => m.FORM_ROUTES)
  },
  {
    path: 'reports',
    canActivate: [authGuard],
    loadChildren: () => import('./features/reports/reports.routes').then(m => m.REPORT_ROUTES)
  },
  {
    path: 'audit',
    canActivate: [authGuard],
    loadChildren: () => import('./features/audit/audit.routes').then(m => m.AUDIT_ROUTES)
  },
  {
    path: 'admin',
    canActivate: [authGuard],
    loadChildren: () => import('./features/admin/admin.routes').then(m => m.ADMIN_ROUTES)
  },
  {
    path: '**',
    redirectTo: '/dashboard'
  }
];
