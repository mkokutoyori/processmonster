import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialogModule } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { WebhookService } from '../../core/services/webhook.service';
import { Webhook } from '../../core/models/process.model';

/**
 * Webhooks Management Page
 */
@Component({
  selector: 'app-webhooks',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatDialogModule,
    MatSnackBarModule,
    MatPaginatorModule,
    MatFormFieldModule,
    MatInputModule,
    MatMenuModule
  ],
  template: `
    <div class="webhooks-container">
      <div class="header">
        <h1>Webhooks Management</h1>
        <button mat-raised-button color="primary" (click)="openCreateDialog()">
          <mat-icon>add</mat-icon>
          Create Webhook
        </button>
      </div>

      <div class="search-bar">
        <mat-form-field appearance="outline" class="search-field">
          <mat-label>Search Webhooks</mat-label>
          <input matInput (keyup)="onSearch($event)" placeholder="Search by name, URL or description">
          <mat-icon matSuffix>search</mat-icon>
        </mat-form-field>
      </div>

      <div class="table-container">
        <table mat-table [dataSource]="webhooks" class="webhooks-table">
          <!-- Name Column -->
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Name</th>
            <td mat-cell *matCellDef="let webhook">{{ webhook.name }}</td>
          </ng-container>

          <!-- URL Column -->
          <ng-container matColumnDef="url">
            <th mat-header-cell *matHeaderCellDef>URL</th>
            <td mat-cell *matCellDef="let webhook">
              <code class="url">{{ webhook.url }}</code>
            </td>
          </ng-container>

          <!-- Events Column -->
          <ng-container matColumnDef="events">
            <th mat-header-cell *matHeaderCellDef>Events</th>
            <td mat-cell *matCellDef="let webhook">
              <mat-chip-set>
                <mat-chip *ngFor="let event of getEventsArray(webhook.events).slice(0, 2)">
                  {{ event }}
                </mat-chip>
                <mat-chip *ngIf="getEventsArray(webhook.events).length > 2">
                  +{{ getEventsArray(webhook.events).length - 2 }} more
                </mat-chip>
              </mat-chip-set>
            </td>
          </ng-container>

          <!-- Status Column -->
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let webhook">
              <mat-chip [color]="webhook.enabled ? 'primary' : 'warn'">
                {{ webhook.enabled ? 'Active' : 'Disabled' }}
              </mat-chip>
            </td>
          </ng-container>

          <!-- Success Rate Column -->
          <ng-container matColumnDef="successRate">
            <th mat-header-cell *matHeaderCellDef>Success Rate</th>
            <td mat-cell *matCellDef="let webhook">
              <span [class.success-rate]="getSuccessRate(webhook) >= 90"
                    [class.warning-rate]="getSuccessRate(webhook) < 90 && getSuccessRate(webhook) >= 50"
                    [class.error-rate]="getSuccessRate(webhook) < 50">
                {{ getSuccessRate(webhook).toFixed(1) }}%
              </span>
              <small class="count">({{ webhook.successCount }}/{{ webhook.successCount + webhook.failureCount }})</small>
            </td>
          </ng-container>

          <!-- Last Success Column -->
          <ng-container matColumnDef="lastSuccess">
            <th mat-header-cell *matHeaderCellDef>Last Success</th>
            <td mat-cell *matCellDef="let webhook">
              {{ webhook.lastSuccessAt ? (webhook.lastSuccessAt | date:'short') : 'Never' }}
            </td>
          </ng-container>

          <!-- Actions Column -->
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Actions</th>
            <td mat-cell *matCellDef="let webhook">
              <button mat-icon-button [matMenuTriggerFor]="menu">
                <mat-icon>more_vert</mat-icon>
              </button>
              <mat-menu #menu="matMenu">
                <button mat-menu-item (click)="testWebhook(webhook.id)">
                  <mat-icon>send</mat-icon>
                  Test
                </button>
                <button mat-menu-item (click)="viewDeliveries(webhook.id)">
                  <mat-icon>history</mat-icon>
                  View Deliveries
                </button>
                <button mat-menu-item *ngIf="webhook.enabled" (click)="disableWebhook(webhook.id)">
                  <mat-icon>block</mat-icon>
                  Disable
                </button>
                <button mat-menu-item *ngIf="!webhook.enabled" (click)="enableWebhook(webhook.id)">
                  <mat-icon>check_circle</mat-icon>
                  Enable
                </button>
                <button mat-menu-item (click)="editWebhook(webhook.id)">
                  <mat-icon>edit</mat-icon>
                  Edit
                </button>
                <button mat-menu-item (click)="deleteWebhook(webhook.id)">
                  <mat-icon>delete</mat-icon>
                  Delete
                </button>
              </mat-menu>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
        </table>

        <mat-paginator
          [length]="totalElements"
          [pageSize]="pageSize"
          [pageSizeOptions]="[10, 20, 50, 100]"
          (page)="onPageChange($event)"
          showFirstLastButtons>
        </mat-paginator>
      </div>
    </div>
  `,
  styles: [`
    .webhooks-container {
      padding: 24px;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
    }

    .search-bar {
      margin-bottom: 16px;
    }

    .search-field {
      width: 400px;
    }

    .table-container {
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .webhooks-table {
      width: 100%;
    }

    code.url {
      background: #f5f5f5;
      padding: 4px 8px;
      border-radius: 4px;
      font-family: 'Courier New', monospace;
      font-size: 12px;
      max-width: 300px;
      display: inline-block;
      overflow: hidden;
      text-overflow: ellipsis;
      white-space: nowrap;
    }

    .success-rate {
      color: #4caf50;
      font-weight: 500;
    }

    .warning-rate {
      color: #ff9800;
      font-weight: 500;
    }

    .error-rate {
      color: #f44336;
      font-weight: 500;
    }

    .count {
      color: #666;
      margin-left: 8px;
    }
  `]
})
export class WebhooksComponent implements OnInit {
  webhooks: Webhook[] = [];
  displayedColumns: string[] = ['name', 'url', 'events', 'status', 'successRate', 'lastSuccess', 'actions'];

  pageSize = 20;
  currentPage = 0;
  totalElements = 0;
  searchKeyword = '';

  constructor(
    private webhookService: WebhookService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadWebhooks();
  }

  loadWebhooks(): void {
    if (this.searchKeyword) {
      this.webhookService.searchWebhooks(this.searchKeyword, this.currentPage, this.pageSize)
        .subscribe({
          next: (response) => {
            this.webhooks = response.content;
            this.totalElements = response.totalElements;
          },
          error: () => {
            this.snackBar.open('Failed to load webhooks', 'Close', { duration: 3000 });
          }
        });
    } else {
      this.webhookService.getAllWebhooks(this.currentPage, this.pageSize)
        .subscribe({
          next: (response) => {
            this.webhooks = response.content;
            this.totalElements = response.totalElements;
          },
          error: () => {
            this.snackBar.open('Failed to load webhooks', 'Close', { duration: 3000 });
          }
        });
    }
  }

  onSearch(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.searchKeyword = target.value;
    this.currentPage = 0;
    this.loadWebhooks();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadWebhooks();
  }

  getEventsArray(events: Set<string> | string[] | any): string[] {
    if (Array.isArray(events)) {
      return events;
    }
    if (events instanceof Set) {
      return Array.from(events);
    }
    return [];
  }

  getSuccessRate(webhook: Webhook): number {
    const total = webhook.successCount + webhook.failureCount;
    if (total === 0) return 100;
    return (webhook.successCount / total) * 100;
  }

  openCreateDialog(): void {
    this.snackBar.open('Create Webhook dialog - To be implemented', 'Close', { duration: 3000 });
  }

  testWebhook(id: number): void {
    this.webhookService.testWebhook(id).subscribe({
      next: () => {
        this.snackBar.open('Test event queued for delivery', 'Close', { duration: 3000 });
      },
      error: () => {
        this.snackBar.open('Failed to test webhook', 'Close', { duration: 3000 });
      }
    });
  }

  viewDeliveries(id: number): void {
    this.snackBar.open('Delivery history - To be implemented', 'Close', { duration: 3000 });
  }

  enableWebhook(id: number): void {
    this.webhookService.enableWebhook(id).subscribe({
      next: () => {
        this.snackBar.open('Webhook enabled successfully', 'Close', { duration: 3000 });
        this.loadWebhooks();
      },
      error: () => {
        this.snackBar.open('Failed to enable webhook', 'Close', { duration: 3000 });
      }
    });
  }

  disableWebhook(id: number): void {
    this.webhookService.disableWebhook(id).subscribe({
      next: () => {
        this.snackBar.open('Webhook disabled successfully', 'Close', { duration: 3000 });
        this.loadWebhooks();
      },
      error: () => {
        this.snackBar.open('Failed to disable webhook', 'Close', { duration: 3000 });
      }
    });
  }

  editWebhook(id: number): void {
    this.snackBar.open('Edit Webhook - To be implemented', 'Close', { duration: 3000 });
  }

  deleteWebhook(id: number): void {
    if (confirm('Are you sure you want to delete this webhook?')) {
      this.webhookService.deleteWebhook(id).subscribe({
        next: () => {
          this.snackBar.open('Webhook deleted successfully', 'Close', { duration: 3000 });
          this.loadWebhooks();
        },
        error: () => {
          this.snackBar.open('Failed to delete webhook', 'Close', { duration: 3000 });
        }
      });
    }
  }
}
