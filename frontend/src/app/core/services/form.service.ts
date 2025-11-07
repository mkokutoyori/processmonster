import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  FormDefinition,
  CreateFormDefinitionRequest,
  UpdateFormDefinitionRequest,
  FormSubmission,
  SaveDraftRequest,
  SubmitFormRequest,
  SubmissionStatus,
  ValidationResponse,
  PagedResponse
} from '../models/process.model';

@Injectable({
  providedIn: 'root'
})
export class FormService {
  private apiUrl = `${environment.apiUrl}/forms`;

  constructor(private http: HttpClient) {}

  // ========== FormDefinition Operations ==========

  /**
   * Get all form definitions
   */
  getAllFormDefinitions(page = 0, size = 20, sort = 'name,asc'): Observable<PagedResponse<FormDefinition>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);
    return this.http.get<PagedResponse<FormDefinition>>(`${this.apiUrl}/definitions`, { params });
  }

  /**
   * Get form definition by ID
   */
  getFormDefinitionById(id: number): Observable<FormDefinition> {
    return this.http.get<FormDefinition>(`${this.apiUrl}/definitions/${id}`);
  }

  /**
   * Get form definition by key (latest version)
   */
  getFormDefinitionByKey(formKey: string): Observable<FormDefinition> {
    return this.http.get<FormDefinition>(`${this.apiUrl}/definitions/key/${formKey}`);
  }

  /**
   * Get all versions of a form
   */
  getFormVersions(formKey: string): Observable<FormDefinition[]> {
    return this.http.get<FormDefinition[]>(`${this.apiUrl}/definitions/key/${formKey}/versions`);
  }

  /**
   * Get published forms
   */
  getPublishedForms(page = 0, size = 20): Observable<PagedResponse<FormDefinition>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PagedResponse<FormDefinition>>(`${this.apiUrl}/definitions/published`, { params });
  }

  /**
   * Get forms by category
   */
  getFormsByCategory(category: string, page = 0, size = 20): Observable<PagedResponse<FormDefinition>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PagedResponse<FormDefinition>>(`${this.apiUrl}/definitions/category/${category}`, { params });
  }

  /**
   * Search forms
   */
  searchForms(keyword: string, page = 0, size = 20): Observable<PagedResponse<FormDefinition>> {
    const params = new HttpParams()
      .set('keyword', keyword)
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PagedResponse<FormDefinition>>(`${this.apiUrl}/definitions/search`, { params });
  }

  /**
   * Create form definition
   */
  createFormDefinition(request: CreateFormDefinitionRequest): Observable<FormDefinition> {
    return this.http.post<FormDefinition>(`${this.apiUrl}/definitions`, request);
  }

  /**
   * Update form definition
   */
  updateFormDefinition(id: number, request: UpdateFormDefinitionRequest): Observable<FormDefinition> {
    return this.http.put<FormDefinition>(`${this.apiUrl}/definitions/${id}`, request);
  }

  /**
   * Publish form definition
   */
  publishFormDefinition(id: number): Observable<FormDefinition> {
    return this.http.put<FormDefinition>(`${this.apiUrl}/definitions/${id}/publish`, {});
  }

  /**
   * Unpublish form definition
   */
  unpublishFormDefinition(id: number): Observable<FormDefinition> {
    return this.http.put<FormDefinition>(`${this.apiUrl}/definitions/${id}/unpublish`, {});
  }

  /**
   * Delete form definition
   */
  deleteFormDefinition(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/definitions/${id}`);
  }

  /**
   * Validate JSON Schema
   */
  validateSchema(schemaJson: string): Observable<ValidationResponse> {
    return this.http.post<ValidationResponse>(`${this.apiUrl}/definitions/validate-schema`, schemaJson, {
      headers: { 'Content-Type': 'application/json' }
    });
  }

  // ========== FormSubmission Operations ==========

  /**
   * Get all submissions
   */
  getAllSubmissions(page = 0, size = 20, sort = 'createdAt,desc'): Observable<PagedResponse<FormSubmission>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);
    return this.http.get<PagedResponse<FormSubmission>>(`${this.apiUrl}/submissions`, { params });
  }

  /**
   * Get submission by ID
   */
  getSubmissionById(id: number): Observable<FormSubmission> {
    return this.http.get<FormSubmission>(`${this.apiUrl}/submissions/${id}`);
  }

  /**
   * Get current user's submissions
   */
  getMySubmissions(page = 0, size = 20): Observable<PagedResponse<FormSubmission>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PagedResponse<FormSubmission>>(`${this.apiUrl}/submissions/my`, { params });
  }

  /**
   * Get current user's draft submissions
   */
  getMyDrafts(page = 0, size = 20): Observable<PagedResponse<FormSubmission>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PagedResponse<FormSubmission>>(`${this.apiUrl}/submissions/my/drafts`, { params });
  }

  /**
   * Get submissions by form definition
   */
  getSubmissionsByForm(formDefinitionId: number, page = 0, size = 20): Observable<PagedResponse<FormSubmission>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PagedResponse<FormSubmission>>(
      `${this.apiUrl}/submissions/definition/${formDefinitionId}`,
      { params }
    );
  }

  /**
   * Get pending submissions for review
   */
  getPendingSubmissions(page = 0, size = 20): Observable<PagedResponse<FormSubmission>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PagedResponse<FormSubmission>>(`${this.apiUrl}/submissions/pending`, { params });
  }

  /**
   * Get submissions by status
   */
  getSubmissionsByStatus(status: SubmissionStatus, page = 0, size = 20): Observable<PagedResponse<FormSubmission>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PagedResponse<FormSubmission>>(`${this.apiUrl}/submissions/status/${status}`, { params });
  }

  /**
   * Save draft (no validation)
   */
  saveDraft(request: SaveDraftRequest): Observable<FormSubmission> {
    return this.http.post<FormSubmission>(`${this.apiUrl}/submissions/save-draft`, request);
  }

  /**
   * Submit form (validates against schema)
   */
  submitForm(request: SubmitFormRequest): Observable<FormSubmission> {
    return this.http.post<FormSubmission>(`${this.apiUrl}/submissions/submit`, request);
  }

  /**
   * Validate submission data
   */
  validateSubmission(id: number, dataJson: string): Observable<ValidationResponse> {
    return this.http.post<ValidationResponse>(
      `${this.apiUrl}/submissions/${id}/validate`,
      dataJson,
      { headers: { 'Content-Type': 'application/json' } }
    );
  }

  /**
   * Approve submission
   */
  approveSubmission(id: number, notes?: string): Observable<FormSubmission> {
    const body = notes ? { notes } : {};
    return this.http.put<FormSubmission>(`${this.apiUrl}/submissions/${id}/approve`, body);
  }

  /**
   * Reject submission
   */
  rejectSubmission(id: number, notes?: string): Observable<FormSubmission> {
    const body = notes ? { notes } : {};
    return this.http.put<FormSubmission>(`${this.apiUrl}/submissions/${id}/reject`, body);
  }

  /**
   * Update draft submission
   */
  updateDraft(id: number, dataJson: string): Observable<FormSubmission> {
    return this.http.put<FormSubmission>(
      `${this.apiUrl}/submissions/${id}/update-draft`,
      dataJson,
      { headers: { 'Content-Type': 'application/json' } }
    );
  }

  /**
   * Delete submission
   */
  deleteSubmission(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/submissions/${id}`);
  }

  // ========== Helper Methods ==========

  /**
   * Parse JSON Schema from string
   */
  parseSchema(schemaJson: string): any {
    try {
      return JSON.parse(schemaJson);
    } catch (e) {
      console.error('Invalid JSON Schema:', e);
      return null;
    }
  }

  /**
   * Parse form data from string
   */
  parseFormData(dataJson: string): any {
    try {
      return JSON.parse(dataJson);
    } catch (e) {
      console.error('Invalid form data JSON:', e);
      return null;
    }
  }

  /**
   * Stringify form data
   */
  stringifyFormData(data: any): string {
    return JSON.stringify(data);
  }

  /**
   * Stringify JSON Schema
   */
  stringifySchema(schema: any): string {
    return JSON.stringify(schema, null, 2);
  }
}
