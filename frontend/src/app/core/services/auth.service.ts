/**
 * Authentication Service
 *
 * Manages user authentication, JWT tokens, and session state.
 *
 * Features:
 * - Login/Logout
 * - JWT access and refresh token management
 * - Automatic token refresh
 * - User session persistence
 */
import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, BehaviorSubject, tap } from 'rxjs';
import { environment } from '../../../environments/environment';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface User {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  roles: string[];
  permissions: string[];
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private readonly ACCESS_TOKEN_KEY = 'access_token';
  private readonly REFRESH_TOKEN_KEY = 'refresh_token';
  private readonly USER_KEY = 'current_user';
  private readonly REDIRECT_URL_KEY = 'redirect_url';

  private currentUserSubject = new BehaviorSubject<User | null>(this.getUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();

  private isAuthenticatedSignal = signal<boolean>(this.hasValidToken());

  /**
   * Login with username and password
   */
  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/login`, credentials)
      .pipe(
        tap(response => {
          this.setSession(response);
          this.currentUserSubject.next(response.user);
          this.isAuthenticatedSignal.set(true);
        })
      );
  }

  /**
   * Logout current user
   */
  logout(): void {
    // Call logout endpoint (optional)
    this.http.post(`${environment.apiUrl}/auth/logout`, {}).subscribe();

    // Clear local storage
    this.clearSession();
    this.currentUserSubject.next(null);
    this.isAuthenticatedSignal.set(false);

    // Redirect to login
    this.router.navigate(['/auth/login']);
  }

  /**
   * Refresh access token using refresh token
   */
  refreshToken(): Observable<LoginResponse> {
    const refreshToken = this.getRefreshToken();

    return this.http.post<LoginResponse>(`${environment.apiUrl}/auth/refresh`, { refreshToken })
      .pipe(
        tap(response => {
          this.setSession(response);
        })
      );
  }

  /**
   * Check if user is authenticated
   */
  isAuthenticated(): boolean {
    return this.hasValidToken();
  }

  /**
   * Get current user
   */
  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  /**
   * Get access token
   */
  getAccessToken(): string | null {
    return localStorage.getItem(this.ACCESS_TOKEN_KEY);
  }

  /**
   * Get refresh token
   */
  getRefreshToken(): string | null {
    return localStorage.getItem(this.REFRESH_TOKEN_KEY);
  }

  /**
   * Check if user has specific role
   */
  hasRole(role: string): boolean {
    const user = this.currentUserSubject.value;
    return user?.roles.includes(role) ?? false;
  }

  /**
   * Check if user has specific permission
   */
  hasPermission(permission: string): boolean {
    const user = this.currentUserSubject.value;
    return user?.permissions.includes(permission) ?? false;
  }

  /**
   * Set redirect URL for after login
   */
  setRedirectUrl(url: string): void {
    sessionStorage.setItem(this.REDIRECT_URL_KEY, url);
  }

  /**
   * Get and clear redirect URL
   */
  getAndClearRedirectUrl(): string {
    const url = sessionStorage.getItem(this.REDIRECT_URL_KEY) || '/dashboard';
    sessionStorage.removeItem(this.REDIRECT_URL_KEY);
    return url;
  }

  /**
   * Store session data in local storage
   */
  private setSession(authResult: LoginResponse): void {
    localStorage.setItem(this.ACCESS_TOKEN_KEY, authResult.accessToken);
    localStorage.setItem(this.REFRESH_TOKEN_KEY, authResult.refreshToken);
    localStorage.setItem(this.USER_KEY, JSON.stringify(authResult.user));
  }

  /**
   * Clear session data from local storage
   */
  private clearSession(): void {
    localStorage.removeItem(this.ACCESS_TOKEN_KEY);
    localStorage.removeItem(this.REFRESH_TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
  }

  /**
   * Check if access token exists and is valid
   */
  private hasValidToken(): boolean {
    const token = this.getAccessToken();
    if (!token) {
      return false;
    }

    // TODO: Decode JWT and check expiration
    // For now, just check if token exists
    return true;
  }

  /**
   * Get user from local storage
   */
  private getUserFromStorage(): User | null {
    const userJson = localStorage.getItem(this.USER_KEY);
    return userJson ? JSON.parse(userJson) : null;
  }
}
