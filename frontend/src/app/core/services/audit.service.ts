import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  AuditLog,
  AuditStats,
  FailedLoginCount
} from '../models/audit.model';
import { PagedResponse } from '../models/process.model';

/**
 * Service for audit log operations
 */
@Injectable({
  providedIn: 'root'
})
export class AuditService {
  private apiUrl = `${environment.apiUrl}/audit`;

  constructor(private http: HttpClient) {}

  /**
   * Get all audit logs (paginated)
   */
  getAllLogs(page: number = 0, size: number = 20, sort?: string): Observable<PagedResponse<AuditLog>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (sort) {
      params = params.set('sort', sort);
    }

    return this.http.get<PagedResponse<AuditLog>>(this.apiUrl, { params });
  }

  /**
   * Get logs by username
   */
  getLogsByUsername(username: string, page: number = 0, size: number = 20): Observable<PagedResponse<AuditLog>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<AuditLog>>(`${this.apiUrl}/username/${username}`, { params });
  }

  /**
   * Get logs by action
   */
  getLogsByAction(action: string, page: number = 0, size: number = 20): Observable<PagedResponse<AuditLog>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<AuditLog>>(`${this.apiUrl}/action/${action}`, { params });
  }

  /**
   * Get logs by entity
   */
  getLogsByEntity(
    entityType: string,
    entityId: number,
    page: number = 0,
    size: number = 20
  ): Observable<PagedResponse<AuditLog>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<AuditLog>>(
      `${this.apiUrl}/entity/${entityType}/${entityId}`,
      { params }
    );
  }

  /**
   * Get logs by date range
   */
  getLogsByDateRange(
    startDate: string,
    endDate: string,
    page: number = 0,
    size: number = 20
  ): Observable<PagedResponse<AuditLog>> {
    const params = new HttpParams()
      .set('startDate', startDate)
      .set('endDate', endDate)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<AuditLog>>(`${this.apiUrl}/daterange`, { params });
  }

  /**
   * Get security logs
   */
  getSecurityLogs(page: number = 0, size: number = 20): Observable<PagedResponse<AuditLog>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<AuditLog>>(`${this.apiUrl}/security`, { params });
  }

  /**
   * Get failed actions
   */
  getFailedActions(page: number = 0, size: number = 20): Observable<PagedResponse<AuditLog>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<AuditLog>>(`${this.apiUrl}/failed`, { params });
  }

  /**
   * Search audit logs
   */
  searchLogs(keyword: string, page: number = 0, size: number = 20): Observable<PagedResponse<AuditLog>> {
    const params = new HttpParams()
      .set('keyword', keyword)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<AuditLog>>(`${this.apiUrl}/search`, { params });
  }

  /**
   * Get recent user logs
   */
  getRecentUserLogs(username: string, hours: number = 24): Observable<AuditLog[]> {
    const params = new HttpParams().set('hours', hours.toString());

    return this.http.get<AuditLog[]>(`${this.apiUrl}/user/${username}/recent`, { params });
  }

  /**
   * Get failed login count
   */
  getFailedLoginCount(username: string, minutes: number = 30): Observable<FailedLoginCount> {
    const params = new HttpParams().set('minutes', minutes.toString());

    return this.http.get<FailedLoginCount>(
      `${this.apiUrl}/failed-logins/${username}`,
      { params }
    );
  }

  /**
   * Get audit statistics
   */
  getAuditStats(): Observable<AuditStats> {
    return this.http.get<AuditStats>(`${this.apiUrl}/stats`);
  }
}
