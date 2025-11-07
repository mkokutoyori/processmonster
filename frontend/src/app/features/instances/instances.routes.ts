/**
 * Process Instance Management Routes
 */
import { Routes } from '@angular/router';

export const INSTANCE_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./instance-list.component').then(m => m.InstanceListComponent)
  }
  // TODO: Add routes for detail view when component is ready
  // {
  //   path: ':id',
  //   loadComponent: () => import('./instance-detail.component').then(m => m.InstanceDetailComponent)
  // },
  // {
  //   path: ':id/history',
  //   loadComponent: () => import('./instance-history.component').then(m => m.InstanceHistoryComponent)
  // }
];
