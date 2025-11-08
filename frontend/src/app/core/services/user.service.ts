/**
 * User Service
 *
 * Handles all user-related API calls.
 */
import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import { User, CreateUserRequest, UpdateUserRequest, PagedResponse } from '../models/user.model';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/users`;

  /**
   * Get all users (paginated)
   */
  getUsers(page: number = 0, size: number = 20, sort: string = 'createdAt,desc'): Observable<PagedResponse<User>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    return this.http.get<PagedResponse<User>>(this.apiUrl, { params });
  }

  /**
   * Search users by keyword
   */
  searchUsers(keyword: string, page: number = 0, size: number = 20): Observable<PagedResponse<User>> {
    const params = new HttpParams()
      .set('keyword', keyword)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<User>>(`${this.apiUrl}/search`, { params });
  }

  /**
   * Get users by role
   */
  getUsersByRole(roleName: string, page: number = 0, size: number = 20): Observable<PagedResponse<User>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<User>>(`${this.apiUrl}/by-role/${roleName}`, { params });
  }

  /**
   * Get user by ID
   */
  getUserById(id: number): Observable<User> {
    return this.http.get<User>(`${this.apiUrl}/${id}`);
  }

  /**
   * Create new user
   */
  createUser(user: CreateUserRequest): Observable<User> {
    return this.http.post<User>(this.apiUrl, user);
  }

  /**
   * Update existing user
   */
  updateUser(id: number, user: UpdateUserRequest): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}`, user);
  }

  /**
   * Delete user (soft delete)
   */
  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Activate user
   */
  activateUser(id: number): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}/activate`, {});
  }

  /**
   * Deactivate user
   */
  deactivateUser(id: number): Observable<User> {
    return this.http.put<User>(`${this.apiUrl}/${id}/deactivate`, {});
  }

  /**
   * Change user password
   */
  changePassword(id: number, newPassword: string): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/password`, newPassword);
  }
}
