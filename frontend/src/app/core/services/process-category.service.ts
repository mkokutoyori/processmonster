import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ProcessCategory,
  CreateProcessCategoryRequest,
  UpdateProcessCategoryRequest,
  PagedResponse
} from '../models/process.model';

/**
 * Service for managing process categories
 */
@Injectable({
  providedIn: 'root'
})
export class ProcessCategoryService {
  private apiUrl = `${environment.apiUrl}/process-categories`;

  constructor(private http: HttpClient) {}

  /**
   * Get all process categories (paginated)
   */
  getCategories(page: number = 0, size: number = 20, sort: string = 'displayOrder,asc'): Observable<PagedResponse<ProcessCategory>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    return this.http.get<PagedResponse<ProcessCategory>>(this.apiUrl, { params });
  }

  /**
   * Get all active categories (for dropdowns)
   */
  getActiveCategories(): Observable<ProcessCategory[]> {
    return this.http.get<ProcessCategory[]>(`${this.apiUrl}/active`);
  }

  /**
   * Get category by ID
   */
  getCategoryById(id: number): Observable<ProcessCategory> {
    return this.http.get<ProcessCategory>(`${this.apiUrl}/${id}`);
  }

  /**
   * Get category by code
   */
  getCategoryByCode(code: string): Observable<ProcessCategory> {
    return this.http.get<ProcessCategory>(`${this.apiUrl}/code/${code}`);
  }

  /**
   * Search categories
   */
  searchCategories(keyword: string, page: number = 0, size: number = 20): Observable<PagedResponse<ProcessCategory>> {
    const params = new HttpParams()
      .set('keyword', keyword)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<ProcessCategory>>(`${this.apiUrl}/search`, { params });
  }

  /**
   * Create new category
   */
  createCategory(request: CreateProcessCategoryRequest): Observable<ProcessCategory> {
    return this.http.post<ProcessCategory>(this.apiUrl, request);
  }

  /**
   * Update category
   */
  updateCategory(id: number, request: UpdateProcessCategoryRequest): Observable<ProcessCategory> {
    return this.http.put<ProcessCategory>(`${this.apiUrl}/${id}`, request);
  }

  /**
   * Delete category
   */
  deleteCategory(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Activate category
   */
  activateCategory(id: number): Observable<ProcessCategory> {
    return this.http.put<ProcessCategory>(`${this.apiUrl}/${id}/activate`, {});
  }

  /**
   * Deactivate category
   */
  deactivateCategory(id: number): Observable<ProcessCategory> {
    return this.http.put<ProcessCategory>(`${this.apiUrl}/${id}/deactivate`, {});
  }
}
