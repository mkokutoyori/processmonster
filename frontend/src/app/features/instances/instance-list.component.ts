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
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ProcessInstanceService } from '../../core/services/process-instance.service';
import { ProcessInstance } from '../../core/models/process.model';

/**
 * Component for listing process instances
 */
@Component({
  selector: 'app-instance-list',
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
    ReactiveFormsModule
  ],
  template: `
    <div class="instance-list-container">
      <div class="header">
        <h1>Process Instances</h1>
        <button mat-raised-button color="primary" (click)="startProcess()">
          <mat-icon>play_arrow</mat-icon>
          Start Process
        </button>
      </div>

      <div class="filters">
        <mat-form-field appearance="outline">
          <mat-label>Filter by Status</mat-label>
          <mat-select [formControl]="statusControl">
            <mat-option [value]="null">All</mat-option>
            <mat-option value="RUNNING">Running</mat-option>
            <mat-option value="SUSPENDED">Suspended</mat-option>
            <mat-option value="COMPLETED">Completed</mat-option>
            <mat-option value="FAILED">Failed</mat-option>
            <mat-option value="TERMINATED">Terminated</mat-option>
          </mat-select>
        </mat-form-field>

        <button mat-stroked-button (click)="loadActiveInstances()">
          <mat-icon>filter_list</mat-icon>
          Active Only
        </button>

        <button mat-stroked-button (click)="refresh()">
          <mat-icon>refresh</mat-icon>
          Refresh
        </button>
      </div>

      <div class="table-container">
        <table mat-table [dataSource]="instances" class="instance-table">
          <!-- ID Column -->
          <ng-container matColumnDef="id">
            <th mat-header-cell *matHeaderCellDef>ID</th>
            <td mat-cell *matCellDef="let instance">{{ instance.id }}</td>
          </ng-container>

          <!-- Process Name Column -->
          <ng-container matColumnDef="processName">
            <th mat-header-cell *matHeaderCellDef>Process</th>
            <td mat-cell *matCellDef="let instance">
              <div class="process-info">
                <strong>{{ instance.processDefinitionName }}</strong>
                <small>{{ instance.processKey }} v{{ instance.processVersion }}</small>
              </div>
            </td>
          </ng-container>

          <!-- Business Key Column -->
          <ng-container matColumnDef="businessKey">
            <th mat-header-cell *matHeaderCellDef>Business Key</th>
            <td mat-cell *matCellDef="let instance">
              <code *ngIf="instance.businessKey">{{ instance.businessKey }}</code>
              <span *ngIf="!instance.businessKey" class="no-value">-</span>
            </td>
          </ng-container>

          <!-- Status Column -->
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let instance">
              <mat-chip [ngClass]="getStatusClass(instance.status)">
                <mat-icon>{{ getStatusIcon(instance.status) }}</mat-icon>
                {{ instance.status }}
              </mat-chip>
            </td>
          </ng-container>

          <!-- Current Activity Column -->
          <ng-container matColumnDef="currentActivity">
            <th mat-header-cell *matHeaderCellDef>Current Activity</th>
            <td mat-cell *matCellDef="let instance">
              <span *ngIf="instance.currentActivityName">{{ instance.currentActivityName }}</span>
              <span *ngIf="!instance.currentActivityName" class="no-value">-</span>
            </td>
          </ng-container>

          <!-- Started By Column -->
          <ng-container matColumnDef="startedBy">
            <th mat-header-cell *matHeaderCellDef>Started By</th>
            <td mat-cell *matCellDef="let instance">{{ instance.startedBy }}</td>
          </ng-container>

          <!-- Start Time Column -->
          <ng-container matColumnDef="startTime">
            <th mat-header-cell *matHeaderCellDef>Start Time</th>
            <td mat-cell *matCellDef="let instance">
              {{ instance.startTime | date:'short' }}
            </td>
          </ng-container>

          <!-- Duration Column -->
          <ng-container matColumnDef="duration">
            <th mat-header-cell *matHeaderCellDef>Duration</th>
            <td mat-cell *matCellDef="let instance">
              <span *ngIf="instance.durationMillis">{{ formatDuration(instance.durationMillis) }}</span>
              <span *ngIf="!instance.durationMillis && instance.status === 'RUNNING'" class="running">
                {{ getRunningDuration(instance.startTime) }}
              </span>
              <span *ngIf="!instance.durationMillis && instance.status !== 'RUNNING'" class="no-value">-</span>
            </td>
          </ng-container>

          <!-- Actions Column -->
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Actions</th>
            <td mat-cell *matCellDef="let instance">
              <button mat-icon-button (click)="viewInstance(instance)" matTooltip="View Details">
                <mat-icon>visibility</mat-icon>
              </button>

              <button mat-icon-button
                      *ngIf="instance.status === 'RUNNING'"
                      (click)="suspendInstance(instance)"
                      matTooltip="Suspend">
                <mat-icon>pause</mat-icon>
              </button>

              <button mat-icon-button
                      *ngIf="instance.status === 'SUSPENDED'"
                      (click)="resumeInstance(instance)"
                      matTooltip="Resume">
                <mat-icon>play_arrow</mat-icon>
              </button>

              <button mat-icon-button
                      *ngIf="instance.status === 'RUNNING' || instance.status === 'SUSPENDED'"
                      (click)="terminateInstance(instance)"
                      matTooltip="Terminate"
                      color="warn">
                <mat-icon>stop</mat-icon>
              </button>

              <button mat-icon-button
                      (click)="viewHistory(instance)"
                      matTooltip="View History">
                <mat-icon>history</mat-icon>
              </button>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
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
    .instance-list-container {
      padding: 24px;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
    }

    .filters {
      display: flex;
      gap: 16px;
      margin-bottom: 24px;
      align-items: center;
    }

    .filters mat-form-field {
      min-width: 200px;
    }

    .table-container {
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .instance-table {
      width: 100%;
    }

    .process-info {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .process-info small {
      color: #666;
      font-size: 12px;
    }

    code {
      background: #f5f5f5;
      padding: 2px 6px;
      border-radius: 4px;
      font-family: 'Courier New', monospace;
      font-size: 12px;
    }

    .no-value {
      color: #999;
      font-style: italic;
    }

    .running {
      color: #2196f3;
      font-weight: 500;
    }

    /* Status chip colors */
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

    .status-RUNNING {
      background-color: #2196f3 !important;
      color: white !important;
    }

    .status-SUSPENDED {
      background-color: #ff9800 !important;
      color: white !important;
    }

    .status-COMPLETED {
      background-color: #4caf50 !important;
      color: white !important;
    }

    .status-FAILED {
      background-color: #f44336 !important;
      color: white !important;
    }

    .status-TERMINATED {
      background-color: #9e9e9e !important;
      color: white !important;
    }
  `]
})
export class InstanceListComponent implements OnInit {
  instances: ProcessInstance[] = [];
  displayedColumns: string[] = ['id', 'processName', 'businessKey', 'status', 'currentActivity', 'startedBy', 'startTime', 'duration', 'actions'];

  totalElements = 0;
  pageSize = 20;
  pageNumber = 0;

  statusControl = new FormControl<string | null>(null);

  constructor(
    private instanceService: ProcessInstanceService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.loadInstances();
    this.setupStatusFilter();
  }

  loadInstances(): void {
    this.instanceService.getInstances(this.pageNumber, this.pageSize).subscribe({
      next: (response) => {
        this.instances = response.content;
        this.totalElements = response.totalElements;
      },
      error: (error) => {
        console.error('Error loading instances:', error);
      }
    });
  }

  loadActiveInstances(): void {
    this.instanceService.getActiveInstances(this.pageNumber, this.pageSize).subscribe({
      next: (response) => {
        this.instances = response.content;
        this.totalElements = response.totalElements;
      },
      error: (error) => {
        console.error('Error loading active instances:', error);
      }
    });
  }

  setupStatusFilter(): void {
    this.statusControl.valueChanges.subscribe((status) => {
      if (status) {
        this.filterByStatus(status);
      } else {
        this.loadInstances();
      }
    });
  }

  filterByStatus(status: string): void {
    this.instanceService.getInstancesByStatus(status, this.pageNumber, this.pageSize).subscribe({
      next: (response) => {
        this.instances = response.content;
        this.totalElements = response.totalElements;
      },
      error: (error) => {
        console.error('Error filtering by status:', error);
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageNumber = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadInstances();
  }

  refresh(): void {
    this.loadInstances();
  }

  startProcess(): void {
    this.router.navigate(['/processes']);
  }

  viewInstance(instance: ProcessInstance): void {
    this.router.navigate(['/instances', instance.id]);
  }

  suspendInstance(instance: ProcessInstance): void {
    const reason = prompt('Reason for suspension (optional):');
    this.instanceService.suspendInstance(instance.id, reason || undefined).subscribe({
      next: () => {
        this.loadInstances();
      },
      error: (error) => {
        console.error('Error suspending instance:', error);
        alert('Failed to suspend instance: ' + error.error?.message);
      }
    });
  }

  resumeInstance(instance: ProcessInstance): void {
    this.instanceService.resumeInstance(instance.id).subscribe({
      next: () => {
        this.loadInstances();
      },
      error: (error) => {
        console.error('Error resuming instance:', error);
        alert('Failed to resume instance: ' + error.error?.message);
      }
    });
  }

  terminateInstance(instance: ProcessInstance): void {
    const reason = prompt('Reason for termination (optional):');
    if (confirm(`Are you sure you want to terminate instance #${instance.id}?`)) {
      this.instanceService.terminateInstance(instance.id, reason || undefined).subscribe({
        next: () => {
          this.loadInstances();
        },
        error: (error) => {
          console.error('Error terminating instance:', error);
          alert('Failed to terminate instance: ' + error.error?.message);
        }
      });
    }
  }

  viewHistory(instance: ProcessInstance): void {
    this.router.navigate(['/instances', instance.id, 'history']);
  }

  getStatusClass(status: string): string {
    return `status-${status}`;
  }

  getStatusIcon(status: string): string {
    const icons: { [key: string]: string } = {
      'RUNNING': 'play_circle',
      'SUSPENDED': 'pause_circle',
      'COMPLETED': 'check_circle',
      'FAILED': 'error',
      'TERMINATED': 'cancel'
    };
    return icons[status] || 'help';
  }

  formatDuration(millis: number): string {
    const seconds = Math.floor(millis / 1000);
    const minutes = Math.floor(seconds / 60);
    const hours = Math.floor(minutes / 60);
    const days = Math.floor(hours / 24);

    if (days > 0) return `${days}d ${hours % 24}h`;
    if (hours > 0) return `${hours}h ${minutes % 60}m`;
    if (minutes > 0) return `${minutes}m ${seconds % 60}s`;
    return `${seconds}s`;
  }

  getRunningDuration(startTime: string): string {
    const start = new Date(startTime);
    const now = new Date();
    const duration = now.getTime() - start.getTime();
    return this.formatDuration(duration);
  }
}
