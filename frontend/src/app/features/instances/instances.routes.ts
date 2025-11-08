/**
 * Process Instance Management Routes
 */
import { Routes } from '@angular/router';

export const INSTANCE_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./instance-list.component').then(m => m.InstanceListComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./instance-detail.component').then(m => m.InstanceDetailComponent)
  }
];
