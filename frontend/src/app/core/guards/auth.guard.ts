/**
 * Authentication Route Guard
 *
 * Protects routes that require authentication.
 * Redirects to login page if user is not authenticated.
 */
import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  if (authService.isAuthenticated()) {
    return true;
  }

  // Store the attempted URL for redirecting
  authService.setRedirectUrl(state.url);

  // Redirect to login page
  return router.createUrlTree(['/auth/login']);
};
