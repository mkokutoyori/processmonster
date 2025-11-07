/**
 * Process Management Routes
 */
import { Routes } from '@angular/router';

export const PROCESS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./process-list.component').then(m => m.ProcessListComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./process-detail.component').then(m => m.ProcessDetailComponent)
  }
  // TODO: Add routes for create and edit when components are ready
  // {
  //   path: 'create',
  //   loadComponent: () => import('./process-create.component').then(m => m.ProcessCreateComponent)
  // },
  // {
  //   path: ':id/edit',
  //   loadComponent: () => import('./process-edit.component').then(m => m.ProcessEditComponent)
  // },
  // {
  //   path: ':id/start',
  //   loadComponent: () => import('./process-start.component').then(m => m.ProcessStartComponent)
  // }
];
