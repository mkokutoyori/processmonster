import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatTabsModule } from '@angular/material/tabs';
import { MatTableModule } from '@angular/material/table';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatExpansionModule } from '@angular/material/expansion';
import { ProcessInstanceService } from '../../core/services/process-instance.service';
import { NotificationService } from '../../core/services/notification.service';
import { ProcessInstance, ExecutionHistory } from '../../core/models/process.model';

/**
 * Component for viewing process instance details
 */
@Component({
  selector: 'app-instance-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatTabsModule,
    MatTableModule,
    MatTooltipModule,
    MatExpansionModule
  ],
  template: `
    <div class="instance-detail-container" *ngIf="instance">
      <div class="header">
        <div class="title-section">
          <button mat-icon-button (click)="goBack()" matTooltip="Back to list">
            <mat-icon>arrow_back</mat-icon>
          </button>
          <div class="title-info">
            <h1>Instance #{{ instance.id }}</h1>
            <div class="meta-info">
              <code>{{ instance.processKey }}:v{{ instance.processVersion }}</code>
              <mat-chip [ngClass]="getStatusClass(instance.status)">
                <mat-icon>{{ getStatusIcon(instance.status) }}</mat-icon>
                {{ instance.status }}
              </mat-chip>
            </div>
          </div>
        </div>
        <div class="action-buttons">
          <button mat-raised-button
                  color="warn"
                  *ngIf="instance.status === 'RUNNING'"
                  (click)="suspendInstance()">
            <mat-icon>pause</mat-icon>
            Suspend
          </button>
          <button mat-raised-button
                  color="primary"
                  *ngIf="instance.status === 'SUSPENDED'"
                  (click)="resumeInstance()">
            <mat-icon>play_arrow</mat-icon>
            Resume
          </button>
          <button mat-stroked-button
                  color="warn"
                  *ngIf="instance.status === 'RUNNING' || instance.status === 'SUSPENDED'"
                  (click)="terminateInstance()">
            <mat-icon>stop</mat-icon>
            Terminate
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
                <mat-card-title>Instance Information</mat-card-title>
              </mat-card-header>
              <mat-card-content>
                <div class="info-grid">
                  <div class="info-item">
                    <label>Instance ID</label>
                    <code>{{ instance.id }}</code>
                  </div>
                  <div class="info-item">
                    <label>Process Definition</label>
                    <span>{{ instance.processDefinitionName }}</span>
                  </div>
                  <div class="info-item">
                    <label>Process Key</label>
                    <code>{{ instance.processKey }}</code>
                  </div>
                  <div class="info-item">
                    <label>Version</label>
                    <span>{{ instance.processVersion }}</span>
                  </div>
                  <div class="info-item">
                    <label>Business Key</label>
                    <code *ngIf="instance.businessKey">{{ instance.businessKey }}</code>
                    <span *ngIf="!instance.businessKey" class="no-value">-</span>
                  </div>
                  <div class="info-item">
                    <label>Status</label>
                    <mat-chip [ngClass]="getStatusClass(instance.status)">
                      <mat-icon>{{ getStatusIcon(instance.status) }}</mat-icon>
                      {{ instance.status }}
                    </mat-chip>
                  </div>
                  <div class="info-item">
                    <label>Current Activity</label>
                    <span *ngIf="instance.currentActivityName">{{ instance.currentActivityName }}</span>
                    <span *ngIf="!instance.currentActivityName" class="no-value">-</span>
                  </div>
                  <div class="info-item">
                    <label>Started By</label>
                    <span>{{ instance.startedBy }}</span>
                  </div>
                  <div class="info-item">
                    <label>Start Time</label>
                    <span>{{ instance.startTime | date:'medium' }}</span>
                  </div>
                  <div class="info-item">
                    <label>End Time</label>
                    <span *ngIf="instance.endTime">{{ instance.endTime | date:'medium' }}</span>
                    <span *ngIf="!instance.endTime" class="no-value">-</span>
                  </div>
                  <div class="info-item">
                    <label>Duration</label>
                    <span *ngIf="instance.durationMillis">{{ formatDuration(instance.durationMillis) }}</span>
                    <span *ngIf="!instance.durationMillis && instance.status === 'RUNNING'" class="running">
                      {{ getRunningDuration(instance.startTime) }}
                    </span>
                    <span *ngIf="!instance.durationMillis && instance.status !== 'RUNNING'" class="no-value">-</span>
                  </div>
                  <div class="info-item full-width" *ngIf="instance.errorMessage">
                    <label>Error Message</label>
                    <div class="error-message">
                      <mat-icon>error</mat-icon>
                      <span>{{ instance.errorMessage }}</span>
                    </div>
                  </div>
                </div>
              </mat-card-content>
            </mat-card>
          </div>
        </mat-tab>

        <!-- Variables Tab -->
        <mat-tab label="Variables">
          <div class="tab-content">
            <mat-card class="variables-card">
              <mat-card-header>
                <mat-card-title>Process Variables</mat-card-title>
                <button mat-icon-button (click)="refreshVariables()" matTooltip="Refresh variables">
                  <mat-icon>refresh</mat-icon>
                </button>
              </mat-card-header>
              <mat-card-content>
                <div *ngIf="variables && objectKeys(variables).length > 0" class="variables-list">
                  <mat-expansion-panel *ngFor="let key of objectKeys(variables)">
                    <mat-expansion-panel-header>
                      <mat-panel-title>
                        <code>{{ key }}</code>
                      </mat-panel-title>
                      <mat-panel-description>
                        {{ getVariableType(variables[key]) }}
                      </mat-panel-description>
                    </mat-expansion-panel-header>
                    <div class="variable-content">
                      <pre>{{ formatVariableValue(variables[key]) }}</pre>
                    </div>
                  </mat-expansion-panel>
                </div>
                <p *ngIf="!variables || objectKeys(variables).length === 0" class="no-data">
                  No variables defined for this instance
                </p>
              </mat-card-content>
            </mat-card>
          </div>
        </mat-tab>

        <!-- History Tab -->
        <mat-tab label="Execution History">
          <div class="tab-content">
            <mat-card class="history-card">
              <mat-card-header>
                <mat-card-title>Execution History</mat-card-title>
                <button mat-icon-button (click)="refreshHistory()" matTooltip="Refresh history">
                  <mat-icon>refresh</mat-icon>
                </button>
              </mat-card-header>
              <mat-card-content>
                <div *ngIf="history && history.length > 0" class="history-timeline">
                  <div *ngFor="let event of history" class="history-event">
                    <div class="event-indicator" [ngClass]="getEventClass(event.eventType)">
                      <mat-icon>{{ getEventIcon(event.eventType) }}</mat-icon>
                    </div>
                    <div class="event-content">
                      <div class="event-header">
                        <span class="event-type">{{ event.eventType }}</span>
                        <span class="event-time">{{ event.timestamp | date:'short' }}</span>
                      </div>
                      <div class="event-details" *ngIf="event.activityName || event.activityId">
                        <span *ngIf="event.activityName"><strong>Activity:</strong> {{ event.activityName }}</span>
                        <span *ngIf="event.activityId"><code>{{ event.activityId }}</code></span>
                      </div>
                      <div class="event-duration" *ngIf="event.durationMillis">
                        <mat-icon>schedule</mat-icon>
                        Duration: {{ formatDuration(event.durationMillis) }}
                      </div>
                      <div class="event-user" *ngIf="event.performedBy">
                        <mat-icon>person</mat-icon>
                        {{ event.performedBy }}
                      </div>
                      <div class="event-error" *ngIf="event.errorMessage">
                        <mat-icon>error</mat-icon>
                        {{ event.errorMessage }}
                      </div>
                    </div>
                  </div>
                </div>
                <p *ngIf="!history || history.length === 0" class="no-data">
                  No execution history available
                </p>
              </mat-card-content>
            </mat-card>
          </div>
        </mat-tab>
      </mat-tab-group>
    </div>

    <div class="loading-container" *ngIf="!instance && !error">
      <mat-icon class="spinning">refresh</mat-icon>
      <p>Loading instance details...</p>
    </div>

    <div class="error-container" *ngIf="error">
      <mat-icon color="warn">error</mat-icon>
      <h2>Error loading instance</h2>
      <p>{{ error }}</p>
      <button mat-raised-button (click)="goBack()">Back to list</button>
    </div>
  `,
  styles: [`
    .instance-detail-container {
      padding: 24px;
      max-width: 1400px;
      margin: 0 auto;
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
      align-items: center;
      gap: 16px;
      flex: 1;
    }

    .title-info {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .title-info h1 {
      margin: 0;
      font-size: 28px;
      font-weight: 500;
    }

    .meta-info {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    code {
      background: #f5f5f5;
      padding: 4px 8px;
      border-radius: 4px;
      font-family: 'Courier New', monospace;
      font-size: 14px;
    }

    .action-buttons {
      display: flex;
      gap: 12px;
      flex-shrink: 0;
    }

    .tab-content {
      padding: 24px 0;
    }

    .info-card, .variables-card, .history-card {
      margin-bottom: 24px;
    }

    mat-card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
    }

    .info-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 24px;
      margin-top: 16px;
    }

    .info-item {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .info-item.full-width {
      grid-column: 1 / -1;
    }

    .info-item label {
      font-weight: 500;
      color: #666;
      font-size: 14px;
    }

    .info-item span {
      font-size: 16px;
      color: #333;
    }

    .no-value {
      color: #999;
      font-style: italic;
    }

    .running {
      color: #2196f3;
      font-weight: 500;
    }

    .error-message {
      display: flex;
      align-items: flex-start;
      gap: 12px;
      padding: 16px;
      background: #ffebee;
      border-radius: 4px;
      color: #c62828;
    }

    .error-message mat-icon {
      color: #c62828;
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

    /* Variables */
    .variables-list {
      margin-top: 16px;
    }

    .variable-content {
      padding: 16px;
      background: #f5f5f5;
      border-radius: 4px;
    }

    .variable-content pre {
      margin: 0;
      font-family: 'Courier New', monospace;
      font-size: 14px;
      white-space: pre-wrap;
      word-wrap: break-word;
    }

    /* History Timeline */
    .history-timeline {
      margin-top: 16px;
      position: relative;
      padding-left: 60px;
    }

    .history-timeline::before {
      content: '';
      position: absolute;
      left: 24px;
      top: 0;
      bottom: 0;
      width: 2px;
      background: #e0e0e0;
    }

    .history-event {
      position: relative;
      margin-bottom: 24px;
      display: flex;
      gap: 16px;
    }

    .event-indicator {
      position: absolute;
      left: -48px;
      width: 48px;
      height: 48px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      background: white;
      border: 2px solid #e0e0e0;
    }

    .event-indicator mat-icon {
      font-size: 24px;
      width: 24px;
      height: 24px;
    }

    .event-indicator.event-process {
      background: #e3f2fd;
      border-color: #2196f3;
    }

    .event-indicator.event-process mat-icon {
      color: #2196f3;
    }

    .event-indicator.event-activity {
      background: #f3e5f5;
      border-color: #9c27b0;
    }

    .event-indicator.event-activity mat-icon {
      color: #9c27b0;
    }

    .event-indicator.event-variable {
      background: #fff3e0;
      border-color: #ff9800;
    }

    .event-indicator.event-variable mat-icon {
      color: #ff9800;
    }

    .event-indicator.event-error {
      background: #ffebee;
      border-color: #f44336;
    }

    .event-indicator.event-error mat-icon {
      color: #f44336;
    }

    .event-content {
      flex: 1;
      padding: 16px;
      background: #fafafa;
      border-radius: 8px;
      border-left: 3px solid #e0e0e0;
    }

    .event-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
    }

    .event-type {
      font-weight: 500;
      color: #333;
      font-size: 14px;
    }

    .event-time {
      color: #666;
      font-size: 12px;
    }

    .event-details, .event-duration, .event-user, .event-error {
      margin-top: 8px;
      font-size: 14px;
      color: #666;
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .event-details mat-icon, .event-duration mat-icon, .event-user mat-icon, .event-error mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
    }

    .event-error {
      color: #f44336;
    }

    .no-data {
      text-align: center;
      color: #999;
      padding: 32px;
      font-style: italic;
    }

    .loading-container, .error-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 64px;
      text-align: center;
    }

    .loading-container mat-icon.spinning {
      font-size: 48px;
      width: 48px;
      height: 48px;
      animation: spin 1s linear infinite;
      color: #3f51b5;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }

    .error-container mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
    }

    .error-container h2 {
      margin: 16px 0 8px;
    }

    .error-container p {
      color: #666;
      margin-bottom: 24px;
    }
  `]
})
export class InstanceDetailComponent implements OnInit {
  instance: ProcessInstance | null = null;
  variables: { [key: string]: any } | null = null;
  history: ExecutionHistory[] = [];
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private instanceService: ProcessInstanceService,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadInstanceDetail(+id);
      this.loadVariables(+id);
      this.loadHistory(+id);
    }
  }

  loadInstanceDetail(id: number): void {
    this.instanceService.getInstanceById(id).subscribe({
      next: (instance) => {
        this.instance = instance;
      },
      error: (error) => {
        console.error('Error loading instance detail:', error);
        this.error = error.error?.message || 'Failed to load instance details';
      }
    });
  }

  loadVariables(id: number): void {
    this.instanceService.getVariables(id).subscribe({
      next: (variables) => {
        this.variables = variables;
      },
      error: (error) => {
        console.error('Error loading variables:', error);
      }
    });
  }

  loadHistory(id: number): void {
    this.instanceService.getHistory(id).subscribe({
      next: (history) => {
        this.history = history;
      },
      error: (error) => {
        console.error('Error loading history:', error);
      }
    });
  }

  refresh(): void {
    if (this.instance) {
      this.loadInstanceDetail(this.instance.id);
    }
  }

  refreshVariables(): void {
    if (this.instance) {
      this.loadVariables(this.instance.id);
    }
  }

  refreshHistory(): void {
    if (this.instance) {
      this.loadHistory(this.instance.id);
    }
  }

  goBack(): void {
    this.router.navigate(['/instances']);
  }

  suspendInstance(): void {
    if (!this.instance) return;
    const reason = prompt('Reason for suspension (optional):');
    this.instanceService.suspendInstance(this.instance.id, reason || undefined).subscribe({
      next: () => {
        this.refresh();
        this.refreshHistory();
      },
      error: (error) => {
        console.error('Error suspending instance:', error);
        this.notificationService.error(error.error?.message || 'Failed to suspend instance');
      }
    });
  }

  resumeInstance(): void {
    if (!this.instance) return;
    this.instanceService.resumeInstance(this.instance.id).subscribe({
      next: () => {
        this.refresh();
        this.refreshHistory();
      },
      error: (error) => {
        console.error('Error resuming instance:', error);
        this.notificationService.error(error.error?.message || 'Failed to resume instance');
      }
    });
  }

  terminateInstance(): void {
    if (!this.instance) return;
    const reason = prompt('Reason for termination (optional):');
    if (confirm(`Are you sure you want to terminate instance #${this.instance.id}?`)) {
      this.instanceService.terminateInstance(this.instance.id, reason || undefined).subscribe({
        next: () => {
          this.refresh();
          this.refreshHistory();
        },
        error: (error) => {
          console.error('Error terminating instance:', error);
          this.notificationService.error(error.error?.message || 'Failed to terminate instance');
        }
      });
    }
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

  objectKeys(obj: any): string[] {
    return obj ? Object.keys(obj) : [];
  }

  getVariableType(value: any): string {
    if (value === null) return 'null';
    if (Array.isArray(value)) return 'array';
    return typeof value;
  }

  formatVariableValue(value: any): string {
    if (typeof value === 'object') {
      return JSON.stringify(value, null, 2);
    }
    return String(value);
  }

  getEventClass(eventType: string): string {
    if (eventType.startsWith('PROCESS_')) return 'event-process';
    if (eventType.startsWith('ACTIVITY_')) return 'event-activity';
    if (eventType.startsWith('VARIABLE_')) return 'event-variable';
    if (eventType.includes('ERROR') || eventType.includes('FAILED')) return 'event-error';
    return '';
  }

  getEventIcon(eventType: string): string {
    const icons: { [key: string]: string } = {
      'PROCESS_STARTED': 'play_circle',
      'PROCESS_SUSPENDED': 'pause_circle',
      'PROCESS_RESUMED': 'play_arrow',
      'PROCESS_COMPLETED': 'check_circle',
      'PROCESS_FAILED': 'error',
      'PROCESS_TERMINATED': 'cancel',
      'ACTIVITY_STARTED': 'play_arrow',
      'ACTIVITY_COMPLETED': 'check',
      'ACTIVITY_FAILED': 'error',
      'VARIABLE_CREATED': 'add',
      'VARIABLE_UPDATED': 'edit',
      'VARIABLE_DELETED': 'delete'
    };
    return icons[eventType] || 'circle';
  }
}
