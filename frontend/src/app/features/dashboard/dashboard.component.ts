/**
 * Dashboard Component
 *
 * Main dashboard with KPIs and quick access to features.
 */
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatGridListModule } from '@angular/material/grid-list';
import { TranslateModule } from '@ngx-translate/core';
import { AuthService } from '../../core/services/auth.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatGridListModule,
    TranslateModule
  ],
  template: `
    <div class="dashboard-container">
      <h1>{{ 'dashboard.title' | translate }}</h1>
      <p class="welcome-message">
        {{ 'app.welcome' | translate }}, {{ currentUser?.firstName || currentUser?.username }}!
      </p>

      <!-- KPI Cards -->
      <mat-grid-list cols="4" rowHeight="150px" [gutterSize]="'16px'" class="kpi-grid">
        <mat-grid-tile>
          <mat-card class="kpi-card">
            <mat-card-content>
              <mat-icon class="kpi-icon primary">trending_up</mat-icon>
              <h2>{{ processesActive }}</h2>
              <p>{{ 'dashboard.processesActive' | translate }}</p>
            </mat-card-content>
          </mat-card>
        </mat-grid-tile>

        <mat-grid-tile>
          <mat-card class="kpi-card">
            <mat-card-content>
              <mat-icon class="kpi-icon accent">assignment</mat-icon>
              <h2>{{ tasksPending }}</h2>
              <p>{{ 'dashboard.tasksPending' | translate }}</p>
            </mat-card-content>
          </mat-card>
        </mat-grid-tile>

        <mat-grid-tile>
          <mat-card class="kpi-card">
            <mat-card-content>
              <mat-icon class="kpi-icon warn">alarm</mat-icon>
              <h2>{{ tasksOverdue }}</h2>
              <p>{{ 'dashboard.tasksOverdue' | translate }}</p>
            </mat-card-content>
          </mat-card>
        </mat-grid-tile>

        <mat-grid-tile>
          <mat-card class="kpi-card">
            <mat-card-content>
              <mat-icon class="kpi-icon success">check_circle</mat-icon>
              <h2>{{ avgCompletionTime }}</h2>
              <p>{{ 'dashboard.avgCompletionTime' | translate }}</p>
            </mat-card-content>
          </mat-card>
        </mat-grid-tile>
      </mat-grid-list>

      <!-- Quick Actions -->
      <mat-card class="quick-actions-card">
        <mat-card-header>
          <mat-card-title>{{ 'common.actions' | translate }}</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <div class="actions-grid">
            <button mat-raised-button color="primary">
              <mat-icon>play_arrow</mat-icon>
              {{ 'instance.start' | translate }}
            </button>
            <button mat-raised-button color="accent">
              <mat-icon>add</mat-icon>
              {{ 'process.create' | translate }}
            </button>
            <button mat-raised-button>
              <mat-icon>people</mat-icon>
              {{ 'user.list' | translate }}
            </button>
            <button mat-raised-button>
              <mat-icon>assessment</mat-icon>
              {{ 'report.generate' | translate }}
            </button>
          </div>
        </mat-card-content>
      </mat-card>

      <!-- Recent Activity (Placeholder) -->
      <mat-card class="recent-activity-card">
        <mat-card-header>
          <mat-card-title>{{ 'common.recentActivity' | translate }}</mat-card-title>
        </mat-card-header>
        <mat-card-content>
          <p class="no-data">{{ 'common.noData' | translate }}</p>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .dashboard-container {
      padding: 2rem;
    }

    h1 {
      margin: 0 0 0.5rem 0;
      color: #333;
    }

    .welcome-message {
      margin: 0 0 2rem 0;
      color: #666;
      font-size: 1.1rem;
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
      margin: 0;
      color: #666;
      font-size: 0.9rem;
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

  currentUser = this.authService.getCurrentUser();

  // Mock KPI data (will be replaced with real data from API)
  processesActive = 12;
  tasksPending = 8;
  tasksOverdue = 3;
  avgCompletionTime = '2.5h';

  ngOnInit(): void {
    // Load dashboard data
    // TODO: Implement dashboard service to fetch real KPIs
  }
}
