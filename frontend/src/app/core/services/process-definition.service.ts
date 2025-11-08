import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  ProcessDefinition,
  ProcessDefinitionDetail,
  ProcessVersionInfo,
  CreateProcessDefinitionRequest,
  UpdateProcessDefinitionRequest,
  PagedResponse
} from '../models/process.model';

/**
 * Service for managing process definitions
 */
@Injectable({
  providedIn: 'root'
})
export class ProcessDefinitionService {
  private apiUrl = `${environment.apiUrl}/processes`;

  constructor(private http: HttpClient) {}

  /**
   * Get all processes (latest versions only by default)
   */
  getProcesses(page: number = 0, size: number = 20, sort: string = 'createdAt,desc', allVersions: boolean = false): Observable<PagedResponse<ProcessDefinition>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort)
      .set('allVersions', allVersions.toString());

    return this.http.get<PagedResponse<ProcessDefinition>>(this.apiUrl, { params });
  }

  /**
   * Get process by ID (includes BPMN XML)
   */
  getProcessById(id: number): Observable<ProcessDefinitionDetail> {
    return this.http.get<ProcessDefinitionDetail>(`${this.apiUrl}/${id}`);
  }

  /**
   * Get latest version by process key
   */
  getLatestVersionByKey(processKey: string): Observable<ProcessDefinitionDetail> {
    return this.http.get<ProcessDefinitionDetail>(`${this.apiUrl}/key/${processKey}`);
  }

  /**
   * Get specific version by key and version number
   */
  getProcessByKeyAndVersion(processKey: string, version: number): Observable<ProcessDefinitionDetail> {
    return this.http.get<ProcessDefinitionDetail>(`${this.apiUrl}/key/${processKey}/version/${version}`);
  }

  /**
   * Get all versions of a process
   */
  getAllVersionsByKey(processKey: string): Observable<ProcessVersionInfo[]> {
    return this.http.get<ProcessVersionInfo[]>(`${this.apiUrl}/key/${processKey}/versions`);
  }

  /**
   * Get BPMN XML for a process
   */
  getBpmnXml(id: number): Observable<string> {
    return this.http.get(`${this.apiUrl}/${id}/xml`, { responseType: 'text' });
  }

  /**
   * Get all process templates
   */
  getTemplates(page: number = 0, size: number = 20): Observable<PagedResponse<ProcessDefinition>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<ProcessDefinition>>(`${this.apiUrl}/templates`, { params });
  }

  /**
   * Get processes by category
   */
  getProcessesByCategory(categoryId: number, page: number = 0, size: number = 20): Observable<PagedResponse<ProcessDefinition>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<ProcessDefinition>>(`${this.apiUrl}/category/${categoryId}`, { params });
  }

  /**
   * Search processes
   */
  searchProcesses(keyword: string, page: number = 0, size: number = 20): Observable<PagedResponse<ProcessDefinition>> {
    const params = new HttpParams()
      .set('keyword', keyword)
      .set('page', page.toString())
      .set('size', size.toString());

    return this.http.get<PagedResponse<ProcessDefinition>>(`${this.apiUrl}/search`, { params });
  }

  /**
   * Create new process
   */
  createProcess(request: CreateProcessDefinitionRequest): Observable<ProcessDefinitionDetail> {
    return this.http.post<ProcessDefinitionDetail>(this.apiUrl, request);
  }

  /**
   * Update process (creates new version if BPMN XML changes)
   */
  updateProcess(id: number, request: UpdateProcessDefinitionRequest): Observable<ProcessDefinitionDetail> {
    return this.http.put<ProcessDefinitionDetail>(`${this.apiUrl}/${id}`, request);
  }

  /**
   * Create new version explicitly
   */
  createNewVersion(id: number, bpmnXml: string): Observable<ProcessDefinitionDetail> {
    return this.http.post<ProcessDefinitionDetail>(`${this.apiUrl}/${id}/new-version`, bpmnXml, {
      headers: { 'Content-Type': 'application/xml' }
    });
  }

  /**
   * Import process from BPMN XML
   */
  importProcess(bpmnXml: string, categoryId?: number, asTemplate: boolean = false): Observable<ProcessDefinitionDetail> {
    const params = new HttpParams()
      .set('asTemplate', asTemplate.toString())
      .set('categoryId', categoryId ? categoryId.toString() : '');

    return this.http.post<ProcessDefinitionDetail>(`${this.apiUrl}/import`, bpmnXml, {
      params,
      headers: { 'Content-Type': 'application/xml' }
    });
  }

  /**
   * Delete process
   */
  deleteProcess(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  /**
   * Publish process
   */
  publishProcess(id: number): Observable<ProcessDefinition> {
    return this.http.put<ProcessDefinition>(`${this.apiUrl}/${id}/publish`, {});
  }

  /**
   * Unpublish process
   */
  unpublishProcess(id: number): Observable<ProcessDefinition> {
    return this.http.put<ProcessDefinition>(`${this.apiUrl}/${id}/unpublish`, {});
  }

  /**
   * Download BPMN XML as file
   */
  downloadBpmnXml(id: number, fileName: string): void {
    this.getBpmnXml(id).subscribe({
      next: (xml) => {
        const blob = new Blob([xml], { type: 'application/xml' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `${fileName}.bpmn`;
        link.click();
        window.URL.revokeObjectURL(url);
      },
      error: (error) => {
        console.error('Error downloading BPMN XML:', error);
      }
    });
  }
}
