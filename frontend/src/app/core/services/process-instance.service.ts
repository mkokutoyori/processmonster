import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ProcessInstance,
  StartProcessInstanceRequest,
  ExecutionHistory,
  PagedResponse
} from '../models/process.model';

/**
 * Service for managing process instances
 */
@Injectable({
  providedIn: 'root'
})
export class ProcessInstanceService {
  private apiUrl = `${environment.apiUrl}/instances`;

  constructor(private http: HttpClient) {}

  /**
   * Start a new process instance
   */
  startProcess(request: StartProcessInstanceRequest): Observable<ProcessInstance> {
    return this.http.post<ProcessInstance>(`${this.apiUrl}/start`, request);
  }

  /**
   * Get all process instances
   */
  getInstances(page: number = 0, size: number = 20, sort: string = 'startTime,desc'): Observable<PagedResponse<ProcessInstance>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);

    return this.http.get<PagedResponse<ProcessInstance>>(this.apiUrl, { params });
  }

  /**
   * Get instance by ID
   */
  getInstanceById(id: number): Observable<ProcessInstance> {
    return this.http.get<ProcessInstance>(`${this.apiUrl}/${id}`);
  }

  /**
   * Get instances by status
   */
  getInstancesByStatus(status: string, page: number = 0, size: number = 20): Observable<PagedResponse<ProcessInstance>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<ProcessInstance>>(`${this.apiUrl}/status/${status}`, { params });
  }

  /**
   * Get active instances
   */
  getActiveInstances(page: number = 0, size: number = 20): Observable<PagedResponse<ProcessInstance>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<ProcessInstance>>(`${this.apiUrl}/active`, { params });
  }

  /**
   * Suspend instance
   */
  suspendInstance(id: number, reason?: string): Observable<ProcessInstance> {
    const params = reason ? new HttpParams().set('reason', reason) : undefined;
    return this.http.put<ProcessInstance>(`${this.apiUrl}/${id}/suspend`, {}, { params });
  }

  /**
   * Resume instance
   */
  resumeInstance(id: number): Observable<ProcessInstance> {
    return this.http.put<ProcessInstance>(`${this.apiUrl}/${id}/resume`, {});
  }

  /**
   * Terminate instance
   */
  terminateInstance(id: number, reason?: string): Observable<ProcessInstance> {
    const params = reason ? new HttpParams().set('reason', reason) : undefined;
    return this.http.put<ProcessInstance>(`${this.apiUrl}/${id}/terminate`, {}, { params });
  }

  /**
   * Get variables for an instance
   */
  getVariables(id: number): Observable<{ [key: string]: any }> {
    return this.http.get<{ [key: string]: any }>(`${this.apiUrl}/${id}/variables`);
  }

  /**
   * Set variables for an instance
   */
  setVariables(id: number, variables: { [key: string]: any }): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/${id}/variables`, variables);
  }

  /**
   * Get execution history
   */
  getHistory(id: number): Observable<ExecutionHistory[]> {
    return this.http.get<ExecutionHistory[]>(`${this.apiUrl}/${id}/history`);
  }

  /**
   * Delete instance
   */
  deleteInstance(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
