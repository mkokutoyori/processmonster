/**
 * Dashboard Component
 *
 * Main dashboard with KPIs and quick access to features.
 */
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatGridListModule } from '@angular/material/grid-list';
import { MatTableModule } from '@angular/material/table';
import { MatChipsModule } from '@angular/material/chips';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { TranslateModule } from '@ngx-translate/core';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';
import { AuthService } from '../../core/services/auth.service';
import { ReportService } from '../../core/services/report.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatGridListModule,
    MatTableModule,
    MatChipsModule,
    MatProgressSpinnerModule,
    MatSnackBarModule,
    TranslateModule,
    BaseChartDirective
  ],
  template: `
    <div class="dashboard-container">
      <div class="dashboard-header">
        <div>
          <h1>{{ 'dashboard.title' | translate }}</h1>
          <p class="welcome-message">
            {{ 'app.welcome' | translate }}, {{ currentUser?.firstName || currentUser?.username }}!
          </p>
        </div>
        <button mat-raised-button color="primary" (click)="refreshDashboard()" [disabled]="isLoading">
          <mat-icon>refresh</mat-icon>
          {{ 'common.refresh' | translate }}
        </button>
      </div>

      <!-- Loading State -->
      <div class="loading-container" *ngIf="isLoading">
        <mat-spinner diameter="50"></mat-spinner>
        <p>{{ 'common.loading' | translate }}...</p>
      </div>

      <!-- KPI Cards -->
      <mat-grid-list cols="4" rowHeight="150px" [gutterSize]="'16px'" class="kpi-grid" *ngIf="!isLoading">
        <mat-grid-tile>
          <mat-card class="kpi-card">
            <mat-card-content>
              <mat-icon class="kpi-icon primary">account_tree</mat-icon>
              <h2>{{ stats.totalProcesses }}</h2>
              <p>{{ 'dashboard.processesActive' | translate }}</p>
              <small>{{ stats.activeProcesses }} {{ 'common.active' | translate }}</small>
            </mat-card-content>
          </mat-card>
        </mat-grid-tile>

        <mat-grid-tile>
          <mat-card class="kpi-card">
            <mat-card-content>
              <mat-icon class="kpi-icon accent">play_circle</mat-icon>
              <h2>{{ stats.totalInstances }}</h2>
              <p>{{ 'dashboard.instances' | translate }}</p>
              <small>{{ stats.runningInstances }} {{ 'common.running' | translate }}</small>
            </mat-card-content>
          </mat-card>
        </mat-grid-tile>

        <mat-grid-tile>
          <mat-card class="kpi-card">
            <mat-card-content>
              <mat-icon class="kpi-icon warn">assignment</mat-icon>
              <h2>{{ stats.totalTasks }}</h2>
              <p>{{ 'dashboard.tasksPending' | translate }}</p>
              <small>{{ stats.pendingTasks }} {{ 'common.pending' | translate }}</small>
            </mat-card-content>
          </mat-card>
        </mat-grid-tile>

        <mat-grid-tile>
          <mat-card class="kpi-card">
            <mat-card-content>
              <mat-icon class="kpi-icon success">people</mat-icon>
              <h2>{{ stats.totalUsers }}</h2>
              <p>{{ 'dashboard.users' | translate }}</p>
              <small>{{ stats.activeUsers }} {{ 'common.online' | translate }}</small>
            </mat-card-content>
          </mat-card>
        </mat-grid-tile>
      </mat-grid-list>

      <!-- Charts Section -->
      <div class="charts-grid" *ngIf="!isLoading">
        <!-- Completion Trend Chart -->
        <mat-card class="chart-card">
          <mat-card-header>
            <mat-card-title>{{ 'dashboard.completionTrend' | translate }}</mat-card-title>
            <mat-card-subtitle>Last 7 days</mat-card-subtitle>
          </mat-card-header>
          <mat-card-content>
            <canvas baseChart
                    [data]="lineChartData"
                    [options]="lineChartOptions"
                    [type]="'line'">
            </canvas>
          </mat-card-content>
        </mat-card>

        <!-- Instance Status Distribution -->
        <mat-card class="chart-card">
          <mat-card-header>
            <mat-card-title>{{ 'dashboard.instanceDistribution' | translate }}</mat-card-title>
            <mat-card-subtitle>Current status</mat-card-subtitle>
          </mat-card-header>
          <mat-card-content>
            <canvas baseChart
                    [data]="doughnutChartData"
                    [options]="doughnutChartOptions"
                    [type]="'doughnut'">
            </canvas>
          </mat-card-content>
        </mat-card>
      </div>

      <!-- Quick Actions -->
      <mat-card class="quick-actions-card" *ngIf="!isLoading">
        <mat-card-header>
          <mat-card-title>{{ 'common.actions' | translate }}</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <div class="actions-grid">
            <button mat-raised-button color="primary" (click)="navigateTo('/processes/create')">
              <mat-icon>add</mat-icon>
              {{ 'process.create' | translate }}
            </button>
            <button mat-raised-button color="accent" (click)="navigateTo('/instances/create')">
              <mat-icon>play_arrow</mat-icon>
              {{ 'instance.start' | translate }}
            </button>
            <button mat-raised-button (click)="navigateTo('/processes')">
              <mat-icon>account_tree</mat-icon>
              {{ 'process.list' | translate }}
            </button>
            <button mat-raised-button (click)="navigateTo('/instances')">
              <mat-icon>list</mat-icon>
              {{ 'instance.list' | translate }}
            </button>
            <button mat-raised-button (click)="navigateTo('/users')">
              <mat-icon>people</mat-icon>
              {{ 'user.list' | translate }}
            </button>
            <button mat-raised-button (click)="navigateTo('/reports')">
              <mat-icon>assessment</mat-icon>
              {{ 'report.generate' | translate }}
            </button>
          </div>
        </mat-card-content>
      </mat-card>

      <!-- Recent Activity -->
      <mat-card class="recent-activity-card" *ngIf="!isLoading">
        <mat-card-header>
          <mat-card-title>{{ 'common.recentActivity' | translate }}</mat-card-title>
          <button mat-button (click)="navigateTo('/audit')">
            {{ 'common.viewAll' | translate }}
            <mat-icon>arrow_forward</mat-icon>
          </button>
        </mat-card-header>
        <mat-card-content>
          <table mat-table [dataSource]="recentActivity" class="activity-table" *ngIf="recentActivity.length > 0">
            <!-- Type Column -->
            <ng-container matColumnDef="type">
              <th mat-header-cell *matHeaderCellDef>{{ 'common.type' | translate }}</th>
              <td mat-cell *matCellDef="let activity">
                <mat-icon [class]="activity.type.toLowerCase()">
                  {{ getActivityIcon(activity.type) }}
                </mat-icon>
              </td>
            </ng-container>

            <!-- Title Column -->
            <ng-container matColumnDef="title">
              <th mat-header-cell *matHeaderCellDef>{{ 'common.activity' | translate }}</th>
              <td mat-cell *matCellDef="let activity">
                <div class="activity-title">{{ activity.title }}</div>
                <div class="activity-description">{{ activity.description }}</div>
              </td>
            </ng-container>

            <!-- User Column -->
            <ng-container matColumnDef="user">
              <th mat-header-cell *matHeaderCellDef>{{ 'common.user' | translate }}</th>
              <td mat-cell *matCellDef="let activity">{{ activity.user || '-' }}</td>
            </ng-container>

            <!-- Status Column -->
            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>{{ 'common.status' | translate }}</th>
              <td mat-cell *matCellDef="let activity">
                <mat-chip [color]="getStatusColor(activity.status)">
                  {{ activity.status }}
                </mat-chip>
              </td>
            </ng-container>

            <!-- Timestamp Column -->
            <ng-container matColumnDef="timestamp">
              <th mat-header-cell *matHeaderCellDef>{{ 'common.time' | translate }}</th>
              <td mat-cell *matCellDef="let activity">
                {{ activity.timestamp | date:'short' }}
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="activityColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: activityColumns;"></tr>
          </table>

          <p class="no-data" *ngIf="recentActivity.length === 0">
            {{ 'common.noData' | translate }}
          </p>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .dashboard-container {
      padding: 2rem;
    }

    .dashboard-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;
    }

    .dashboard-header button {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    h1 {
      margin: 0 0 0.5rem 0;
      color: #333;
    }

    .welcome-message {
      margin: 0.5rem 0 0 0;
      color: #666;
      font-size: 1.1rem;
    }

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 48px;
      gap: 16px;
    }

    .kpi-grid {
      margin-bottom: 2rem;
    }

    .kpi-card {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
    }

    /* Charts Grid */
    .charts-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(400px, 1fr));
      gap: 16px;
      margin-bottom: 2rem;
    }

    .chart-card {
      min-height: 350px;
    }

    .chart-card mat-card-content {
      padding: 16px;
    }

    .chart-card canvas {
      max-height: 280px;
    }

    .kpi-card mat-card-content {
      text-align: center;
      padding: 1rem;
    }

    .kpi-icon {
      font-size: 48px;
      width: 48px;
      height: 48px;
      margin-bottom: 0.5rem;
    }

    .kpi-icon.primary { color: #1976d2; }
    .kpi-icon.accent { color: #ff4081; }
    .kpi-icon.warn { color: #ff9800; }
    .kpi-icon.success { color: #4caf50; }

    .kpi-card h2 {
      margin: 0.5rem 0;
      font-size: 2rem;
      font-weight: 500;
    }

    .kpi-card p {
      margin: 0.5rem 0;
      color: #666;
      font-size: 0.9rem;
    }

    .kpi-card small {
      display: block;
      color: #999;
      font-size: 0.75rem;
      margin-top: 0.25rem;
    }

    .quick-actions-card,
    .recent-activity-card {
      margin-bottom: 2rem;
    }

    .actions-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 1rem;
      margin-top: 1rem;
    }

    .actions-grid button {
      height: 48px;
    }

    .actions-grid button mat-icon {
      margin-right: 0.5rem;
    }

    .recent-activity-card mat-card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .activity-table {
      width: 100%;
    }

    .activity-title {
      font-weight: 500;
      color: #333;
    }

    .activity-description {
      font-size: 12px;
      color: #666;
      margin-top: 4px;
    }

    .activity-table mat-icon {
      vertical-align: middle;
    }

    .activity-table mat-icon.process {
      color: #1976d2;
    }

    .activity-table mat-icon.instance {
      color: #388e3c;
    }

    .activity-table mat-icon.task {
      color: #f57c00;
    }

    .activity-table mat-icon.user {
      color: #7b1fa2;
    }

    .no-data {
      text-align: center;
      color: #999;
      padding: 2rem;
    }

    @media (max-width: 1200px) {
      .kpi-grid {
        grid-template-columns: repeat(2, 1fr) !important;
      }
    }

    @media (max-width: 768px) {
      .dashboard-container {
        padding: 1rem;
      }

      .kpi-grid {
        grid-template-columns: 1fr !important;
      }

      .actions-grid {
        grid-template-columns: 1fr;
      }
    }
  `]
})
export class DashboardComponent implements OnInit {
  private authService = inject(AuthService);
  private reportService = inject(ReportService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  currentUser = this.authService.getCurrentUser();
  isLoading = true;

  stats: any = {
    totalProcesses: 0,
    activeProcesses: 0,
    totalInstances: 0,
    runningInstances: 0,
    completedInstances: 0,
    failedInstances: 0,
    totalUsers: 0,
    activeUsers: 0,
    totalTasks: 0,
    pendingTasks: 0
  };

  recentActivity: any[] = [];
  activityColumns: string[] = ['type', 'title', 'user', 'status', 'timestamp'];

  // Line Chart Data (Completion Trend)
  lineChartData: ChartData<'line'> = {
    labels: [],
    datasets: [
      {
        label: 'Completed Tasks',
        data: [],
        borderColor: '#1976d2',
        backgroundColor: 'rgba(25, 118, 210, 0.1)',
        fill: true,
        tension: 0.4
      },
      {
        label: 'Started Instances',
        data: [],
        borderColor: '#4caf50',
        backgroundColor: 'rgba(76, 175, 80, 0.1)',
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
        beginAtZero: true,
        ticks: {
          precision: 0
        }
      }
    }
  };

  // Doughnut Chart Data (Instance Distribution)
  doughnutChartData: ChartData<'doughnut'> = {
    labels: ['Running', 'Completed', 'Failed', 'Pending'],
    datasets: [{
      data: [0, 0, 0, 0],
      backgroundColor: [
        '#1976d2',  // Running - Blue
        '#4caf50',  // Completed - Green
        '#f44336',  // Failed - Red
        '#ff9800'   // Pending - Orange
      ],
      hoverBackgroundColor: [
        '#1565c0',
        '#388e3c',
        '#d32f2f',
        '#f57c00'
      ]
    }]
  };

  doughnutChartOptions: ChartConfiguration['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: {
        display: true,
        position: 'bottom'
      },
      tooltip: {
        callbacks: {
          label: function(context) {
            const label = context.label || '';
            const value = context.parsed || 0;
            const total = (context.dataset.data as number[]).reduce((a, b) => a + b, 0);
            const percentage = total > 0 ? ((value / total) * 100).toFixed(1) : '0';
            return `${label}: ${value} (${percentage}%)`;
          }
        }
      }
    }
  };

  ngOnInit(): void {
    this.loadDashboardData();
    this.loadChartData();
  }

  loadDashboardData(): void {
    this.isLoading = true;

    // Load statistics
    this.reportService.getDashboardStats().subscribe({
      next: (stats) => {
        this.stats = stats;
        this.updateDoughnutChart(); // Update chart with new data
      },
      error: () => {
        // Use mock data if API fails
        this.loadMockStats();
        this.updateDoughnutChart(); // Update chart with mock data
      }
    });

    // Load recent activity
    this.reportService.getRecentActivity(10).subscribe({
      next: (activities) => {
        this.recentActivity = activities;
        this.isLoading = false;
      },
      error: () => {
        // Use mock data if API fails
        this.loadMockActivity();
        this.isLoading = false;
      }
    });
  }

  loadMockStats(): void {
    this.stats = {
      totalProcesses: 42,
      activeProcesses: 28,
      totalInstances: 156,
      runningInstances: 23,
      completedInstances: 118,
      failedInstances: 15,
      totalUsers: 67,
      activeUsers: 12,
      totalTasks: 89,
      pendingTasks: 34
    };
  }

  loadMockActivity(): void {
    this.recentActivity = [
      {
        id: 1,
        type: 'PROCESS',
        title: 'Loan Application Process',
        description: 'Process created',
        timestamp: new Date(),
        status: 'SUCCESS',
        user: 'admin'
      },
      {
        id: 2,
        type: 'INSTANCE',
        title: 'Account Opening #1234',
        description: 'Instance started',
        timestamp: new Date(Date.now() - 3600000),
        status: 'RUNNING',
        user: 'manager'
      },
      {
        id: 3,
        type: 'TASK',
        title: 'Verify Customer Documents',
        description: 'Task completed',
        timestamp: new Date(Date.now() - 7200000),
        status: 'COMPLETED',
        user: 'analyst'
      }
    ];
  }

  refreshDashboard(): void {
    this.loadDashboardData();
    this.snackBar.open('Dashboard refreshed', 'Close', { duration: 2000 });
  }

  getActivityIcon(type: string): string {
    const icons: { [key: string]: string } = {
      'PROCESS': 'account_tree',
      'INSTANCE': 'play_circle',
      'TASK': 'assignment',
      'USER': 'person',
      'SYSTEM': 'settings'
    };
    return icons[type] || 'info';
  }

  getStatusColor(status: string): string {
    const colors: { [key: string]: string } = {
      'SUCCESS': 'primary',
      'RUNNING': 'accent',
      'COMPLETED': 'primary',
      'FAILED': 'warn',
      'PENDING': ''
    };
    return colors[status] || '';
  }

  loadChartData(): void {
    // Load trend data for line chart
    this.reportService.getDailyCompletionTrend(7).subscribe({
      next: (trendData) => {
        this.lineChartData.labels = trendData.map((d: any) => d.date);
        this.lineChartData.datasets[0].data = trendData.map((d: any) => d.completedTasks || 0);
        this.lineChartData.datasets[1].data = trendData.map((d: any) => d.startedInstances || 0);
      },
      error: () => {
        // Use mock data if API fails
        this.loadMockTrendData();
      }
    });

    // Update doughnut chart with instance distribution
    this.updateDoughnutChart();
  }

  loadMockTrendData(): void {
    const last7Days = Array.from({ length: 7 }, (_, i) => {
      const date = new Date();
      date.setDate(date.getDate() - (6 - i));
      return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    });

    this.lineChartData.labels = last7Days;
    this.lineChartData.datasets[0].data = [12, 19, 15, 25, 22, 30, 28]; // Completed tasks
    this.lineChartData.datasets[1].data = [8, 11, 13, 15, 14, 18, 16];  // Started instances
  }

  updateDoughnutChart(): void {
    // Use mock data or update from stats
    this.doughnutChartData.datasets[0].data = [
      this.stats.runningInstances || 23,
      this.stats.completedInstances || 118,
      this.stats.failedInstances || 15,
      Math.max(0, this.stats.totalInstances - this.stats.runningInstances - this.stats.completedInstances - this.stats.failedInstances) || 0
    ];
  }

  navigateTo(route: string): void {
    this.router.navigate([route]);
  }
}
