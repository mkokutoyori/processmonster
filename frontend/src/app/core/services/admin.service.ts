import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  SystemParameter,
  CreateSystemParameterRequest,
  UpdateSystemParameterRequest,
  UpdateSystemParameterValueRequest,
  SystemConfiguration,
  AdminStats
} from '../models/admin.model';
import { PagedResponse } from '../models/process.model';

/**
 * Service for system administration operations
 */
@Injectable({
  providedIn: 'root'
})
export class AdminService {
  private apiUrl = `${environment.apiUrl}/admin`;

  constructor(private http: HttpClient) {}

  /**
   * Create a system parameter
   */
  createParameter(request: CreateSystemParameterRequest): Observable<SystemParameter> {
    return this.http.post<SystemParameter>(`${this.apiUrl}/parameters`, request);
  }

  /**
   * Get parameter by ID
   */
  getParameterById(id: number): Observable<SystemParameter> {
    return this.http.get<SystemParameter>(`${this.apiUrl}/parameters/${id}`);
  }

  /**
   * Get all parameters (paginated)
   */
  getAllParameters(page: number = 0, size: number = 20, sort?: string): Observable<PagedResponse<SystemParameter>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (sort) {
      params = params.set('sort', sort);
    }

    return this.http.get<PagedResponse<SystemParameter>>(`${this.apiUrl}/parameters`, { params });
  }

  /**
   * Get parameters by category
   */
  getParametersByCategory(category: string): Observable<SystemParameter[]> {
    return this.http.get<SystemParameter[]>(`${this.apiUrl}/parameters/category/${category}`);
  }

  /**
   * Get editable parameters
   */
  getEditableParameters(): Observable<SystemParameter[]> {
    return this.http.get<SystemParameter[]>(`${this.apiUrl}/parameters/editable`);
  }

  /**
   * Get all categories
   */
  getAllCategories(): Observable<string[]> {
    return this.http.get<string[]>(`${this.apiUrl}/parameters/categories`);
  }

  /**
   * Search parameters
   */
  searchParameters(keyword: string, page: number = 0, size: number = 20): Observable<PagedResponse<SystemParameter>> {
    const params = new HttpParams()
      .set('keyword', keyword)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<SystemParameter>>(`${this.apiUrl}/parameters/search`, { params });
  }

  /**
   * Update parameter configuration
   */
  updateParameter(id: number, request: UpdateSystemParameterRequest): Observable<SystemParameter> {
    return this.http.put<SystemParameter>(`${this.apiUrl}/parameters/${id}`, request);
  }

  /**
   * Update parameter value
   */
  updateParameterValue(id: number, value: string): Observable<SystemParameter> {
    const request: UpdateSystemParameterValueRequest = { value };
    return this.http.put<SystemParameter>(`${this.apiUrl}/parameters/${id}/value`, request);
  }

  /**
   * Reset parameter to default value
   */
  resetToDefault(id: number): Observable<SystemParameter> {
    return this.http.put<SystemParameter>(`${this.apiUrl}/parameters/${id}/reset`, {});
  }

  /**
   * Delete parameter
   */
  deleteParameter(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/parameters/${id}`);
  }

  /**
   * Get system configuration as a map
   */
  getSystemConfiguration(): Observable<SystemConfiguration> {
    return this.http.get<SystemConfiguration>(`${this.apiUrl}/config`);
  }

  /**
   * Get system configuration by category
   */
  getSystemConfigurationByCategory(category: string): Observable<SystemConfiguration> {
    return this.http.get<SystemConfiguration>(`${this.apiUrl}/config/${category}`);
  }

  /**
   * Get admin statistics
   */
  getAdminStats(): Observable<AdminStats> {
    return this.http.get<AdminStats>(`${this.apiUrl}/stats`);
  }
}
