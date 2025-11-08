import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatMenuModule } from '@angular/material/menu';
import { MatDividerModule } from '@angular/material/divider';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { FormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged, Subject } from 'rxjs';
import { FormService } from '../../core/services/form.service';

/**
 * Form List Component
 * Lists all available dynamic forms
 */
@Component({
  selector: 'app-form-list',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatTableModule,
    MatPaginatorModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatTooltipModule,
    MatMenuModule,
    MatDividerModule,
    MatSnackBarModule
  ],
  template: `
    <div class="form-list-container">
      <div class="header">
        <h1>Dynamic Forms</h1>
        <button mat-raised-button color="primary" (click)="createForm()">
          <mat-icon>add</mat-icon>
          Create Form
        </button>
      </div>

      <!-- Filters -->
      <mat-card class="filters-card">
        <mat-card-content>
          <div class="filters">
            <mat-form-field appearance="outline" class="search-field">
              <mat-label>Search</mat-label>
              <input matInput
                     [(ngModel)]="searchKeyword"
                     (ngModelChange)="onSearchChange($event)"
                     placeholder="Search by name or key">
              <mat-icon matSuffix>search</mat-icon>
            </mat-form-field>

            <mat-form-field appearance="outline">
              <mat-label>Status</mat-label>
              <mat-select [(ngModel)]="selectedStatus" (selectionChange)="loadForms()">
                <mat-option value="">All</mat-option>
                <mat-option value="ACTIVE">Active</mat-option>
                <mat-option value="DRAFT">Draft</mat-option>
                <mat-option value="ARCHIVED">Archived</mat-option>
              </mat-select>
            </mat-form-field>

            <button mat-raised-button (click)="resetFilters()">
              <mat-icon>clear</mat-icon>
              Reset
            </button>
          </div>
        </mat-card-content>
      </mat-card>

      <!-- Forms Table -->
      <mat-card>
        <mat-card-content>
          <table mat-table [dataSource]="forms" class="forms-table">
            <!-- Name Column -->
            <ng-container matColumnDef="name">
              <th mat-header-cell *matHeaderCellDef>Name</th>
              <td mat-cell *matCellDef="let form">
                <div class="form-name">
                  <strong>{{ form.name }}</strong>
                  <small *ngIf="form.description">{{ form.description }}</small>
                </div>
              </td>
            </ng-container>

            <!-- Key Column -->
            <ng-container matColumnDef="key">
              <th mat-header-cell *matHeaderCellDef>Key</th>
              <td mat-cell *matCellDef="let form">
                <code>{{ form.key }}</code>
              </td>
            </ng-container>

            <!-- Fields Column -->
            <ng-container matColumnDef="fields">
              <th mat-header-cell *matHeaderCellDef>Fields</th>
              <td mat-cell *matCellDef="let form">
                <mat-chip>
                  <mat-icon>text_fields</mat-icon>
                  {{ form.fields?.length || 0 }} fields
                </mat-chip>
              </td>
            </ng-container>

            <!-- Status Column -->
            <ng-container matColumnDef="status">
              <th mat-header-cell *matHeaderCellDef>Status</th>
              <td mat-cell *matCellDef="let form">
                <mat-chip [ngClass]="getStatusClass(form.status)">
                  {{ form.status || 'DRAFT' }}
                </mat-chip>
              </td>
            </ng-container>

            <!-- Version Column -->
            <ng-container matColumnDef="version">
              <th mat-header-cell *matHeaderCellDef>Version</th>
              <td mat-cell *matCellDef="let form">
                v{{ form.version || 1 }}
              </td>
            </ng-container>

            <!-- Created Column -->
            <ng-container matColumnDef="created">
              <th mat-header-cell *matHeaderCellDef>Created</th>
              <td mat-cell *matCellDef="let form">
                {{ form.createdAt | date:'short' }}
              </td>
            </ng-container>

            <!-- Actions Column -->
            <ng-container matColumnDef="actions">
              <th mat-header-cell *matHeaderCellDef>Actions</th>
              <td mat-cell *matCellDef="let form">
                <button mat-icon-button [matMenuTriggerFor]="menu" matTooltip="Actions">
                  <mat-icon>more_vert</mat-icon>
                </button>
                <mat-menu #menu="matMenu">
                  <button mat-menu-item (click)="viewForm(form.id)">
                    <mat-icon>visibility</mat-icon>
                    <span>View</span>
                  </button>
                  <button mat-menu-item (click)="editForm(form.id)">
                    <mat-icon>edit</mat-icon>
                    <span>Edit</span>
                  </button>
                  <button mat-menu-item (click)="duplicateForm(form.id)">
                    <mat-icon>content_copy</mat-icon>
                    <span>Duplicate</span>
                  </button>
                  <button mat-menu-item (click)="downloadForm(form.id)">
                    <mat-icon>download</mat-icon>
                    <span>Export JSON</span>
                  </button>
                  <mat-divider></mat-divider>
                  <button mat-menu-item (click)="deleteForm(form.id)" class="delete-action">
                    <mat-icon color="warn">delete</mat-icon>
                    <span>Delete</span>
                  </button>
                </mat-menu>
              </td>
            </ng-container>

            <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
            <tr mat-row *matRowDef="let row; columns: displayedColumns;" class="form-row"></tr>
          </table>

          <!-- Empty State -->
          <div class="empty-state" *ngIf="forms.length === 0 && !isLoading">
            <mat-icon>description</mat-icon>
            <h3>No forms found</h3>
            <p *ngIf="searchKeyword || selectedStatus">Try adjusting your filters</p>
            <p *ngIf="!searchKeyword && !selectedStatus">Create your first form to get started</p>
            <button mat-raised-button color="primary" (click)="createForm()">
              <mat-icon>add</mat-icon>
              Create Form
            </button>
          </div>

          <!-- Pagination -->
          <mat-paginator
            *ngIf="forms.length > 0"
            [length]="totalItems"
            [pageSize]="pageSize"
            [pageSizeOptions]="[10, 20, 50, 100]"
            (page)="onPageChange($event)">
          </mat-paginator>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .form-list-container {
      padding: 24px;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
    }

    .header h1 {
      margin: 0;
    }

    .header button {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .filters-card {
      margin-bottom: 16px;
    }

    .filters {
      display: flex;
      gap: 16px;
      align-items: center;
      flex-wrap: wrap;
    }

    .search-field {
      flex: 1;
      min-width: 300px;
    }

    .filters button {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .forms-table {
      width: 100%;
    }

    .form-name {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .form-name small {
      color: #666;
      font-size: 12px;
    }

    code {
      background: #f5f5f5;
      padding: 4px 8px;
      border-radius: 4px;
      font-size: 12px;
      font-family: 'Courier New', monospace;
    }

    mat-chip {
      cursor: default;
    }

    mat-chip mat-icon {
      font-size: 18px;
      width: 18px;
      height: 18px;
      margin-right: 4px;
    }

    .status-active {
      background: #c8e6c9 !important;
      color: #2e7d32;
    }

    .status-draft {
      background: #fff3e0 !important;
      color: #e65100;
    }

    .status-archived {
      background: #e0e0e0 !important;
      color: #616161;
    }

    .form-row {
      cursor: pointer;
      transition: background-color 0.2s;
    }

    .form-row:hover {
      background-color: #f5f5f5;
    }

    .delete-action {
      color: #f44336;
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 64px 24px;
      text-align: center;
      color: #999;
    }

    .empty-state mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      margin-bottom: 16px;
    }

    .empty-state h3 {
      margin: 0 0 8px 0;
      color: #666;
    }

    .empty-state p {
      margin: 0 0 24px 0;
      font-size: 14px;
    }

    .empty-state button {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  `]
})
export class FormListComponent implements OnInit {
  forms: any[] = [];
  displayedColumns: string[] = ['name', 'key', 'fields', 'status', 'version', 'created', 'actions'];

  searchKeyword = '';
  selectedStatus = '';
  currentPage = 0;
  pageSize = 20;
  totalItems = 0;
  isLoading = false;

  private searchSubject = new Subject<string>();

  constructor(
    private formService: FormService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    // Setup search debounce
    this.searchSubject.pipe(
      debounceTime(300),
      distinctUntilChanged()
    ).subscribe(keyword => {
      this.searchKeyword = keyword;
      this.currentPage = 0;
      this.loadForms();
    });
  }

  ngOnInit(): void {
    this.loadForms();
  }

  loadForms(): void {
    this.isLoading = true;

    this.formService.getAllForms(this.currentPage, this.pageSize).subscribe({
      next: (response) => {
        this.forms = response.content || [];
        this.totalItems = response.totalElements || 0;
        this.isLoading = false;
      },
      error: () => {
        this.isLoading = false;
        this.loadMockForms();
      }
    });
  }

  loadMockForms(): void {
    this.forms = [
      {
        id: 1,
        name: 'Customer Onboarding Form',
        key: 'customer-onboarding',
        description: 'New customer registration and KYC',
        fields: [{}, {}, {}, {}, {}],
        status: 'ACTIVE',
        version: 2,
        createdAt: new Date()
      },
      {
        id: 2,
        name: 'Loan Application',
        key: 'loan-application',
        description: 'Personal loan application form',
        fields: [{}, {}, {}, {}, {}, {}, {}],
        status: 'ACTIVE',
        version: 1,
        createdAt: new Date(Date.now() - 86400000)
      },
      {
        id: 3,
        name: 'Account Opening Form',
        key: 'account-opening',
        description: null,
        fields: [{}, {}, {}],
        status: 'DRAFT',
        version: 1,
        createdAt: new Date(Date.now() - 172800000)
      }
    ];
    this.totalItems = 3;
  }

  onSearchChange(keyword: string): void {
    this.searchSubject.next(keyword);
  }

  onPageChange(event: PageEvent): void {
    this.currentPage = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadForms();
  }

  resetFilters(): void {
    this.searchKeyword = '';
    this.selectedStatus = '';
    this.currentPage = 0;
    this.loadForms();
  }

  getStatusClass(status: string): string {
    return `status-${(status || 'draft').toLowerCase()}`;
  }

  createForm(): void {
    this.router.navigate(['/forms/create']);
  }

  viewForm(id: number): void {
    this.router.navigate(['/forms', id, 'view']);
  }

  editForm(id: number): void {
    this.router.navigate(['/forms', id, 'edit']);
  }

  duplicateForm(id: number): void {
    if (confirm('Create a copy of this form?')) {
      this.formService.duplicateForm(id).subscribe({
        next: () => {
          this.snackBar.open('Form duplicated successfully', 'Close', { duration: 3000 });
          this.loadForms();
        },
        error: () => {
          this.snackBar.open('Failed to duplicate form', 'Close', { duration: 3000 });
        }
      });
    }
  }

  downloadForm(id: number): void {
    this.formService.getFormById(id).subscribe({
      next: (form) => {
        const json = JSON.stringify(form, null, 2);
        const blob = new Blob([json], { type: 'application/json' });
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `${form.key}.json`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.snackBar.open('Form exported', 'Close', { duration: 2000 });
      },
      error: () => {
        this.snackBar.open('Failed to export form', 'Close', { duration: 3000 });
      }
    });
  }

  deleteForm(id: number): void {
    if (confirm('Are you sure you want to delete this form? This action cannot be undone.')) {
      this.formService.deleteForm(id).subscribe({
        next: () => {
          this.snackBar.open('Form deleted successfully', 'Close', { duration: 3000 });
          this.loadForms();
        },
        error: () => {
          this.snackBar.open('Failed to delete form', 'Close', { duration: 3000 });
        }
      });
    }
  }
}
