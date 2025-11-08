import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Webhook,
  CreateWebhookRequest,
  UpdateWebhookRequest,
  WebhookDelivery,
  PagedResponse
} from '../models/process.model';

/**
 * Service for webhook operations
 */
@Injectable({
  providedIn: 'root'
})
export class WebhookService {
  private apiUrl = `${environment.apiUrl}/webhooks`;

  constructor(private http: HttpClient) {}

  /**
   * Create a new webhook
   */
  createWebhook(request: CreateWebhookRequest): Observable<Webhook> {
    return this.http.post<Webhook>(this.apiUrl, request);
  }

  /**
   * Get webhook by ID
   */
  getWebhookById(id: number): Observable<Webhook> {
    return this.http.get<Webhook>(`${this.apiUrl}/${id}`);
  }

  /**
   * Get all webhooks (paginated)
   */
  getAllWebhooks(page: number = 0, size: number = 20, sort?: string): Observable<PagedResponse<Webhook>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (sort) {
      params = params.set('sort', sort);
    }

    return this.http.get<PagedResponse<Webhook>>(this.apiUrl, { params });
  }

  /**
   * Get enabled webhooks
   */
  getEnabledWebhooks(): Observable<Webhook[]> {
    return this.http.get<Webhook[]>(`${this.apiUrl}/enabled`);
  }

  /**
   * Search webhooks
   */
  searchWebhooks(keyword: string, page: number = 0, size: number = 20): Observable<PagedResponse<Webhook>> {
    const params = new HttpParams()
      .set('keyword', keyword)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<Webhook>>(`${this.apiUrl}/search`, { params });
  }

  /**
   * Update webhook
   */
  updateWebhook(id: number, request: UpdateWebhookRequest): Observable<Webhook> {
    return this.http.put<Webhook>(`${this.apiUrl}/${id}`, request);
  }

  /**
   * Enable webhook
   */
  enableWebhook(id: number): Observable<Webhook> {
    return this.http.put<Webhook>(`${this.apiUrl}/${id}/enable`, {});
  }

  /**
   * Disable webhook
   */
  disableWebhook(id: number): Observable<Webhook> {
    return this.http.put<Webhook>(`${this.apiUrl}/${id}/disable`, {});
  }

  /**
   * Delete webhook
   */
  deleteWebhook(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Test webhook
   */
  testWebhook(id: number): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(`${this.apiUrl}/${id}/test`, {});
  }

  /**
   * Get delivery history for a webhook
   */
  getDeliveryHistory(id: number, page: number = 0, size: number = 20): Observable<PagedResponse<WebhookDelivery>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<WebhookDelivery>>(`${this.apiUrl}/${id}/deliveries`, { params });
  }

  /**
   * Get recent deliveries
   */
  getRecentDeliveries(id: number, hours: number = 24): Observable<WebhookDelivery[]> {
    const params = new HttpParams().set('hours', hours.toString());
    return this.http.get<WebhookDelivery[]>(`${this.apiUrl}/${id}/deliveries/recent`, { params });
  }

  /**
   * Get webhook statistics
   */
  getWebhookStats(): Observable<{ totalWebhooks: number; enabledWebhooks: number }> {
    return this.http.get<{ totalWebhooks: number; enabledWebhooks: number }>(
      `${this.apiUrl}/stats`
    );
  }
}
