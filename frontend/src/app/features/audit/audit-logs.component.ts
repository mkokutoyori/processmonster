import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatTabsModule } from '@angular/material/tabs';
import { MatCardModule } from '@angular/material/card';
import { AuditService } from '../../core/services/audit.service';
import { AuditLog, AuditSeverity } from '../../core/models/audit.model';

/**
 * Audit Logs Page
 */
@Component({
  selector: 'app-audit-logs',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatSnackBarModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatTabsModule,
    MatCardModule
  ],
  template: `
    <div class="audit-logs-container">
      <div class="header">
        <h1>Audit Logs</h1>
        <button mat-raised-button color="primary" (click)="exportLogs()">
          <mat-icon>download</mat-icon>
          Export Logs
        </button>
      </div>

      <mat-card class="filters-card">
        <mat-card-content>
          <div class="filters">
            <mat-form-field appearance="outline" class="filter-field">
              <mat-label>Search</mat-label>
              <input matInput [(ngModel)]="searchKeyword" (keyup.enter)="onSearch()"
                     placeholder="Search by action, user, entity...">
              <mat-icon matSuffix>search</mat-icon>
            </mat-form-field>

            <mat-form-field appearance="outline" class="filter-field">
              <mat-label>Severity</mat-label>
              <mat-select [(ngModel)]="selectedSeverity" (selectionChange)="onFilterChange()">
                <mat-option value="">All</mat-option>
                <mat-option value="INFO">Info</mat-option>
                <mat-option value="WARNING">Warning</mat-option>
                <mat-option value="ERROR">Error</mat-option>
                <mat-option value="CRITICAL">Critical</mat-option>
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline" class="filter-field">
              <mat-label>Start Date</mat-label>
              <input matInput [matDatepicker]="startPicker" [(ngModel)]="startDate"
                     (dateChange)="onFilterChange()">
              <mat-datepicker-toggle matSuffix [for]="startPicker"></mat-datepicker-toggle>
              <mat-datepicker #startPicker></mat-datepicker>
            </mat-form-field>

            <mat-form-field appearance="outline" class="filter-field">
              <mat-label>End Date</mat-label>
              <input matInput [matDatepicker]="endPicker" [(ngModel)]="endDate"
                     (dateChange)="onFilterChange()">
              <mat-datepicker-toggle matSuffix [for]="endPicker"></mat-datepicker-toggle>
              <mat-datepicker #endPicker></mat-datepicker>
            </mat-form-field>

            <button mat-raised-button (click)="clearFilters()">Clear Filters</button>
          </div>
        </mat-card-content>
      </mat-card>

      <mat-tab-group (selectedTabChange)="onTabChange($event)">
        <mat-tab label="All Logs">
          <div class="table-container">
            <table mat-table [dataSource]="auditLogs" class="audit-logs-table">
              <!-- Timestamp Column -->
              <ng-container matColumnDef="timestamp">
                <th mat-header-cell *matHeaderCellDef>Timestamp</th>
                <td mat-cell *matCellDef="let log">{{ log.timestamp | date:'short' }}</td>
              </ng-container>

              <!-- Severity Column -->
              <ng-container matColumnDef="severity">
                <th mat-header-cell *matHeaderCellDef>Severity</th>
                <td mat-cell *matCellDef="let log">
                  <mat-chip [color]="getSeverityColor(log.severity)">
                    {{ log.severity }}
                  </mat-chip>
                </td>
              </ng-container>

              <!-- Username Column -->
              <ng-container matColumnDef="username">
                <th mat-header-cell *matHeaderCellDef>User</th>
                <td mat-cell *matCellDef="let log">{{ log.username }}</td>
              </ng-container>

              <!-- Action Column -->
              <ng-container matColumnDef="action">
                <th mat-header-cell *matHeaderCellDef>Action</th>
                <td mat-cell *matCellDef="let log">
                  <code>{{ log.action }}</code>
                </td>
              </ng-container>

              <!-- Entity Column -->
              <ng-container matColumnDef="entity">
                <th mat-header-cell *matHeaderCellDef>Entity</th>
                <td mat-cell *matCellDef="let log">
                  <span *ngIf="log.entityType">
                    {{ log.entityType }}
                    <span *ngIf="log.entityId">#{{ log.entityId }}</span>
                  </span>
                  <span *ngIf="!log.entityType">-</span>
                </td>
              </ng-container>

              <!-- Result Column -->
              <ng-container matColumnDef="result">
                <th mat-header-cell *matHeaderCellDef>Result</th>
                <td mat-cell *matCellDef="let log">
                  <mat-chip [color]="log.result === 'SUCCESS' ? 'primary' : 'warn'">
                    {{ log.result }}
                  </mat-chip>
                </td>
              </ng-container>

              <!-- IP Address Column -->
              <ng-container matColumnDef="ipAddress">
                <th mat-header-cell *matHeaderCellDef>IP Address</th>
                <td mat-cell *matCellDef="let log">
                  <code>{{ log.ipAddress || '-' }}</code>
                </td>
              </ng-container>

              <!-- Actions Column -->
              <ng-container matColumnDef="actions">
                <th mat-header-cell *matHeaderCellDef>Actions</th>
                <td mat-cell *matCellDef="let log">
                  <button mat-icon-button (click)="viewDetails(log)">
                    <mat-icon>visibility</mat-icon>
                  </button>
                </td>
              </ng-container>

              <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
              <tr mat-row *matRowDef="let row; columns: displayedColumns;"
                  [class.error-row]="row.severity === 'ERROR' || row.severity === 'CRITICAL'">
              </tr>
            </table>

            <mat-paginator
              [length]="totalElements"
              [pageSize]="pageSize"
              [pageSizeOptions]="[20, 50, 100]"
              (page)="onPageChange($event)"
              showFirstLastButtons>
            </mat-paginator>
          </div>
        </mat-tab>

        <mat-tab label="Security Logs">
          <div class="table-container">
            <p class="tab-info">Security-related audit logs (authentication, authorization, etc.)</p>
          </div>
        </mat-tab>

        <mat-tab label="Failed Actions">
          <div class="table-container">
            <p class="tab-info">Failed or error actions</p>
          </div>
        </mat-tab>
      </mat-tab-group>
    </div>
  `,
  styles: [`
    .audit-logs-container {
      padding: 24px;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
    }

    .filters-card {
      margin-bottom: 24px;
    }

    .filters {
      display: flex;
      gap: 16px;
      align-items: center;
      flex-wrap: wrap;
    }

    .filter-field {
      min-width: 200px;
    }

    .table-container {
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
      margin-top: 16px;
    }

    .audit-logs-table {
      width: 100%;
    }

    code {
      background: #f5f5f5;
      padding: 4px 8px;
      border-radius: 4px;
      font-family: 'Courier New', monospace;
      font-size: 12px;
    }

    .error-row {
      background-color: #ffebee;
    }

    .tab-info {
      padding: 24px;
      color: #666;
      text-align: center;
    }
  `]
})
export class AuditLogsComponent implements OnInit {
  auditLogs: AuditLog[] = [];
  displayedColumns: string[] = ['timestamp', 'severity', 'username', 'action', 'entity', 'result', 'ipAddress', 'actions'];

  pageSize = 20;
  currentPage = 0;
  totalElements = 0;

  searchKeyword = '';
  selectedSeverity = '';
  startDate: Date | null = null;
  endDate: Date | null = null;
  currentTab = 0;

  constructor(
    private auditService: AuditService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadAuditLogs();
  }

  loadAuditLogs(): void {
    let observable;

    if (this.searchKeyword) {
      observable = this.auditService.searchLogs(this.searchKeyword, this.currentPage, this.pageSize);
    } else if (this.startDate && this.endDate) {
      const start = this.startDate.toISOString();
      const end = this.endDate.toISOString();
      observable = this.auditService.getLogsByDateRange(start, end, this.currentPage, this.pageSize);
    } else if (this.currentTab === 1) {
      observable = this.auditService.getSecurityLogs(this.currentPage, this.pageSize);
    } else if (this.currentTab === 2) {
      observable = this.auditService.getFailedActions(this.currentPage, this.pageSize);
    } else {
      observable = this.auditService.getAllLogs(this.currentPage, this.pageSize);
    }

    observable.subscribe({
      next: (response) => {
        this.auditLogs = response.content;
        this.totalElements = response.totalElements;
      },
      error: () => {
        this.snackBar.open('Failed to load audit logs', 'Close', { duration: 3000 });
      }
    });
  }

  onSearch(): void {
    this.currentPage = 0;
    this.loadAuditLogs();
  }

  onFilterChange(): void {
    this.currentPage = 0;
    this.loadAuditLogs();
  }

  clearFilters(): void {
    this.searchKeyword = '';
    this.selectedSeverity = '';
    this.startDate = null;
    this.endDate = null;
    this.currentPage = 0;
    this.loadAuditLogs();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadAuditLogs();
  }

  onTabChange(event: any): void {
    this.currentTab = event.index;
    this.currentPage = 0;
    this.loadAuditLogs();
  }

  getSeverityColor(severity: string): string {
    switch (severity) {
      case 'INFO': return 'primary';
      case 'WARNING': return 'accent';
      case 'ERROR': return 'warn';
      case 'CRITICAL': return 'warn';
      default: return '';
    }
  }

  viewDetails(log: AuditLog): void {
    this.snackBar.open('Audit log details - To be implemented', 'Close', { duration: 3000 });
  }

  exportLogs(): void {
    this.snackBar.open('Export audit logs - To be implemented', 'Close', { duration: 3000 });
  }
}
