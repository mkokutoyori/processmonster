import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../environments/environment';
import {
  Task,
  CreateTaskRequest,
  UpdateTaskRequest,
  TaskComment,
  CreateCommentRequest,
  TaskAttachment,
  PagedResponse,
  TaskStatus
} from '../models/process.model';

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private apiUrl = `${environment.apiUrl}/tasks`;

  constructor(private http: HttpClient) {}

  // Task CRUD
  getAllTasks(page = 0, size = 20, sort = 'dueDate,asc'): Observable<PagedResponse<Task>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString())
      .set('sort', sort);
    return this.http.get<PagedResponse<Task>>(this.apiUrl, { params });
  }

  getTaskById(id: number): Observable<Task> {
    return this.http.get<Task>(`${this.apiUrl}/${id}`);
  }

  createTask(request: CreateTaskRequest): Observable<Task> {
    return this.http.post<Task>(this.apiUrl, request);
  }

  updateTask(id: number, request: UpdateTaskRequest): Observable<Task> {
    return this.http.put<Task>(`${this.apiUrl}/${id}`, request);
  }

  deleteTask(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // Task operations
  claimTask(id: number): Observable<Task> {
    return this.http.put<Task>(`${this.apiUrl}/${id}/claim`, {});
  }

  assignTask(id: number, assignee: string): Observable<Task> {
    const params = new HttpParams().set('assignee', assignee);
    return this.http.put<Task>(`${this.apiUrl}/${id}/assign`, {}, { params });
  }

  startTask(id: number): Observable<Task> {
    return this.http.put<Task>(`${this.apiUrl}/${id}/start`, {});
  }

  completeTask(id: number, formData?: any): Observable<Task> {
    return this.http.put<Task>(`${this.apiUrl}/${id}/complete`, formData || {});
  }

  cancelTask(id: number, reason?: string): Observable<Task> {
    const params = reason ? new HttpParams().set('reason', reason) : new HttpParams();
    return this.http.put<Task>(`${this.apiUrl}/${id}/cancel`, {}, { params });
  }

  // Queries
  getMyTasks(page = 0, size = 20): Observable<PagedResponse<Task>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PagedResponse<Task>>(`${this.apiUrl}/inbox`, { params });
  }

  getMyActiveTasks(page = 0, size = 20): Observable<PagedResponse<Task>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PagedResponse<Task>>(`${this.apiUrl}/inbox/active`, { params });
  }

  getQueueTasks(candidateGroup: string, page = 0, size = 20): Observable<PagedResponse<Task>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PagedResponse<Task>>(`${this.apiUrl}/queue/${candidateGroup}`, { params });
  }

  getTasksByStatus(status: TaskStatus, page = 0, size = 20): Observable<PagedResponse<Task>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PagedResponse<Task>>(`${this.apiUrl}/status/${status}`, { params });
  }

  getOverdueTasks(page = 0, size = 20): Observable<PagedResponse<Task>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PagedResponse<Task>>(`${this.apiUrl}/overdue`, { params });
  }

  getTasksDueSoon(page = 0, size = 20): Observable<PagedResponse<Task>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PagedResponse<Task>>(`${this.apiUrl}/due-soon`, { params });
  }

  searchTasks(keyword: string, page = 0, size = 20): Observable<PagedResponse<Task>> {
    const params = new HttpParams()
      .set('keyword', keyword)
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<PagedResponse<Task>>(`${this.apiUrl}/search`, { params });
  }

  // Comments
  getTaskComments(taskId: number): Observable<TaskComment[]> {
    return this.http.get<TaskComment[]>(`${this.apiUrl}/${taskId}/comments`);
  }

  addComment(taskId: number, request: CreateCommentRequest): Observable<TaskComment> {
    return this.http.post<TaskComment>(`${this.apiUrl}/${taskId}/comments`, request);
  }

  // Attachments
  getTaskAttachments(taskId: number): Observable<TaskAttachment[]> {
    return this.http.get<TaskAttachment[]>(`${this.apiUrl}/${taskId}/attachments`);
  }

  addAttachment(taskId: number, file: File, description?: string): Observable<TaskAttachment> {
    const formData = new FormData();
    formData.append('file', file);
    if (description) {
      formData.append('description', description);
    }
    return this.http.post<TaskAttachment>(`${this.apiUrl}/${taskId}/attachments`, formData);
  }

  deleteAttachment(attachmentId: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/attachments/${attachmentId}`);
  }

  // Statistics
  countMyActiveTasks(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/stats/my-active-count`);
  }

  countOverdueTasks(): Observable<number> {
    return this.http.get<number>(`${this.apiUrl}/stats/overdue-count`);
  }

  // Forms
  getTaskForm(taskId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${taskId}/form`);
  }

  getTaskFormReadOnly(taskId: number): Observable<any> {
    return this.http.get<any>(`${this.apiUrl}/${taskId}/form/readonly`);
  }

  submitTaskForm(taskId: number, formData: any): Observable<Task> {
    return this.http.post<Task>(`${this.apiUrl}/${taskId}/submit-form`, formData);
  }

  validateTaskForm(taskId: number, formData: any): Observable<{ valid: boolean; taskId: number }> {
    return this.http.post<{ valid: boolean; taskId: number }>(`${this.apiUrl}/${taskId}/validate-form`, formData);
  }
}
