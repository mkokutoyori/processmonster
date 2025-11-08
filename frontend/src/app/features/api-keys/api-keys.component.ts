import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { ApiKeyService } from '../../core/services/api-key.service';
import { ApiKey } from '../../core/models/process.model';

/**
 * API Keys Management Page
 */
@Component({
  selector: 'app-api-keys',
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
    MatInputModule
  ],
  template: `
    <div class="api-keys-container">
      <div class="header">
        <h1>API Keys Management</h1>
        <button mat-raised-button color="primary" (click)="openCreateDialog()">
          <mat-icon>add</mat-icon>
          Create API Key
        </button>
      </div>

      <div class="search-bar">
        <mat-form-field appearance="outline" class="search-field">
          <mat-label>Search API Keys</mat-label>
          <input matInput (keyup)="onSearch($event)" placeholder="Search by name or description">
          <mat-icon matSuffix>search</mat-icon>
        </mat-form-field>
      </div>

      <div class="table-container">
        <table mat-table [dataSource]="apiKeys" class="api-keys-table">
          <!-- Name Column -->
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Name</th>
            <td mat-cell *matCellDef="let key">{{ key.name }}</td>
          </ng-container>

          <!-- Key Prefix Column -->
          <ng-container matColumnDef="keyPrefix">
            <th mat-header-cell *matHeaderCellDef>Key Prefix</th>
            <td mat-cell *matCellDef="let key">
              <code>{{ key.keyPrefix }}...</code>
            </td>
          </ng-container>

          <!-- Permissions Column -->
          <ng-container matColumnDef="permissions">
            <th mat-header-cell *matHeaderCellDef>Permissions</th>
            <td mat-cell *matCellDef="let key">
              <mat-chip-set>
                <mat-chip *ngFor="let perm of getPermissionsArray(key.permissions).slice(0, 3)">
                  {{ perm }}
                </mat-chip>
                <mat-chip *ngIf="getPermissionsArray(key.permissions).length > 3">
                  +{{ getPermissionsArray(key.permissions).length - 3 }} more
                </mat-chip>
              </mat-chip-set>
            </td>
          </ng-container>

          <!-- Status Column -->
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let key">
              <mat-chip [color]="key.enabled ? 'primary' : 'warn'">
                {{ key.enabled ? 'Active' : 'Disabled' }}
              </mat-chip>
            </td>
          </ng-container>

          <!-- Expires At Column -->
          <ng-container matColumnDef="expiresAt">
            <th mat-header-cell *matHeaderCellDef>Expires</th>
            <td mat-cell *matCellDef="let key">
              {{ key.expiresAt ? (key.expiresAt | date:'short') : 'Never' }}
            </td>
          </ng-container>

          <!-- Last Used Column -->
          <ng-container matColumnDef="lastUsedAt">
            <th mat-header-cell *matHeaderCellDef>Last Used</th>
            <td mat-cell *matCellDef="let key">
              {{ key.lastUsedAt ? (key.lastUsedAt | date:'short') : 'Never' }}
            </td>
          </ng-container>

          <!-- Actions Column -->
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Actions</th>
            <td mat-cell *matCellDef="let key">
              <button mat-icon-button [matMenuTriggerFor]="menu">
                <mat-icon>more_vert</mat-icon>
              </button>
              <mat-menu #menu="matMenu">
                <button mat-menu-item *ngIf="key.enabled" (click)="disableApiKey(key.id)">
                  <mat-icon>block</mat-icon>
                  Disable
                </button>
                <button mat-menu-item *ngIf="!key.enabled" (click)="enableApiKey(key.id)">
                  <mat-icon>check_circle</mat-icon>
                  Enable
                </button>
                <button mat-menu-item (click)="deleteApiKey(key.id)">
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
    .api-keys-container {
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

    .api-keys-table {
      width: 100%;
    }

    code {
      background: #f5f5f5;
      padding: 4px 8px;
      border-radius: 4px;
      font-family: 'Courier New', monospace;
    }
  `]
})
export class ApiKeysComponent implements OnInit {
  apiKeys: ApiKey[] = [];
  displayedColumns: string[] = ['name', 'keyPrefix', 'permissions', 'status', 'expiresAt', 'lastUsedAt', 'actions'];

  pageSize = 20;
  currentPage = 0;
  totalElements = 0;
  searchKeyword = '';

  constructor(
    private apiKeyService: ApiKeyService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadApiKeys();
  }

  loadApiKeys(): void {
    if (this.searchKeyword) {
      this.apiKeyService.searchApiKeys(this.searchKeyword, this.currentPage, this.pageSize)
        .subscribe({
          next: (response) => {
            this.apiKeys = response.content;
            this.totalElements = response.totalElements;
          },
          error: (error) => {
            this.snackBar.open('Failed to load API keys', 'Close', { duration: 3000 });
          }
        });
    } else {
      this.apiKeyService.getAllApiKeys(this.currentPage, this.pageSize)
        .subscribe({
          next: (response) => {
            this.apiKeys = response.content;
            this.totalElements = response.totalElements;
          },
          error: (error) => {
            this.snackBar.open('Failed to load API keys', 'Close', { duration: 3000 });
          }
        });
    }
  }

  onSearch(event: Event): void {
    const target = event.target as HTMLInputElement;
    this.searchKeyword = target.value;
    this.currentPage = 0;
    this.loadApiKeys();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadApiKeys();
  }

  getPermissionsArray(permissions: Set<string> | string[] | any): string[] {
    if (Array.isArray(permissions)) {
      return permissions;
    }
    if (permissions instanceof Set) {
      return Array.from(permissions);
    }
    return [];
  }

  openCreateDialog(): void {
    // TODO: Open create API key dialog
    this.snackBar.open('Create API Key dialog - To be implemented', 'Close', { duration: 3000 });
  }

  enableApiKey(id: number): void {
    this.apiKeyService.enableApiKey(id).subscribe({
      next: () => {
        this.snackBar.open('API Key enabled successfully', 'Close', { duration: 3000 });
        this.loadApiKeys();
      },
      error: () => {
        this.snackBar.open('Failed to enable API key', 'Close', { duration: 3000 });
      }
    });
  }

  disableApiKey(id: number): void {
    this.apiKeyService.disableApiKey(id).subscribe({
      next: () => {
        this.snackBar.open('API Key disabled successfully', 'Close', { duration: 3000 });
        this.loadApiKeys();
      },
      error: () => {
        this.snackBar.open('Failed to disable API key', 'Close', { duration: 3000 });
      }
    });
  }

  deleteApiKey(id: number): void {
    if (confirm('Are you sure you want to delete this API key?')) {
      this.apiKeyService.deleteApiKey(id).subscribe({
        next: () => {
          this.snackBar.open('API Key deleted successfully', 'Close', { duration: 3000 });
          this.loadApiKeys();
        },
        error: () => {
          this.snackBar.open('Failed to delete API key', 'Close', { duration: 3000 });
        }
      });
    }
  }
}
