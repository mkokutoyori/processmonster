import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';
import { ProcessDefinitionService } from '../../core/services/process-definition.service';
import { ProcessCategoryService } from '../../core/services/process-category.service';
import { ProcessInstanceService } from '../../core/services/process-instance.service';
import { NotificationService } from '../../core/services/notification.service';
import { ProcessDefinition, ProcessCategory } from '../../core/models/process.model';
import { ProcessStartDialogComponent, ProcessStartResult } from './process-start-dialog.component';

/**
 * Component for listing process definitions
 */
@Component({
  selector: 'app-process-list',
  standalone: true,
  imports: [
    CommonModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatDialogModule,
    ReactiveFormsModule
  ],
  template: `
    <div class="process-list-container">
      <div class="header">
        <h1>Process Definitions</h1>
        <button mat-raised-button color="primary" (click)="createProcess()">
          <mat-icon>add</mat-icon>
          New Process
        </button>
      </div>

      <div class="filters">
        <mat-form-field appearance="outline" class="search-field">
          <mat-label>Search</mat-label>
          <input matInput [formControl]="searchControl" placeholder="Search by name, key or tags">
          <mat-icon matSuffix>search</mat-icon>
        </mat-form-field>

        <mat-form-field appearance="outline" class="category-field">
          <mat-label>Category</mat-label>
          <mat-select [formControl]="categoryControl">
            <mat-option [value]="null">All Categories</mat-option>
            <mat-option *ngFor="let category of categories" [value]="category.id">
              {{ category.name }}
            </mat-option>
          </mat-select>
        </mat-form-field>
      </div>

      <div class="table-container">
        <table mat-table [dataSource]="processes" class="process-table">
          <!-- Name Column -->
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>Name</th>
            <td mat-cell *matCellDef="let process">
              <div class="process-name">
                {{ process.name }}
                <mat-chip *ngIf="process.isTemplate" class="template-chip">Template</mat-chip>
              </div>
            </td>
          </ng-container>

          <!-- Process Key Column -->
          <ng-container matColumnDef="processKey">
            <th mat-header-cell *matHeaderCellDef>Process Key</th>
            <td mat-cell *matCellDef="let process">
              <code>{{ process.processKey }}</code>
            </td>
          </ng-container>

          <!-- Version Column -->
          <ng-container matColumnDef="version">
            <th mat-header-cell *matHeaderCellDef>Version</th>
            <td mat-cell *matCellDef="let process">
              v{{ process.version }}
              <mat-chip *ngIf="process.isLatestVersion" class="latest-chip">Latest</mat-chip>
            </td>
          </ng-container>

          <!-- Category Column -->
          <ng-container matColumnDef="category">
            <th mat-header-cell *matHeaderCellDef>Category</th>
            <td mat-cell *matCellDef="let process">
              <mat-chip *ngIf="process.category" [style.background-color]="process.category.color">
                {{ process.category.name }}
              </mat-chip>
            </td>
          </ng-container>

          <!-- Status Column -->
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>Status</th>
            <td mat-cell *matCellDef="let process">
              <mat-chip [class.published]="process.published" [class.draft]="!process.published">
                {{ process.published ? 'Published' : 'Draft' }}
              </mat-chip>
              <mat-chip *ngIf="process.deployed" class="deployed-chip">Deployed</mat-chip>
            </td>
          </ng-container>

          <!-- Actions Column -->
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>Actions</th>
            <td mat-cell *matCellDef="let process">
              <button mat-icon-button
                      *ngIf="process.published && process.active"
                      (click)="startProcess(process)"
                      matTooltip="Start Process"
                      color="primary">
                <mat-icon>play_arrow</mat-icon>
              </button>
              <button mat-icon-button (click)="viewProcess(process)" matTooltip="View">
                <mat-icon>visibility</mat-icon>
              </button>
              <button mat-icon-button (click)="editProcess(process)" matTooltip="Edit">
                <mat-icon>edit</mat-icon>
              </button>
              <button mat-icon-button (click)="downloadBpmn(process)" matTooltip="Download BPMN">
                <mat-icon>download</mat-icon>
              </button>
              <button mat-icon-button (click)="deleteProcess(process)" matTooltip="Delete" color="warn">
                <mat-icon>delete</mat-icon>
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
    .process-list-container {
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
    }

    .search-field {
      flex: 1;
    }

    .category-field {
      min-width: 200px;
    }

    .table-container {
      background: white;
      border-radius: 8px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.1);
    }

    .process-table {
      width: 100%;
    }

    .process-name {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    code {
      background: #f5f5f5;
      padding: 2px 6px;
      border-radius: 4px;
      font-family: 'Courier New', monospace;
    }

    .template-chip {
      background-color: #9c27b0 !important;
      color: white !important;
      font-size: 11px;
      height: 20px;
    }

    .latest-chip {
      background-color: #4caf50 !important;
      color: white !important;
      font-size: 11px;
      height: 20px;
      margin-left: 4px;
    }

    .published {
      background-color: #4caf50 !important;
      color: white !important;
    }

    .draft {
      background-color: #ff9800 !important;
      color: white !important;
    }

    .deployed-chip {
      background-color: #2196f3 !important;
      color: white !important;
      margin-left: 4px;
    }
  `]
})
export class ProcessListComponent implements OnInit {
  processes: ProcessDefinition[] = [];
  categories: ProcessCategory[] = [];
  displayedColumns: string[] = ['name', 'processKey', 'version', 'category', 'status', 'actions'];

  totalElements = 0;
  pageSize = 20;
  pageNumber = 0;

  searchControl = new FormControl('');
  categoryControl = new FormControl<number | null>(null);

  constructor(
    private processService: ProcessDefinitionService,
    private categoryService: ProcessCategoryService,
    private instanceService: ProcessInstanceService,
    private router: Router,
    private dialog: MatDialog,
    private notificationService: NotificationService
  ) {}

  ngOnInit(): void {
    this.loadProcesses();
    this.loadCategories();
    this.setupSearch();
    this.setupCategoryFilter();
  }

  loadProcesses(): void {
    this.processService.getProcesses(this.pageNumber, this.pageSize).subscribe({
      next: (response) => {
        this.processes = response.content;
        this.totalElements = response.totalElements;
      },
      error: (error) => {
        console.error('Error loading processes:', error);
      }
    });
  }

  loadCategories(): void {
    this.categoryService.getActiveCategories().subscribe({
      next: (categories) => {
        this.categories = categories;
      },
      error: (error) => {
        console.error('Error loading categories:', error);
      }
    });
  }

  setupSearch(): void {
    this.searchControl.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged()
      )
      .subscribe((keyword) => {
        if (keyword && keyword.trim()) {
          this.searchProcesses(keyword.trim());
        } else {
          this.loadProcesses();
        }
      });
  }

  setupCategoryFilter(): void {
    this.categoryControl.valueChanges.subscribe((categoryId) => {
      if (categoryId) {
        this.filterByCategory(categoryId);
      } else {
        this.loadProcesses();
      }
    });
  }

  searchProcesses(keyword: string): void {
    this.processService.searchProcesses(keyword, this.pageNumber, this.pageSize).subscribe({
      next: (response) => {
        this.processes = response.content;
        this.totalElements = response.totalElements;
      },
      error: (error) => {
        console.error('Error searching processes:', error);
      }
    });
  }

  filterByCategory(categoryId: number): void {
    this.processService.getProcessesByCategory(categoryId, this.pageNumber, this.pageSize).subscribe({
      next: (response) => {
        this.processes = response.content;
        this.totalElements = response.totalElements;
      },
      error: (error) => {
        console.error('Error filtering by category:', error);
      }
    });
  }

  onPageChange(event: PageEvent): void {
    this.pageNumber = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadProcesses();
  }

  createProcess(): void {
    this.router.navigate(['/processes/create']);
  }

  viewProcess(process: ProcessDefinition): void {
    this.router.navigate(['/processes', process.id]);
  }

  editProcess(process: ProcessDefinition): void {
    this.router.navigate(['/processes', process.id, 'edit']);
  }

  downloadBpmn(process: ProcessDefinition): void {
    this.processService.downloadBpmnXml(process.id, process.processKey);
  }

  startProcess(process: ProcessDefinition): void {
    const dialogRef = this.dialog.open(ProcessStartDialogComponent, {
      width: '700px',
      data: { process }
    });

    dialogRef.afterClosed().subscribe((result: ProcessStartResult | null) => {
      if (result) {
        const request = {
          processDefinitionId: process.id,
          businessKey: result.businessKey,
          variables: result.variables
        };

        this.instanceService.startProcess(request).subscribe({
          next: (instance) => {
            this.notificationService.success(`Process instance #${instance.id} started successfully!`);
            this.router.navigate(['/instances', instance.id]);
          },
          error: (error) => {
            console.error('Error starting process:', error);
            this.notificationService.error(error.error?.message || 'Failed to start process');
          }
        });
      }
    });
  }

  deleteProcess(process: ProcessDefinition): void {
    if (confirm(`Are you sure you want to delete process "${process.name}"?`)) {
      this.processService.deleteProcess(process.id).subscribe({
        next: () => {
          this.loadProcesses();
        },
        error: (error) => {
          console.error('Error deleting process:', error);
        }
      });
    }
  }
}
