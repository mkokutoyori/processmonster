import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatTabsModule } from '@angular/material/tabs';
import { MatListModule } from '@angular/material/list';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TaskService } from '../../core/services/task.service';
import { NotificationService } from '../../core/services/notification.service';
import { Task, TaskComment, TaskAttachment, TaskStatus, TaskPriority } from '../../core/models/process.model';

/**
 * Component for viewing task details
 */
@Component({
  selector: 'app-task-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatTabsModule,
    MatListModule,
    MatTooltipModule,
    MatFormFieldModule,
    MatInputModule,
    ReactiveFormsModule
  ],
  template: `
    <div class="task-detail-container" *ngIf="task">
      <div class="header">
        <div class="title-section">
          <button mat-icon-button (click)="goBack()" matTooltip="Back to inbox">
            <mat-icon>arrow_back</mat-icon>
          </button>
          <div class="title-info">
            <h1>{{ task.name }}</h1>
            <div class="meta-info">
              <mat-chip [ngClass]="getPriorityClass(task.priority)">
                <mat-icon>{{ getPriorityIcon(task.priority) }}</mat-icon>
                {{ task.priority }}
              </mat-chip>
              <mat-chip [ngClass]="getStatusClass(task.status)">
                <mat-icon>{{ getStatusIcon(task.status) }}</mat-icon>
                {{ task.status }}
              </mat-chip>
              <mat-chip *ngIf="task.isOverdue" class="overdue-chip">
                <mat-icon>warning</mat-icon>
                OVERDUE
              </mat-chip>
            </div>
          </div>
        </div>
        <div class="action-buttons">
          <button mat-raised-button
                  color="primary"
                  *ngIf="!task.assignee && task.status === 'CREATED'"
                  (click)="claimTask()">
            <mat-icon>person_add</mat-icon>
            Claim
          </button>
          <button mat-raised-button
                  color="accent"
                  *ngIf="task.assignee && task.status === 'ASSIGNED'"
                  (click)="startTask()">
            <mat-icon>play_arrow</mat-icon>
            Start
          </button>
          <button mat-raised-button
                  color="primary"
                  *ngIf="task.status === 'IN_PROGRESS'"
                  (click)="completeTask()">
            <mat-icon>check_circle</mat-icon>
            Complete
          </button>
          <button mat-stroked-button
                  color="warn"
                  *ngIf="task.isActive"
                  (click)="cancelTask()">
            <mat-icon>cancel</mat-icon>
            Cancel
          </button>
          <button mat-stroked-button (click)="refresh()">
            <mat-icon>refresh</mat-icon>
            Refresh
          </button>
        </div>
      </div>

      <mat-tab-group>
        <!-- Overview Tab -->
        <mat-tab label="Overview">
          <div class="tab-content">
            <mat-card class="info-card">
              <mat-card-header>
                <mat-card-title>Task Information</mat-card-title>
              </mat-card-header>
              <mat-card-content>
                <div class="info-grid">
                  <div class="info-item">
                    <label>Task ID</label>
                    <code>{{ task.id }}</code>
                  </div>
                  <div class="info-item">
                    <label>Name</label>
                    <strong>{{ task.name }}</strong>
                  </div>
                  <div class="info-item full-width" *ngIf="task.description">
                    <label>Description</label>
                    <p class="description">{{ task.description }}</p>
                  </div>
                  <div class="info-item">
                    <label>Priority</label>
                    <mat-chip [ngClass]="getPriorityClass(task.priority)">
                      <mat-icon>{{ getPriorityIcon(task.priority) }}</mat-icon>
                      {{ task.priority }}
                    </mat-chip>
                  </div>
                  <div class="info-item">
                    <label>Status</label>
                    <mat-chip [ngClass]="getStatusClass(task.status)">
                      <mat-icon>{{ getStatusIcon(task.status) }}</mat-icon>
                      {{ task.status }}
                    </mat-chip>
                  </div>
                  <div class="info-item">
                    <label>Assignee</label>
                    <span *ngIf="task.assignee">{{ task.assignee }}</span>
                    <span *ngIf="!task.assignee" class="no-value">Unassigned</span>
                  </div>
                  <div class="info-item">
                    <label>Candidate Group</label>
                    <span *ngIf="task.candidateGroup">{{ task.candidateGroup }}</span>
                    <span *ngIf="!task.candidateGroup" class="no-value">-</span>
                  </div>
                  <div class="info-item">
                    <label>Due Date</label>
                    <span *ngIf="task.dueDate" [ngClass]="{ 'overdue-text': task.isOverdue }">
                      {{ task.dueDate | date:'medium' }}
                    </span>
                    <span *ngIf="!task.dueDate" class="no-value">-</span>
                  </div>
                  <div class="info-item">
                    <label>Follow-up Date</label>
                    <span *ngIf="task.followUpDate">{{ task.followUpDate | date:'medium' }}</span>
                    <span *ngIf="!task.followUpDate" class="no-value">-</span>
                  </div>
                  <div class="info-item">
                    <label>Created</label>
                    <span>{{ task.createdAt | date:'medium' }}</span>
                  </div>
                  <div class="info-item">
                    <label>Created By</label>
                    <span>{{ task.createdBy }}</span>
                  </div>
                  <div class="info-item" *ngIf="task.claimedDate">
                    <label>Claimed</label>
                    <span>{{ task.claimedDate | date:'medium' }}</span>
                  </div>
                  <div class="info-item" *ngIf="task.claimedBy">
                    <label>Claimed By</label>
                    <span>{{ task.claimedBy }}</span>
                  </div>
                  <div class="info-item" *ngIf="task.completedDate">
                    <label>Completed</label>
                    <span>{{ task.completedDate | date:'medium' }}</span>
                  </div>
                  <div class="info-item" *ngIf="task.completedBy">
                    <label>Completed By</label>
                    <span>{{ task.completedBy }}</span>
                  </div>
                  <div class="info-item" *ngIf="task.processInstanceId">
                    <label>Process Instance</label>
                    <code (click)="viewInstance()" class="clickable">
                      {{ task.processInstanceBusinessKey || task.processInstanceId }}
                    </code>
                  </div>
                  <div class="info-item" *ngIf="task.processDefinitionName">
                    <label>Process</label>
                    <span>{{ task.processDefinitionName }}</span>
                  </div>
                </div>
              </mat-card-content>
            </mat-card>
          </div>
        </mat-tab>

        <!-- Comments Tab -->
        <mat-tab label="Comments ({{ task.commentCount }})">
          <div class="tab-content">
            <mat-card class="comments-card">
              <mat-card-header>
                <mat-card-title>Comments</mat-card-title>
                <button mat-icon-button (click)="loadComments()" matTooltip="Refresh comments">
                  <mat-icon>refresh</mat-icon>
                </button>
              </mat-card-header>
              <mat-card-content>
                <!-- Add Comment Form -->
                <form [formGroup]="commentForm" (ngSubmit)="addComment()" class="comment-form">
                  <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Add a comment</mat-label>
                    <textarea matInput
                              formControlName="content"
                              rows="3"
                              placeholder="Write your comment here..."></textarea>
                  </mat-form-field>
                  <button mat-raised-button color="primary" type="submit" [disabled]="!commentForm.valid">
                    <mat-icon>add_comment</mat-icon>
                    Add Comment
                  </button>
                </form>

                <!-- Comments List -->
                <mat-list *ngIf="comments && comments.length > 0" class="comments-list">
                  <mat-list-item *ngFor="let comment of comments">
                    <div class="comment-item">
                      <div class="comment-header">
                        <mat-chip [ngClass]="getCommentTypeClass(comment.type)">
                          {{ comment.type }}
                        </mat-chip>
                        <span class="comment-author">{{ comment.createdBy }}</span>
                        <span class="comment-time">{{ comment.createdAt | date:'short' }}</span>
                      </div>
                      <div class="comment-content">
                        {{ comment.content }}
                      </div>
                    </div>
                  </mat-list-item>
                </mat-list>

                <p *ngIf="!comments || comments.length === 0" class="no-data">
                  No comments yet
                </p>
              </mat-card-content>
            </mat-card>
          </div>
        </mat-tab>

        <!-- Attachments Tab -->
        <mat-tab label="Attachments ({{ task.attachmentCount }})">
          <div class="tab-content">
            <mat-card class="attachments-card">
              <mat-card-header>
                <mat-card-title>Attachments</mat-card-title>
                <button mat-icon-button (click)="loadAttachments()" matTooltip="Refresh attachments">
                  <mat-icon>refresh</mat-icon>
                </button>
              </mat-card-header>
              <mat-card-content>
                <!-- Add Attachment -->
                <div class="upload-section">
                  <input type="file"
                         #fileInput
                         (change)="onFileSelected($event)"
                         style="display: none">
                  <button mat-raised-button color="primary" (click)="fileInput.click()">
                    <mat-icon>attach_file</mat-icon>
                    Upload Attachment
                  </button>
                  <span *ngIf="selectedFile" class="selected-file">{{ selectedFile.name }}</span>
                </div>

                <!-- Attachments List -->
                <mat-list *ngIf="attachments && attachments.length > 0" class="attachments-list">
                  <mat-list-item *ngFor="let attachment of attachments">
                    <div class="attachment-item">
                      <mat-icon class="file-icon">{{ getFileIcon(attachment.mimeType) }}</mat-icon>
                      <div class="attachment-info">
                        <strong>{{ attachment.fileName }}</strong>
                        <small>
                          {{ attachment.formattedSize }} - {{ attachment.mimeType }}
                          - uploaded by {{ attachment.uploadedBy }} on {{ attachment.createdAt | date:'short' }}
                        </small>
                        <p *ngIf="attachment.description">{{ attachment.description }}</p>
                      </div>
                      <button mat-icon-button color="warn" (click)="deleteAttachment(attachment)" matTooltip="Delete">
                        <mat-icon>delete</mat-icon>
                      </button>
                    </div>
                  </mat-list-item>
                </mat-list>

                <p *ngIf="!attachments || attachments.length === 0" class="no-data">
                  No attachments yet
                </p>
              </mat-card-content>
            </mat-card>
          </div>
        </mat-tab>
      </mat-tab-group>
    </div>
  `,
  styles: [`
    .task-detail-container {
      padding: 24px;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 24px;
      gap: 16px;
    }

    .title-section {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      flex: 1;
    }

    .title-info h1 {
      margin: 0 0 12px 0;
    }

    .meta-info {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }

    .action-buttons {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }

    .tab-content {
      padding: 24px 0;
    }

    .info-card, .comments-card, .attachments-card {
      margin-bottom: 16px;
    }

    mat-card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .info-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 16px;
      margin-top: 16px;
    }

    .info-item {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .info-item.full-width {
      grid-column: 1 / -1;
    }

    .info-item label {
      font-weight: 600;
      color: #666;
      font-size: 12px;
      text-transform: uppercase;
    }

    .description {
      margin: 8px 0;
      line-height: 1.5;
    }

    code {
      background: #f5f5f5;
      padding: 4px 8px;
      border-radius: 4px;
      font-family: 'Courier New', monospace;
      font-size: 13px;
    }

    code.clickable {
      cursor: pointer;
      color: #1976d2;
    }

    code.clickable:hover {
      background: #e3f2fd;
    }

    .no-value {
      color: #999;
      font-style: italic;
    }

    .overdue-text {
      color: #f44336;
      font-weight: 600;
    }

    .overdue-chip {
      background-color: #f44336 !important;
      color: white !important;
    }

    /* Comment Section */
    .comment-form {
      margin-bottom: 24px;
      display: flex;
      flex-direction: column;
      gap: 12px;
    }

    .comment-form .full-width {
      width: 100%;
    }

    .comments-list {
      margin-top: 16px;
    }

    .comment-item {
      width: 100%;
      padding: 12px;
      border: 1px solid #e0e0e0;
      border-radius: 8px;
      margin-bottom: 12px;
    }

    .comment-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 8px;
    }

    .comment-author {
      font-weight: 600;
      color: #333;
    }

    .comment-time {
      color: #999;
      font-size: 12px;
    }

    .comment-content {
      padding: 8px 0;
      line-height: 1.5;
    }

    .comment-type-GENERAL {
      background-color: #2196f3 !important;
      color: white !important;
    }

    .comment-type-QUESTION {
      background-color: #ff9800 !important;
      color: white !important;
    }

    .comment-type-DECISION {
      background-color: #9c27b0 !important;
      color: white !important;
    }

    .comment-type-ESCALATION {
      background-color: #f44336 !important;
      color: white !important;
    }

    .comment-type-RESOLUTION {
      background-color: #4caf50 !important;
      color: white !important;
    }

    /* Attachment Section */
    .upload-section {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 24px;
    }

    .selected-file {
      color: #666;
      font-size: 14px;
    }

    .attachments-list {
      margin-top: 16px;
    }

    .attachment-item {
      width: 100%;
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 12px;
      border: 1px solid #e0e0e0;
      border-radius: 8px;
      margin-bottom: 12px;
    }

    .file-icon {
      color: #666;
      font-size: 32px;
      width: 32px;
      height: 32px;
    }

    .attachment-info {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .attachment-info small {
      color: #999;
      font-size: 12px;
    }

    .no-data {
      text-align: center;
      color: #999;
      padding: 24px;
      font-style: italic;
    }

    /* Priority chip colors */
    mat-chip {
      display: inline-flex;
      align-items: center;
      gap: 4px;
    }

    mat-chip mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
    }

    .priority-LOW {
      background-color: #9e9e9e !important;
      color: white !important;
    }

    .priority-NORMAL {
      background-color: #2196f3 !important;
      color: white !important;
    }

    .priority-HIGH {
      background-color: #ff9800 !important;
      color: white !important;
    }

    .priority-CRITICAL {
      background-color: #f44336 !important;
      color: white !important;
    }

    /* Status chip colors */
    .status-CREATED {
      background-color: #9e9e9e !important;
      color: white !important;
    }

    .status-ASSIGNED {
      background-color: #2196f3 !important;
      color: white !important;
    }

    .status-IN_PROGRESS {
      background-color: #ff9800 !important;
      color: white !important;
    }

    .status-COMPLETED {
      background-color: #4caf50 !important;
      color: white !important;
    }

    .status-CANCELLED {
      background-color: #757575 !important;
      color: white !important;
    }
  `]
})
export class TaskDetailComponent implements OnInit {
  task: Task | null = null;
  comments: TaskComment[] = [];
  attachments: TaskAttachment[] = [];
  selectedFile: File | null = null;

  commentForm: FormGroup;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private taskService: TaskService,
    private notificationService: NotificationService,
    private fb: FormBuilder
  ) {
    this.commentForm = this.fb.group({
      content: ['', Validators.required]
    });
  }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadTask(+id);
      this.loadComments();
      this.loadAttachments();
    }
  }

  loadTask(id: number): void {
    this.taskService.getTaskById(id).subscribe({
      next: (task) => {
        this.task = task;
      },
      error: (error) => {
        console.error('Error loading task:', error);
        this.notificationService.error('Failed to load task details');
      }
    });
  }

  loadComments(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.taskService.getTaskComments(+id).subscribe({
        next: (comments) => {
          this.comments = comments;
        },
        error: (error) => {
          console.error('Error loading comments:', error);
        }
      });
    }
  }

  loadAttachments(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.taskService.getTaskAttachments(+id).subscribe({
        next: (attachments) => {
          this.attachments = attachments;
        },
        error: (error) => {
          console.error('Error loading attachments:', error);
        }
      });
    }
  }

  refresh(): void {
    if (this.task) {
      this.loadTask(this.task.id);
    }
  }

  goBack(): void {
    this.router.navigate(['/tasks']);
  }

  viewInstance(): void {
    if (this.task?.processInstanceId) {
      this.router.navigate(['/instances', this.task.processInstanceId]);
    }
  }

  claimTask(): void {
    if (this.task) {
      this.taskService.claimTask(this.task.id).subscribe({
        next: () => {
          this.notificationService.success('Task claimed successfully');
          this.loadTask(this.task!.id);
        },
        error: (error) => {
          console.error('Error claiming task:', error);
          this.notificationService.error(error.error?.message || 'Failed to claim task');
        }
      });
    }
  }

  startTask(): void {
    if (this.task) {
      this.taskService.startTask(this.task.id).subscribe({
        next: () => {
          this.notificationService.success('Task started successfully');
          this.loadTask(this.task!.id);
        },
        error: (error) => {
          console.error('Error starting task:', error);
          this.notificationService.error(error.error?.message || 'Failed to start task');
        }
      });
    }
  }

  completeTask(): void {
    if (this.task) {
      this.taskService.completeTask(this.task.id).subscribe({
        next: () => {
          this.notificationService.success('Task completed successfully');
          this.loadTask(this.task!.id);
        },
        error: (error) => {
          console.error('Error completing task:', error);
          this.notificationService.error(error.error?.message || 'Failed to complete task');
        }
      });
    }
  }

  cancelTask(): void {
    if (this.task) {
      const reason = prompt('Reason for cancellation (optional):');
      if (confirm('Are you sure you want to cancel this task?')) {
        this.taskService.cancelTask(this.task.id, reason || undefined).subscribe({
          next: () => {
            this.notificationService.success('Task cancelled successfully');
            this.loadTask(this.task!.id);
          },
          error: (error) => {
            console.error('Error cancelling task:', error);
            this.notificationService.error(error.error?.message || 'Failed to cancel task');
          }
        });
      }
    }
  }

  addComment(): void {
    if (this.task && this.commentForm.valid) {
      const request = {
        content: this.commentForm.value.content,
        type: 'GENERAL'
      };
      this.taskService.addComment(this.task.id, request).subscribe({
        next: () => {
          this.notificationService.success('Comment added successfully');
          this.commentForm.reset();
          this.loadComments();
          if (this.task) {
            this.loadTask(this.task.id);
          }
        },
        error: (error) => {
          console.error('Error adding comment:', error);
          this.notificationService.error('Failed to add comment');
        }
      });
    }
  }

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFile = file;
      this.uploadAttachment();
    }
  }

  uploadAttachment(): void {
    if (this.task && this.selectedFile) {
      this.taskService.addAttachment(this.task.id, this.selectedFile).subscribe({
        next: () => {
          this.notificationService.success('Attachment uploaded successfully');
          this.selectedFile = null;
          this.loadAttachments();
          if (this.task) {
            this.loadTask(this.task.id);
          }
        },
        error: (error) => {
          console.error('Error uploading attachment:', error);
          this.notificationService.error('Failed to upload attachment');
          this.selectedFile = null;
        }
      });
    }
  }

  deleteAttachment(attachment: TaskAttachment): void {
    if (confirm(`Are you sure you want to delete ${attachment.fileName}?`)) {
      this.taskService.deleteAttachment(attachment.id).subscribe({
        next: () => {
          this.notificationService.success('Attachment deleted successfully');
          this.loadAttachments();
          if (this.task) {
            this.loadTask(this.task.id);
          }
        },
        error: (error) => {
          console.error('Error deleting attachment:', error);
          this.notificationService.error('Failed to delete attachment');
        }
      });
    }
  }

  getStatusClass(status: TaskStatus): string {
    return `status-${status}`;
  }

  getStatusIcon(status: TaskStatus): string {
    const icons: { [key: string]: string } = {
      'CREATED': 'fiber_new',
      'ASSIGNED': 'person',
      'IN_PROGRESS': 'pending',
      'COMPLETED': 'check_circle',
      'CANCELLED': 'cancel'
    };
    return icons[status] || 'help';
  }

  getPriorityClass(priority: TaskPriority): string {
    return `priority-${priority}`;
  }

  getPriorityIcon(priority: TaskPriority): string {
    const icons: { [key: string]: string } = {
      'LOW': 'arrow_downward',
      'NORMAL': 'remove',
      'HIGH': 'arrow_upward',
      'CRITICAL': 'priority_high'
    };
    return icons[priority] || 'help';
  }

  getCommentTypeClass(type: string): string {
    return `comment-type-${type}`;
  }

  getFileIcon(mimeType: string): string {
    if (mimeType.startsWith('image/')) return 'image';
    if (mimeType.startsWith('video/')) return 'videocam';
    if (mimeType.startsWith('audio/')) return 'audiotrack';
    if (mimeType.includes('pdf')) return 'picture_as_pdf';
    if (mimeType.includes('word')) return 'description';
    if (mimeType.includes('excel') || mimeType.includes('spreadsheet')) return 'table_chart';
    if (mimeType.includes('powerpoint') || mimeType.includes('presentation')) return 'slideshow';
    if (mimeType.includes('zip') || mimeType.includes('compressed')) return 'folder_zip';
    return 'insert_drive_file';
  }
}
