import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatBadgeModule } from '@angular/material/badge';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { TaskService } from '../../core/services/task.service';
import { NotificationService } from '../../core/services/notification.service';
import { Task, TaskStatus, TaskPriority } from '../../core/models/process.model';

/**
 * Component for displaying user's task inbox
 */
@Component({
  selector: 'app-task-inbox',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatTooltipModule,
    MatBadgeModule,
    ReactiveFormsModule
  ],
  template: `
    <div class="task-inbox-container">
      <div class="header">
        <h1>My Tasks Inbox</h1>
        <div class="header-actions">
          <button mat-raised-button color="accent" (click)="loadMyActiveTasks()">
            <mat-icon [matBadge]="activeTaskCount" matBadgeColor="warn" [matBadgeHidden]="activeTaskCount === 0">inbox</mat-icon>
            Active Tasks
          </button>
          <button mat-raised-button color="primary" (click)="createTask()">
            <mat-icon>add</mat-icon>
            New Task
          </button>
        </div>
      </div>

      <div class="filters">
        <mat-form-field appearance="outline">
          <mat-label>Filter by Status</mat-label>
          <mat-select [formControl]="statusControl">
            <mat-option [value]="null">All</mat-option>
            <mat-option value="CREATED">Created</mat-option>
            <mat-option value="ASSIGNED">Assigned</mat-option>
            <mat-option value="IN_PROGRESS">In Progress</mat-option>
            <mat-option value="COMPLETED">Completed</mat-option>
            <mat-option value="CANCELLED">Cancelled</mat-option>
          </mat-select>
        </mat-form-field>

        <mat-form-field appearance="outline">
          <mat-label>Filter by Priority</mat-label>
          <mat-select [formControl]="priorityControl">
            <mat-option [value]="null">All</mat-option>
            <mat-option value="LOW">Low</mat-option>
            <mat-option value="NORMAL">Normal</mat-option>
            <mat-option value="HIGH">High</mat-option>
            <mat-option value="CRITICAL">Critical</mat-option>
          </mat-select>
        </mat-form-field>

        <button mat-stroked-button (click)="loadOverdueTasks()">
          <mat-icon>warning</mat-icon>
          Overdue
        </button>

        <button mat-stroked-button (click)="loadTasksDueSoon()">
          <mat-icon>schedule</mat-icon>
          Due Soon
        </button>

        <button mat-stroked-button (click)="refresh()">
          <mat-icon>refresh</mat-icon>
          Refresh
        </button>
      </div>

      <div class="table-container">
        <table mat-table [dataSource]="tasks" class="task-table">
          <!-- ID Column -->
          <ng-container matColumnDef="id">
            <th mat-header-cell *matHeaderCellDef>ID</th>
            <td mat-cell *matCellDef="let task">{{ task.id }}</td>
          </ng-container>

          <!-- Name Column -->
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Task Name</th>
            <td mat-cell *matCellDef="let task">
              <div class="task-name">
                <strong>{{ task.name }}</strong>
                <mat-icon *ngIf="task.isOverdue" class="overdue-icon" matTooltip="Overdue">warning</mat-icon>
              </div>
              <small *ngIf="task.description">{{ truncateDescription(task.description) }}</small>
            </td>
          </ng-container>

          <!-- Priority Column -->
          <ng-container matColumnDef="priority">
            <th mat-header-cell *matHeaderCellDef>Priority</th>
            <td mat-cell *matCellDef="let task">
              <mat-chip [ngClass]="getPriorityClass(task.priority)">
                <mat-icon>{{ getPriorityIcon(task.priority) }}</mat-icon>
                {{ task.priority }}
              </mat-chip>
            </td>
          </ng-container>

          <!-- Status Column -->
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let task">
              <mat-chip [ngClass]="getStatusClass(task.status)">
                <mat-icon>{{ getStatusIcon(task.status) }}</mat-icon>
                {{ task.status }}
              </mat-chip>
            </td>
          </ng-container>

          <!-- Assignee Column -->
          <ng-container matColumnDef="assignee">
            <th mat-header-cell *matHeaderCellDef>Assignee</th>
            <td mat-cell *matCellDef="let task">
              <span *ngIf="task.assignee">{{ task.assignee }}</span>
              <span *ngIf="!task.assignee" class="no-value">Unassigned</span>
            </td>
          </ng-container>

          <!-- Due Date Column -->
          <ng-container matColumnDef="dueDate">
            <th mat-header-cell *matHeaderCellDef>Due Date</th>
            <td mat-cell *matCellDef="let task">
              <span *ngIf="task.dueDate" [ngClass]="{ 'overdue-text': task.isOverdue }">
                {{ task.dueDate | date:'short' }}
              </span>
              <span *ngIf="!task.dueDate" class="no-value">-</span>
            </td>
          </ng-container>

          <!-- Process Column -->
          <ng-container matColumnDef="process">
            <th mat-header-cell *matHeaderCellDef>Process</th>
            <td mat-cell *matCellDef="let task">
              <span *ngIf="task.processInstanceId">
                <code (click)="viewInstance(task.processInstanceId)" class="clickable">
                  {{ task.processInstanceBusinessKey || task.processInstanceId }}
                </code>
              </span>
              <span *ngIf="!task.processInstanceId" class="no-value">-</span>
            </td>
          </ng-container>

          <!-- Metadata Column -->
          <ng-container matColumnDef="metadata">
            <th mat-header-cell *matHeaderCellDef>Info</th>
            <td mat-cell *matCellDef="let task">
              <div class="metadata">
                <mat-icon *ngIf="task.commentCount > 0"
                          matTooltip="{{ task.commentCount }} comment(s)"
                          [matBadge]="task.commentCount"
                          matBadgeSize="small">
                  comment
                </mat-icon>
                <mat-icon *ngIf="task.attachmentCount > 0"
                          matTooltip="{{ task.attachmentCount }} attachment(s)"
                          [matBadge]="task.attachmentCount"
                          matBadgeSize="small">
                  attach_file
                </mat-icon>
              </div>
            </td>
          </ng-container>

          <!-- Actions Column -->
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Actions</th>
            <td mat-cell *matCellDef="let task">
              <button mat-icon-button (click)="viewTask(task)" matTooltip="View Details">
                <mat-icon>visibility</mat-icon>
              </button>

              <button mat-icon-button
                      *ngIf="!task.assignee && task.status === 'CREATED'"
                      (click)="claimTask(task)"
                      matTooltip="Claim Task"
                      color="primary">
                <mat-icon>person_add</mat-icon>
              </button>

              <button mat-icon-button
                      *ngIf="task.assignee && task.status === 'ASSIGNED'"
                      (click)="startTask(task)"
                      matTooltip="Start Task"
                      color="accent">
                <mat-icon>play_arrow</mat-icon>
              </button>

              <button mat-icon-button
                      *ngIf="task.status === 'IN_PROGRESS'"
                      (click)="completeTask(task)"
                      matTooltip="Complete Task"
                      color="primary">
                <mat-icon>check_circle</mat-icon>
              </button>

              <button mat-icon-button
                      *ngIf="task.isActive"
                      (click)="cancelTask(task)"
                      matTooltip="Cancel Task"
                      color="warn">
                <mat-icon>cancel</mat-icon>
              </button>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;" [ngClass]="{ 'overdue-row': row.isOverdue }"></tr>
        </table>

        <mat-paginator
          [length]="totalElements"
          [pageSize]="pageSize"
          [pageSizeOptions]="[10, 20, 50]"
          (page)="onPageChange($event)"
          showFirstLastButtons>
        </mat-paginator>
      </div>
    </div>
  `,
  styles: [`
    .task-inbox-container {
      padding: 24px;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }

    .filters {
      display: flex;
      gap: 16px;
      margin-bottom: 24px;
      align-items: center;
    }

    .filters mat-form-field {
      min-width: 180px;
    }

    .table-container {
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .task-table {
      width: 100%;
    }

    .task-name {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .task-name small {
      color: #666;
      font-size: 12px;
      display: block;
      margin-top: 4px;
    }

    .overdue-icon {
      color: #f44336;
      font-size: 18px;
      width: 18px;
      height: 18px;
    }

    .overdue-text {
      color: #f44336;
      font-weight: 600;
    }

    .overdue-row {
      background-color: #ffebee !important;
    }

    code {
      background: #f5f5f5;
      padding: 2px 6px;
      border-radius: 4px;
      font-family: 'Courier New', monospace;
      font-size: 12px;
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

    .metadata {
      display: flex;
      gap: 8px;
      align-items: center;
    }

    .metadata mat-icon {
      color: #666;
      font-size: 20px;
      width: 20px;
      height: 20px;
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
export class TaskInboxComponent implements OnInit {
  tasks: Task[] = [];
  displayedColumns: string[] = ['id', 'name', 'priority', 'status', 'assignee', 'dueDate', 'process', 'metadata', 'actions'];

  totalElements = 0;
  pageSize = 20;
  pageNumber = 0;

  activeTaskCount = 0;

  statusControl = new FormControl<string | null>(null);
  priorityControl = new FormControl<string | null>(null);

  constructor(
    private taskService: TaskService,
    private router: Router,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadMyTasks();
    this.loadActiveTaskCount();
    this.setupFilters();
  }

  loadMyTasks(): void {
    this.taskService.getMyTasks(this.pageNumber, this.pageSize).subscribe({
      next: (response) => {
        this.tasks = response.content;
        this.totalElements = response.totalElements;
      },
      error: (error) => {
        console.error('Error loading tasks:', error);
        this.notificationService.error('Failed to load tasks');
      }
    });
  }

  loadMyActiveTasks(): void {
    this.taskService.getMyActiveTasks(this.pageNumber, this.pageSize).subscribe({
      next: (response) => {
        this.tasks = response.content;
        this.totalElements = response.totalElements;
      },
      error: (error) => {
        console.error('Error loading active tasks:', error);
        this.notificationService.error('Failed to load active tasks');
      }
    });
  }

  loadOverdueTasks(): void {
    this.taskService.getOverdueTasks(this.pageNumber, this.pageSize).subscribe({
      next: (response) => {
        this.tasks = response.content;
        this.totalElements = response.totalElements;
      },
      error: (error) => {
        console.error('Error loading overdue tasks:', error);
        this.notificationService.error('Failed to load overdue tasks');
      }
    });
  }

  loadTasksDueSoon(): void {
    this.taskService.getTasksDueSoon(this.pageNumber, this.pageSize).subscribe({
      next: (response) => {
        this.tasks = response.content;
        this.totalElements = response.totalElements;
      },
      error: (error) => {
        console.error('Error loading tasks due soon:', error);
        this.notificationService.error('Failed to load tasks due soon');
      }
    });
  }

  loadActiveTaskCount(): void {
    this.taskService.countMyActiveTasks().subscribe({
      next: (count) => {
        this.activeTaskCount = count;
      },
      error: (error) => {
        console.error('Error loading active task count:', error);
      }
    });
  }

  setupFilters(): void {
    this.statusControl.valueChanges.subscribe((status) => {
      if (status) {
        this.filterByStatus(status as TaskStatus);
      } else {
        this.loadMyTasks();
      }
    });

    this.priorityControl.valueChanges.subscribe(() => {
      this.applyFilters();
    });
  }

  filterByStatus(status: TaskStatus): void {
    this.taskService.getTasksByStatus(status, this.pageNumber, this.pageSize).subscribe({
      next: (response) => {
        this.tasks = response.content;
        this.totalElements = response.totalElements;
      },
      error: (error) => {
        console.error('Error filtering by status:', error);
        this.notificationService.error('Failed to filter tasks');
      }
    });
  }

  applyFilters(): void {
    // For now just reload, could enhance with combined filters
    this.loadMyTasks();
  }

  onPageChange(event: PageEvent): void {
    this.pageNumber = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadMyTasks();
  }

  refresh(): void {
    this.loadMyTasks();
    this.loadActiveTaskCount();
  }

  createTask(): void {
    this.router.navigate(['/tasks/new']);
  }

  viewTask(task: Task): void {
    this.router.navigate(['/tasks', task.id]);
  }

  viewInstance(instanceId: number): void {
    this.router.navigate(['/instances', instanceId]);
  }

  claimTask(task: Task): void {
    this.taskService.claimTask(task.id).subscribe({
      next: () => {
        this.notificationService.success(`Task #${task.id} claimed successfully`);
        this.loadMyTasks();
        this.loadActiveTaskCount();
      },
      error: (error) => {
        console.error('Error claiming task:', error);
        this.notificationService.error(error.error?.message || 'Failed to claim task');
      }
    });
  }

  startTask(task: Task): void {
    this.taskService.startTask(task.id).subscribe({
      next: () => {
        this.notificationService.success(`Task #${task.id} started successfully`);
        this.loadMyTasks();
      },
      error: (error) => {
        console.error('Error starting task:', error);
        this.notificationService.error(error.error?.message || 'Failed to start task');
      }
    });
  }

  completeTask(task: Task): void {
    this.taskService.completeTask(task.id).subscribe({
      next: () => {
        this.notificationService.success(`Task #${task.id} completed successfully`);
        this.loadMyTasks();
        this.loadActiveTaskCount();
      },
      error: (error) => {
        console.error('Error completing task:', error);
        this.notificationService.error(error.error?.message || 'Failed to complete task');
      }
    });
  }

  cancelTask(task: Task): void {
    const reason = prompt('Reason for cancellation (optional):');
    if (confirm(`Are you sure you want to cancel task #${task.id}?`)) {
      this.taskService.cancelTask(task.id, reason || undefined).subscribe({
        next: () => {
          this.notificationService.success(`Task #${task.id} cancelled successfully`);
          this.loadMyTasks();
          this.loadActiveTaskCount();
        },
        error: (error) => {
          console.error('Error cancelling task:', error);
          this.notificationService.error(error.error?.message || 'Failed to cancel task');
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

  truncateDescription(description: string): string {
    return description.length > 100 ? description.substring(0, 100) + '...' : description;
  }
}
