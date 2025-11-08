import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatSelectModule } from '@angular/material/select';
import { MatDatepickerModule } from '@angular/material/datepickerModule';
import { MatNativeDateModule } from '@angular/material/core';
import { MatInputModule } from '@angular/material/input';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';
import { ReportService } from '../../core/services/report.service';

/**
 * Reports & Analytics Page
 */
@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTabsModule,
    MatFormFieldModule,
    MatSelectModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatInputModule,
    MatTableModule,
    MatChipsModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    BaseChartDirective
  ],
  template: `
    <div class="reports-container">
      <div class="header">
        <h1>Reports & Analytics</h1>
        <div class="header-actions">
          <button mat-raised-button color="primary" (click)="exportReport()">
            <mat-icon>download</mat-icon>
            Export Report
          </button>
          <button mat-raised-button (click)="refreshReports()">
            <mat-icon>refresh</mat-icon>
            Refresh
          </button>
        </div>
      </div>

      <!-- Filters -->
      <mat-card class="filters-card">
        <mat-card-content>
          <div class="filters">
            <mat-form-field appearance="outline" class="filter-field">
              <mat-label>Report Type</mat-label>
              <mat-select [(ngModel)]="selectedReportType" (selectionChange)="onFilterChange()">
                <mat-option value="all">All Reports</mat-option>
                <mat-option value="process">Process Reports</mat-option>
                <mat-option value="instance">Instance Reports</mat-option>
                <mat-option value="task">Task Reports</mat-option>
                <mat-option value="user">User Reports</mat-option>
                <mat-option value="performance">Performance Reports</mat-option>
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline" class="filter-field">
              <mat-label>Time Period</mat-label>
              <mat-select [(ngModel)]="selectedPeriod" (selectionChange)="onFilterChange()">
                <mat-option value="today">Today</mat-option>
                <mat-option value="week">This Week</mat-option>
                <mat-option value="month">This Month</mat-option>
                <mat-option value="quarter">This Quarter</mat-option>
                <mat-option value="year">This Year</mat-option>
                <mat-option value="custom">Custom Range</mat-option>
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline" class="filter-field" *ngIf="selectedPeriod === 'custom'">
              <mat-label>Start Date</mat-label>
              <input matInput [matDatepicker]="startPicker" [(ngModel)]="startDate">
              <mat-datepicker-toggle matSuffix [for]="startPicker"></mat-datepicker-toggle>
              <mat-datepicker #startPicker></mat-datepicker>
            </mat-form-field>

            <mat-form-field appearance="outline" class="filter-field" *ngIf="selectedPeriod === 'custom'">
              <mat-label>End Date</mat-label>
              <input matInput [matDatepicker]="endPicker" [(ngModel)]="endDate">
              <mat-datepicker-toggle matSuffix [for]="endPicker"></mat-datepicker-toggle>
              <mat-datepicker #endPicker></mat-datepicker>
            </mat-form-field>

            <button mat-raised-button color="accent" (click)="generateReport()">
              <mat-icon>analytics</mat-icon>
              Generate Report
            </button>
          </div>
        </mat-card-content>
      </mat-card>

      <!-- Loading State -->
      <div class="loading-container" *ngIf="isLoading">
        <mat-spinner diameter="50"></mat-spinner>
        <p>Loading reports...</p>
      </div>

      <!-- Report Tabs -->
      <mat-tab-group *ngIf="!isLoading">
        <!-- Summary Tab -->
        <mat-tab label="Summary">
          <div class="tab-content">
            <div class="metrics-grid">
              <mat-card class="metric-card">
                <mat-card-content>
                  <div class="metric-header">
                    <mat-icon>account_tree</mat-icon>
                    <h3>Total Processes</h3>
                  </div>
                  <div class="metric-value">{{ summaryData.totalProcesses }}</div>
                  <div class="metric-change positive">
                    <mat-icon>trending_up</mat-icon>
                    <span>+12% from last period</span>
                  </div>
                </mat-card-content>
              </mat-card>

              <mat-card class="metric-card">
                <mat-card-content>
                  <div class="metric-header">
                    <mat-icon>play_circle</mat-icon>
                    <h3>Instances Executed</h3>
                  </div>
                  <div class="metric-value">{{ summaryData.instancesExecuted }}</div>
                  <div class="metric-change positive">
                    <mat-icon>trending_up</mat-icon>
                    <span>+8% from last period</span>
                  </div>
                </mat-card-content>
              </mat-card>

              <mat-card class="metric-card">
                <mat-card-content>
                  <div class="metric-header">
                    <mat-icon>check_circle</mat-icon>
                    <h3>Completion Rate</h3>
                  </div>
                  <div class="metric-value">{{ summaryData.completionRate }}%</div>
                  <div class="metric-change positive">
                    <mat-icon>trending_up</mat-icon>
                    <span>+2% from last period</span>
                  </div>
                </mat-card-content>
              </mat-card>

              <mat-card class="metric-card">
                <mat-card-content>
                  <div class="metric-header">
                    <mat-icon>timer</mat-icon>
                    <h3>Avg Duration</h3>
                  </div>
                  <div class="metric-value">{{ summaryData.avgDuration }}</div>
                  <div class="metric-change negative">
                    <mat-icon>trending_down</mat-icon>
                    <span>-5% from last period</span>
                  </div>
                </mat-card-content>
              </mat-card>
            </div>

            <mat-card class="chart-card">
              <mat-card-header>
                <mat-card-title>Instance Trend</mat-card-title>
              </mat-card-header>
              <mat-card-content>
                <canvas baseChart
                        [data]="instanceTrendChartData"
                        [options]="lineChartOptions"
                        [type]="'line'">
                </canvas>
              </mat-card-content>
            </mat-card>
          </div>
        </mat-tab>

        <!-- Process Performance Tab -->
        <mat-tab label="Process Performance">
          <div class="tab-content">
            <mat-card>
              <mat-card-header>
                <mat-card-title>Process Performance Metrics</mat-card-title>
              </mat-card-header>
              <mat-card-content>
                <table mat-table [dataSource]="processPerformance" class="performance-table">
                  <ng-container matColumnDef="processName">
                    <th mat-header-cell *matHeaderCellDef>Process Name</th>
                    <td mat-cell *matCellDef="let row">{{ row.processName }}</td>
                  </ng-container>

                  <ng-container matColumnDef="totalExecutions">
                    <th mat-header-cell *matHeaderCellDef>Total Executions</th>
                    <td mat-cell *matCellDef="let row">{{ row.totalExecutions }}</td>
                  </ng-container>

                  <ng-container matColumnDef="avgDuration">
                    <th mat-header-cell *matHeaderCellDef>Avg Duration</th>
                    <td mat-cell *matCellDef="let row">{{ row.avgDuration }}</td>
                  </ng-container>

                  <ng-container matColumnDef="successRate">
                    <th mat-header-cell *matHeaderCellDef>Success Rate</th>
                    <td mat-cell *matCellDef="let row">
                      <span [class.high-success]="row.successRate >= 90"
                            [class.medium-success]="row.successRate >= 70 && row.successRate < 90"
                            [class.low-success]="row.successRate < 70">
                        {{ row.successRate }}%
                      </span>
                    </td>
                  </ng-container>

                  <ng-container matColumnDef="status">
                    <th mat-header-cell *matHeaderCellDef>Status</th>
                    <td mat-cell *matCellDef="let row">
                      <mat-chip [color]="row.status === 'HEALTHY' ? 'primary' : 'warn'">
                        {{ row.status }}
                      </mat-chip>
                    </td>
                  </ng-container>

                  <tr mat-header-row *matHeaderRowDef="performanceColumns"></tr>
                  <tr mat-row *matRowDef="let row; columns: performanceColumns;"></tr>
                </table>
              </mat-card-content>
            </mat-card>
          </div>
        </mat-tab>

        <!-- User Activity Tab -->
        <mat-tab label="User Activity">
          <div class="tab-content">
            <mat-card>
              <mat-card-header>
                <mat-card-title>User Activity Statistics</mat-card-title>
              </mat-card-header>
              <mat-card-content>
                <table mat-table [dataSource]="userActivity" class="activity-table">
                  <ng-container matColumnDef="username">
                    <th mat-header-cell *matHeaderCellDef>User</th>
                    <td mat-cell *matCellDef="let row">{{ row.username }}</td>
                  </ng-container>

                  <ng-container matColumnDef="tasksCompleted">
                    <th mat-header-cell *matHeaderCellDef>Tasks Completed</th>
                    <td mat-cell *matCellDef="let row">{{ row.tasksCompleted }}</td>
                  </ng-container>

                  <ng-container matColumnDef="instancesStarted">
                    <th mat-header-cell *matHeaderCellDef>Instances Started</th>
                    <td mat-cell *matCellDef="let row">{{ row.instancesStarted }}</td>
                  </ng-container>

                  <ng-container matColumnDef="avgResponseTime">
                    <th mat-header-cell *matHeaderCellDef>Avg Response Time</th>
                    <td mat-cell *matCellDef="let row">{{ row.avgResponseTime }}</td>
                  </ng-container>

                  <ng-container matColumnDef="productivity">
                    <th mat-header-cell *matHeaderCellDef>Productivity</th>
                    <td mat-cell *matCellDef="let row">
                      <div class="productivity-bar">
                        <div class="bar-fill" [style.width.%]="row.productivity"></div>
                        <span>{{ row.productivity }}%</span>
                      </div>
                    </td>
                  </ng-container>

                  <tr mat-header-row *matHeaderRowDef="activityColumns"></tr>
                  <tr mat-row *matRowDef="let row; columns: activityColumns;"></tr>
                </table>
              </mat-card-content>
            </mat-card>
          </div>
        </mat-tab>

        <!-- SLA Compliance Tab -->
        <mat-tab label="SLA Compliance">
          <div class="tab-content">
            <mat-card>
              <mat-card-header>
                <mat-card-title>Service Level Agreement Compliance</mat-card-title>
              </mat-card-header>
              <mat-card-content>
                <div class="sla-metrics">
                  <div class="sla-item">
                    <h4>Overall SLA Compliance</h4>
                    <div class="sla-gauge">
                      <div class="gauge-value">94.5%</div>
                      <div class="gauge-label">On-Time Completion</div>
                    </div>
                  </div>

                  <div class="sla-item">
                    <h4>Breaches by Severity</h4>
                    <ul class="breach-list">
                      <li>
                        <mat-chip color="warn">Critical</mat-chip>
                        <span>3 breaches</span>
                      </li>
                      <li>
                        <mat-chip color="accent">High</mat-chip>
                        <span>12 breaches</span>
                      </li>
                      <li>
                        <mat-chip>Medium</mat-chip>
                        <span>28 breaches</span>
                      </li>
                    </ul>
                  </div>
                </div>
              </mat-card-content>
            </mat-card>
          </div>
        </mat-tab>
      </mat-tab-group>
    </div>
  `,
  styles: [`
    .reports-container {
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

    .header-actions button {
      display: flex;
      align-items: center;
      gap: 8px;
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

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 48px;
      gap: 16px;
    }

    .tab-content {
      padding: 24px 0;
    }

    .metrics-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
      gap: 16px;
      margin-bottom: 24px;
    }

    .metric-card {
      padding: 16px;
    }

    .metric-header {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 16px;
    }

    .metric-header mat-icon {
      color: #1976d2;
      font-size: 32px;
      width: 32px;
      height: 32px;
    }

    .metric-header h3 {
      margin: 0;
      color: #666;
      font-size: 14px;
      font-weight: 500;
    }

    .metric-value {
      font-size: 32px;
      font-weight: 500;
      color: #333;
      margin-bottom: 8px;
    }

    .metric-change {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 14px;
    }

    .metric-change.positive {
      color: #4caf50;
    }

    .metric-change.negative {
      color: #f44336;
    }

    .metric-change mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
    }

    .chart-card {
      margin-top: 24px;
    }

    .chart-card mat-card-content {
      padding: 16px;
      min-height: 300px;
    }

    .chart-card canvas {
      max-height: 300px !important;
    }

    .performance-table,
    .activity-table {
      width: 100%;
    }

    .high-success {
      color: #4caf50;
      font-weight: 500;
    }

    .medium-success {
      color: #ff9800;
      font-weight: 500;
    }

    .low-success {
      color: #f44336;
      font-weight: 500;
    }

    .productivity-bar {
      position: relative;
      width: 100%;
      height: 24px;
      background: #e0e0e0;
      border-radius: 12px;
      overflow: hidden;
    }

    .bar-fill {
      height: 100%;
      background: linear-gradient(90deg, #1976d2, #42a5f5);
      transition: width 0.3s ease;
    }

    .productivity-bar span {
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      color: #fff;
      font-size: 12px;
      font-weight: 500;
      text-shadow: 0 1px 2px rgba(0,0,0,0.3);
    }

    .sla-metrics {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
      gap: 24px;
    }

    .sla-item h4 {
      margin: 0 0 16px 0;
      color: #333;
    }

    .sla-gauge {
      text-align: center;
      padding: 32px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 12px;
      color: #fff;
    }

    .gauge-value {
      font-size: 48px;
      font-weight: 500;
      margin-bottom: 8px;
    }

    .gauge-label {
      font-size: 14px;
      opacity: 0.9;
    }

    .breach-list {
      list-style: none;
      padding: 0;
      margin: 0;
    }

    .breach-list li {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px;
      background: #f5f5f5;
      border-radius: 4px;
      margin-bottom: 8px;
    }

    .breach-list span {
      font-weight: 500;
      color: #333;
    }
  `]
})
export class ReportsComponent implements OnInit {
  selectedReportType = 'all';
  selectedPeriod = 'month';
  startDate: Date | null = null;
  endDate: Date | null = null;
  isLoading = false;

  summaryData = {
    totalProcesses: 42,
    instancesExecuted: 1248,
    completionRate: 94.5,
    avgDuration: '2.5h'
  };

  processPerformance: any[] = [];
  performanceColumns: string[] = ['processName', 'totalExecutions', 'avgDuration', 'successRate', 'status'];

  userActivity: any[] = [];
  activityColumns: string[] = ['username', 'tasksCompleted', 'instancesStarted', 'avgResponseTime', 'productivity'];

  // Chart Data
  instanceTrendChartData: ChartData<'line'> = {
    labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul'],
    datasets: [
      {
        label: 'Completed',
        data: [65, 59, 80, 81, 56, 55, 40],
        borderColor: '#4caf50',
        backgroundColor: 'rgba(76, 175, 80, 0.1)',
        fill: true,
        tension: 0.4
      },
      {
        label: 'Failed',
        data: [28, 48, 40, 19, 86, 27, 90],
        borderColor: '#f44336',
        backgroundColor: 'rgba(244, 67, 54, 0.1)',
        fill: true,
        tension: 0.4
      }
    ]
  };

  lineChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'top'
      },
      tooltip: {
        mode: 'index',
        intersect: false
      }
    },
    scales: {
      y: {
        beginAtZero: true
      }
    }
  };

  constructor(
    private reportService: ReportService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadReports();
  }

  loadReports(): void {
    this.isLoading = true;

    // Load process performance
    this.reportService.getProcessPerformance(this.selectedPeriod).subscribe({
      next: (data) => {
        this.processPerformance = data;
      },
      error: () => {
        this.loadMockProcessPerformance();
      }
    });

    // Load user activity
    this.reportService.getUserActivity(this.selectedPeriod).subscribe({
      next: (data) => {
        this.userActivity = data;
        this.isLoading = false;
      },
      error: () => {
        this.loadMockUserActivity();
        this.isLoading = false;
      }
    });
  }

  loadMockProcessPerformance(): void {
    this.processPerformance = [
      { processName: 'Loan Application', totalExecutions: 342, avgDuration: '2.3h', successRate: 96.2, status: 'HEALTHY' },
      { processName: 'Account Opening', totalExecutions: 567, avgDuration: '1.5h', successRate: 98.1, status: 'HEALTHY' },
      { processName: 'KYC Verification', totalExecutions: 234, avgDuration: '45m', successRate: 87.5, status: 'WARNING' },
      { processName: 'Payment Processing', totalExecutions: 892, avgDuration: '15m', successRate: 99.3, status: 'HEALTHY' }
    ];
  }

  loadMockUserActivity(): void {
    this.userActivity = [
      { username: 'admin', tasksCompleted: 156, instancesStarted: 42, avgResponseTime: '12m', productivity: 92 },
      { username: 'manager', tasksCompleted: 234, instancesStarted: 67, avgResponseTime: '8m', productivity: 95 },
      { username: 'analyst', tasksCompleted: 189, instancesStarted: 23, avgResponseTime: '15m', productivity: 88 }
    ];
  }

  onFilterChange(): void {
    this.loadReports();
  }

  generateReport(): void {
    this.snackBar.open('Generating report...', 'Close', { duration: 2000 });
    this.loadReports();
  }

  refreshReports(): void {
    this.loadReports();
    this.snackBar.open('Reports refreshed', 'Close', { duration: 2000 });
  }

  exportReport(): void {
    this.snackBar.open('Export functionality - To be implemented', 'Close', { duration: 3000 });
  }
}
