import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ApiKey,
  ApiKeyCreated,
  CreateApiKeyRequest,
  UpdateApiKeyRequest,
  PagedResponse
} from '../models/process.model';

/**
 * Service for API key operations
 */
@Injectable({
  providedIn: 'root'
})
export class ApiKeyService {
  private apiUrl = `${environment.apiUrl}/api-keys`;

  constructor(private http: HttpClient) {}

  /**
   * Create a new API key
   */
  createApiKey(request: CreateApiKeyRequest): Observable<ApiKeyCreated> {
    return this.http.post<ApiKeyCreated>(this.apiUrl, request);
  }

  /**
   * Get API key by ID
   */
  getApiKeyById(id: number): Observable<ApiKey> {
    return this.http.get<ApiKey>(`${this.apiUrl}/${id}`);
  }

  /**
   * Get all API keys (paginated)
   */
  getAllApiKeys(page: number = 0, size: number = 20, sort?: string): Observable<PagedResponse<ApiKey>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (sort) {
      params = params.set('sort', sort);
    }

    return this.http.get<PagedResponse<ApiKey>>(this.apiUrl, { params });
  }

  /**
   * Get active API keys
   */
  getActiveApiKeys(): Observable<ApiKey[]> {
    return this.http.get<ApiKey[]>(`${this.apiUrl}/active`);
  }

  /**
   * Search API keys
   */
  searchApiKeys(keyword: string, page: number = 0, size: number = 20): Observable<PagedResponse<ApiKey>> {
    const params = new HttpParams()
      .set('keyword', keyword)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<ApiKey>>(`${this.apiUrl}/search`, { params });
  }

  /**
   * Update API key
   */
  updateApiKey(id: number, request: UpdateApiKeyRequest): Observable<ApiKey> {
    return this.http.put<ApiKey>(`${this.apiUrl}/${id}`, request);
  }

  /**
   * Enable API key
   */
  enableApiKey(id: number): Observable<ApiKey> {
    return this.http.put<ApiKey>(`${this.apiUrl}/${id}/enable`, {});
  }

  /**
   * Disable API key
   */
  disableApiKey(id: number): Observable<ApiKey> {
    return this.http.put<ApiKey>(`${this.apiUrl}/${id}/disable`, {});
  }

  /**
   * Delete API key
   */
  deleteApiKey(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Get API key statistics
   */
  getApiKeyStats(): Observable<{ totalKeys: number; activeKeys: number; expiredKeys: number }> {
    return this.http.get<{ totalKeys: number; activeKeys: number; expiredKeys: number }>(
      `${this.apiUrl}/stats`
    );
  }
}
