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
    path: 'create',
    loadComponent: () => import('./process-editor.component').then(m => m.ProcessEditorComponent)
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./process-editor.component').then(m => m.ProcessEditorComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./process-detail.component').then(m => m.ProcessDetailComponent)
  }
  // TODO: Add route for process start when component is ready
  // {
  //   path: ':id/start',
  //   loadComponent: () => import('./process-start.component').then(m => m.ProcessStartComponent)
  // }
];
