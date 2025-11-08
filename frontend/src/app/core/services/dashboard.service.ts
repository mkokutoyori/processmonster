import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  SystemKPIs,
  StatusStats,
  UserTaskStats,
  ProcessDefinitionStats,
  DailyCompletionTrend
} from '../models/process.model';

/**
 * Service for dashboard and metrics operations
 */
@Injectable({
  providedIn: 'root'
})
export class DashboardService {
  private apiUrl = `${environment.apiUrl}/dashboard`;

  constructor(private http: HttpClient) {}

  /**
   * Get system-wide KPIs
   */
  getSystemKPIs(): Observable<SystemKPIs> {
    return this.http.get<SystemKPIs>(`${this.apiUrl}/kpis`);
  }

  /**
   * Get process statistics grouped by status
   */
  getProcessStatsByStatus(): Observable<StatusStats> {
    return this.http.get<StatusStats>(`${this.apiUrl}/process-stats`);
  }

  /**
   * Get task statistics grouped by status
   */
  getTaskStatsByStatus(): Observable<StatusStats> {
    return this.http.get<StatusStats>(`${this.apiUrl}/task-stats`);
  }

  /**
   * Get task statistics grouped by priority
   */
  getTaskStatsByPriority(): Observable<StatusStats> {
    return this.http.get<StatusStats>(`${this.apiUrl}/task-priority-stats`);
  }

  /**
   * Get user task performance statistics
   * @param username Optional username (defaults to current user)
   */
  getUserTaskStats(username?: string): Observable<UserTaskStats> {
    let params = new HttpParams();
    if (username) {
      params = params.set('username', username);
    }
    return this.http.get<UserTaskStats>(`${this.apiUrl}/user-stats`, { params });
  }

  /**
   * Get daily task completion trend
   * @param days Number of days to include (default: 7, max: 90)
   */
  getDailyCompletionTrend(days: number = 7): Observable<DailyCompletionTrend> {
    const params = new HttpParams().set('days', days.toString());
    return this.http.get<DailyCompletionTrend>(`${this.apiUrl}/completion-trend`, { params });
  }

  /**
   * Get statistics for a specific process definition
   * @param processDefinitionKey Process definition key
   */
  getProcessDefinitionStats(processDefinitionKey: string): Observable<ProcessDefinitionStats> {
    return this.http.get<ProcessDefinitionStats>(
      `${this.apiUrl}/process-definition-stats/${processDefinitionKey}`
    );
  }
}
