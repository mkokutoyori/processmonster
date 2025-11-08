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
import { MatMenuModule } from '@angular/material/menu';
import { MatCardModule } from '@angular/material/card';
import { MatExpansionModule } from '@angular/material/expansion';
import { AdminService } from '../../core/services/admin.service';
import { SystemParameter, SystemParameterCategory } from '../../core/models/admin.model';

/**
 * System Parameters Management Page
 */
@Component({
  selector: 'app-system-parameters',
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
    MatMenuModule,
    MatCardModule,
    MatExpansionModule
  ],
  template: `
    <div class="system-parameters-container">
      <div class="header">
        <h1>System Parameters</h1>
        <button mat-raised-button color="primary" (click)="openCreateDialog()">
          <mat-icon>add</mat-icon>
          Create Parameter
        </button>
      </div>

      <!-- Statistics Card -->
      <mat-card class="stats-card" *ngIf="stats">
        <mat-card-content>
          <div class="stats-grid">
            <div class="stat-item">
              <div class="stat-label">Total Parameters</div>
              <div class="stat-value">{{ stats.totalParameters }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">Categories</div>
              <div class="stat-value">{{ stats.totalCategories }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">Editable</div>
              <div class="stat-value">{{ stats.editableParameters }}</div>
            </div>
            <div class="stat-item">
              <div class="stat-label">Encrypted</div>
              <div class="stat-value">{{ stats.encryptedParameters }}</div>
            </div>
          </div>
        </mat-card-content>
      </mat-card>

      <!-- Filters -->
      <mat-card class="filters-card">
        <mat-card-content>
          <div class="filters">
            <mat-form-field appearance="outline" class="filter-field">
              <mat-label>Search</mat-label>
              <input matInput [(ngModel)]="searchKeyword" (keyup.enter)="onSearch()"
                     placeholder="Search by key or description">
              <mat-icon matSuffix>search</mat-icon>
            </mat-form-field>

            <mat-form-field appearance="outline" class="filter-field">
              <mat-label>Category</mat-label>
              <mat-select [(ngModel)]="selectedCategory" (selectionChange)="onCategoryChange()">
                <mat-option value="">All Categories</mat-option>
                <mat-option *ngFor="let cat of categories" [value]="cat">{{ cat }}</mat-option>
              </mat-select>
            </mat-form-field>

            <mat-form-field appearance="outline" class="filter-field">
              <mat-label>Show</mat-label>
              <mat-select [(ngModel)]="filterType" (selectionChange)="onFilterChange()">
                <mat-option value="all">All Parameters</mat-option>
                <mat-option value="editable">Editable Only</mat-option>
                <mat-option value="encrypted">Encrypted Only</mat-option>
              </mat-select>
            </mat-form-field>

            <button mat-raised-button (click)="clearFilters()">Clear Filters</button>
          </div>
        </mat-card-content>
      </mat-card>

      <!-- Parameters by Category -->
      <mat-accordion multi *ngIf="!searchKeyword && !selectedCategory">
        <mat-expansion-panel *ngFor="let category of categories" [expanded]="category === 'General'">
          <mat-expansion-panel-header>
            <mat-panel-title>
              {{ category }}
              <mat-chip class="category-count">
                {{ getParametersByCategory(category).length }}
              </mat-chip>
            </mat-panel-title>
          </mat-expansion-panel-header>

          <table mat-table [dataSource]="getParametersByCategory(category)" class="parameters-table">
            <ng-container matColumnDef="key">
              <th mat-header-cell *matHeaderCellDef>Key</th>
              <td mat-cell *matCellDef="let param">
                <code>{{ param.key }}</code>
              </td>
            </ng-container>

            <ng-container matColumnDef="value">
              <th mat-header-cell *matHeaderCellDef>Value</th>
              <td mat-cell *matCellDef="let param">
                <span *ngIf="!param.encrypted">{{ param.value || param.defaultValue || '-' }}</span>
                <span *ngIf="param.encrypted" class="encrypted-value">
                  <mat-icon>lock</mat-icon>
                  ***ENCRYPTED***
                </span>
              </td>
            </ng-container>

            <ng-container matColumnDef="dataType">
              <th mat-header-cell *matHeaderCellDef>Type</th>
              <td mat-cell *matCellDef="let param">
                <mat-chip>{{ param.dataType }}</mat-chip>
              </td>
            </ng-container>

            <ng-container matColumnDef="description">
              <th mat-header-cell *matHeaderCellDef>Description</th>
              <td mat-cell *matCellDef="let param">{{ param.description || '-' }}</td>
            </ng-container>

            <ng-container matColumnDef="flags">
              <th mat-header-cell *matHeaderCellDef>Flags</th>
              <td mat-cell *matCellDef="let param">
                <mat-chip *ngIf="!param.editable" color="warn">Read-Only</mat-chip>
                <mat-chip *ngIf="param.encrypted" color="accent">
                  <mat-icon>lock</mat-icon>
                  Encrypted
                </mat-chip>
              </td>
            </ng-container>

            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef>Actions</th>
              <td mat-cell *matCellDef="let param">
                <button mat-icon-button [matMenuTriggerFor]="menu" [disabled]="!param.editable">
                  <mat-icon>more_vert</mat-icon>
                </button>
                <mat-menu #menu="matMenu">
                  <button mat-menu-item (click)="editValue(param)">
                    <mat-icon>edit</mat-icon>
                    Edit Value
                  </button>
                  <button mat-menu-item (click)="resetParameter(param.id)">
                    <mat-icon>refresh</mat-icon>
                    Reset to Default
                  </button>
                  <button mat-menu-item (click)="viewHistory(param.id)">
                    <mat-icon>history</mat-icon>
                    View History
                  </button>
                </mat-menu>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
          </table>
        </mat-expansion-panel>
      </mat-accordion>

      <!-- Search Results Table -->
      <div class="table-container" *ngIf="searchKeyword || selectedCategory">
        <table mat-table [dataSource]="parameters" class="parameters-table">
          <ng-container matColumnDef="key">
            <th mat-header-cell *matHeaderCellDef>Key</th>
            <td mat-cell *matCellDef="let param">
              <code>{{ param.key }}</code>
            </td>
          </ng-container>

          <ng-container matColumnDef="category">
            <th mat-header-cell *matHeaderCellDef>Category</th>
            <td mat-cell *matCellDef="let param">{{ param.category }}</td>
          </ng-container>

          <ng-container matColumnDef="value">
            <th mat-header-cell *matHeaderCellDef>Value</th>
            <td mat-cell *matCellDef="let param">
              <span *ngIf="!param.encrypted">{{ param.value || param.defaultValue || '-' }}</span>
              <span *ngIf="param.encrypted" class="encrypted-value">
                <mat-icon>lock</mat-icon>
                ***ENCRYPTED***
              </span>
            </td>
          </ng-container>

          <ng-container matColumnDef="dataType">
            <th mat-header-cell *matHeaderCellDef>Type</th>
            <td mat-cell *matCellDef="let param">
              <mat-chip>{{ param.dataType }}</mat-chip>
            </td>
          </ng-container>

          <ng-container matColumnDef="description">
            <th mat-header-cell *matHeaderCellDef>Description</th>
            <td mat-cell *matCellDef="let param">{{ param.description || '-' }}</td>
          </ng-container>

          <ng-container matColumnDef="flags">
            <th mat-header-cell *matHeaderCellDef>Flags</th>
            <td mat-cell *matCellDef="let param">
              <mat-chip *ngIf="!param.editable" color="warn">Read-Only</mat-chip>
              <mat-chip *ngIf="param.encrypted" color="accent">
                <mat-icon>lock</mat-icon>
                Encrypted
              </mat-chip>
            </td>
          </ng-container>

          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Actions</th>
            <td mat-cell *matCellDef="let param">
              <button mat-icon-button [matMenuTriggerFor]="menu" [disabled]="!param.editable">
                <mat-icon>more_vert</mat-icon>
              </button>
              <mat-menu #menu="matMenu">
                <button mat-menu-item (click)="editValue(param)">
                  <mat-icon>edit</mat-icon>
                  Edit Value
                </button>
                <button mat-menu-item (click)="resetParameter(param.id)">
                  <mat-icon>refresh</mat-icon>
                  Reset to Default
                </button>
              </mat-menu>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumnsWithCategory"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumnsWithCategory;"></tr>
        </table>

        <mat-paginator
          [length]="totalElements"
          [pageSize]="pageSize"
          [pageSizeOptions]="[20, 50, 100]"
          (page)="onPageChange($event)"
          showFirstLastButtons>
        </mat-paginator>
      </div>
    </div>
  `,
  styles: [`
    .system-parameters-container {
      padding: 24px;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
    }

    .stats-card {
      margin-bottom: 24px;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(4, 1fr);
      gap: 24px;
    }

    .stat-item {
      text-align: center;
    }

    .stat-label {
      color: #666;
      font-size: 14px;
      margin-bottom: 8px;
    }

    .stat-value {
      font-size: 32px;
      font-weight: 500;
      color: #1976d2;
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

    .category-count {
      margin-left: 12px;
    }

    .table-container {
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .parameters-table {
      width: 100%;
    }

    code {
      background: #f5f5f5;
      padding: 4px 8px;
      border-radius: 4px;
      font-family: 'Courier New', monospace;
      font-size: 12px;
    }

    .encrypted-value {
      display: flex;
      align-items: center;
      gap: 8px;
      color: #ff9800;
    }

    .encrypted-value mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
    }
  `]
})
export class SystemParametersComponent implements OnInit {
  parameters: SystemParameter[] = [];
  allParameters: SystemParameter[] = [];
  categories: string[] = [];
  displayedColumns: string[] = ['key', 'value', 'dataType', 'description', 'flags', 'actions'];
  displayedColumnsWithCategory: string[] = ['key', 'category', 'value', 'dataType', 'description', 'flags', 'actions'];

  pageSize = 20;
  currentPage = 0;
  totalElements = 0;

  searchKeyword = '';
  selectedCategory = '';
  filterType = 'all';

  stats: any = null;

  constructor(
    private adminService: AdminService,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    this.loadCategories();
    this.loadAllParameters();
    this.loadStats();
  }

  loadCategories(): void {
    this.adminService.getAllCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
      }
    });
  }

  loadAllParameters(): void {
    this.adminService.getAllParameters(0, 1000).subscribe({
      next: (response) => {
        this.allParameters = response.content;
      }
    });
  }

  loadStats(): void {
    this.adminService.getAdminStats().subscribe({
      next: (stats) => {
        this.stats = stats;
      }
    });
  }

  loadParameters(): void {
    let observable;

    if (this.searchKeyword) {
      observable = this.adminService.searchParameters(this.searchKeyword, this.currentPage, this.pageSize);
    } else if (this.selectedCategory) {
      // For category filter, we'll use the local data
      this.parameters = this.getParametersByCategory(this.selectedCategory);
      this.totalElements = this.parameters.length;
      return;
    } else {
      observable = this.adminService.getAllParameters(this.currentPage, this.pageSize);
    }

    observable.subscribe({
      next: (response) => {
        this.parameters = response.content;
        this.totalElements = response.totalElements;
      },
      error: () => {
        this.snackBar.open('Failed to load parameters', 'Close', { duration: 3000 });
      }
    });
  }

  getParametersByCategory(category: string): SystemParameter[] {
    return this.allParameters.filter(p => p.category === category);
  }

  onSearch(): void {
    this.currentPage = 0;
    this.loadParameters();
  }

  onCategoryChange(): void {
    this.currentPage = 0;
    this.loadParameters();
  }

  onFilterChange(): void {
    this.currentPage = 0;
    if (this.filterType === 'editable') {
      this.adminService.getEditableParameters().subscribe({
        next: (params) => {
          this.parameters = params;
          this.totalElements = params.length;
        }
      });
    } else {
      this.loadParameters();
    }
  }

  clearFilters(): void {
    this.searchKeyword = '';
    this.selectedCategory = '';
    this.filterType = 'all';
    this.currentPage = 0;
    this.loadAllParameters();
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadParameters();
  }

  openCreateDialog(): void {
    this.snackBar.open('Create parameter dialog - To be implemented', 'Close', { duration: 3000 });
  }

  editValue(param: SystemParameter): void {
    const newValue = prompt(`Edit value for ${param.key}:`, param.value || param.defaultValue || '');
    if (newValue !== null) {
      this.adminService.updateParameterValue(param.id, newValue).subscribe({
        next: () => {
          this.snackBar.open('Parameter updated successfully', 'Close', { duration: 3000 });
          this.loadAllParameters();
        },
        error: () => {
          this.snackBar.open('Failed to update parameter', 'Close', { duration: 3000 });
        }
      });
    }
  }

  resetParameter(id: number): void {
    if (confirm('Reset this parameter to its default value?')) {
      this.adminService.resetToDefault(id).subscribe({
        next: () => {
          this.snackBar.open('Parameter reset to default', 'Close', { duration: 3000 });
          this.loadAllParameters();
        },
        error: () => {
          this.snackBar.open('Failed to reset parameter', 'Close', { duration: 3000 });
        }
      });
    }
  }

  viewHistory(id: number): void {
    this.snackBar.open('Parameter history - To be implemented', 'Close', { duration: 3000 });
  }
}
