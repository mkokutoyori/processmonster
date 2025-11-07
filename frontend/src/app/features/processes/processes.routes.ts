/**
 * Process Management Routes
 */
import { Routes } from '@angular/router';

export const PROCESS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./process-list.component').then(m => m.ProcessListComponent)
  }
  // TODO Phase 4: Add routes for create, edit, view when components are ready
  // {
  //   path: 'create',
  //   loadComponent: () => import('./process-create.component').then(m => m.ProcessCreateComponent)
  // },
  // {
  //   path: ':id',
  //   loadComponent: () => import('./process-detail.component').then(m => m.ProcessDetailComponent)
  // },
  // {
  //   path: ':id/edit',
  //   loadComponent: () => import('./process-edit.component').then(m => m.ProcessEditComponent)
  // }
];
