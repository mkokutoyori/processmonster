/**
 * Task Management Routes
 */
import { Routes } from '@angular/router';

export const TASK_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./task-inbox.component').then(m => m.TaskInboxComponent)
  },
  {
    path: ':id',
    loadComponent: () => import('./task-detail.component').then(m => m.TaskDetailComponent)
  }
];
