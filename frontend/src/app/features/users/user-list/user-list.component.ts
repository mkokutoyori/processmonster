/**
 * User List Component
 *
 * Displays a paginated table of users with search, filter, and actions.
 */
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormControl, ReactiveFormsModule } from '@angular/forms';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatChipsModule } from '@angular/material/chips';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatDialogModule, MatDialog } from '@angular/material/dialog';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ToastrService } from 'ngx-toastr';
import { debounceTime, distinctUntilChanged } from 'rxjs';
import { UserService } from '../../../core/services/user.service';
import { User, PagedResponse } from '../../../core/models/user.model';

@Component({
  selector: 'app-user-list',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatTableModule,
    MatPaginatorModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatChipsModule,
    MatTooltipModule,
    MatDialogModule,
    TranslateModule
  ],
  template: `
    <div class="user-list-container">
      <div class="header">
        <h1>{{ 'user.list' | translate }}</h1>
        <button mat-raised-button color="primary" (click)="createUser()">
          <mat-icon>add</mat-icon>
          {{ 'user.create' | translate }}
        </button>
      </div>

      <!-- Search Bar -->
      <mat-form-field appearance="outline" class="search-field">
        <mat-label>{{ 'user.search' | translate }}</mat-label>
        <input matInput [formControl]="searchControl" [placeholder]="'user.search' | translate">
        <mat-icon matPrefix>search</mat-icon>
      </mat-form-field>

      <!-- Users Table -->
      <div class="table-container">
        <table mat-table [dataSource]="users" class="users-table">
          <!-- Username Column -->
          <ng-container matColumnDef="username">
            <th mat-header-cell *matHeaderCellDef>{{ 'auth.username' | translate }}</th>
            <td mat-cell *matCellDef="let user">{{ user.username }}</td>
          </ng-container>

          <!-- Email Column -->
          <ng-container matColumnDef="email">
            <th mat-header-cell *matHeaderCellDef>{{ 'auth.email' | translate }}</th>
            <td mat-cell *matCellDef="let user">{{ user.email }}</td>
          </ng-container>

          <!-- Name Column -->
          <ng-container matColumnDef="name">
            <th mat-header-cell *matHeaderCellDef>{{ 'common.name' | translate }}</th>
            <td mat-cell *matCellDef="let user">
              {{ user.firstName || '' }} {{ user.lastName || '' }}
            </td>
          </ng-container>

          <!-- Roles Column -->
          <ng-container matColumnDef="roles">
            <th mat-header-cell *matHeaderCellDef>{{ 'user.roles' | translate }}</th>
            <td mat-cell *matCellDef="let user">
              <mat-chip-set>
                @for (role of user.roles; track role) {
                  <mat-chip>{{ role }}</mat-chip>
                }
              </mat-chip-set>
            </td>
          </ng-container>

          <!-- Status Column -->
          <ng-container matColumnDef="status">
            <th mat-header-cell *matHeaderCellDef>{{ 'common.status' | translate }}</th>
            <td mat-cell *matCellDef="let user">
              <span [class]="user.enabled ? 'status-active' : 'status-inactive'">
                {{ (user.enabled ? 'common.active' : 'common.inactive') | translate }}
              </span>
            </td>
          </ng-container>

          <!-- Actions Column -->
          <ng-container matColumnDef="actions">
            <th mat-header-cell *matHeaderCellDef>{{ 'common.actions' | translate }}</th>
            <td mat-cell *matCellDef="let user">
              <button mat-icon-button [matTooltip]="'common.view' | translate" (click)="viewUser(user.id)">
                <mat-icon>visibility</mat-icon>
              </button>
              <button mat-icon-button [matTooltip]="'common.edit' | translate" (click)="editUser(user.id)">
                <mat-icon>edit</mat-icon>
              </button>
              @if (user.enabled) {
                <button mat-icon-button [matTooltip]="'user.deactivate' | translate" (click)="deactivateUser(user)">
                  <mat-icon>block</mat-icon>
                </button>
              } @else {
                <button mat-icon-button [matTooltip]="'user.activate' | translate" (click)="activateUser(user)">
                  <mat-icon>check_circle</mat-icon>
                </button>
              }
              <button mat-icon-button color="warn" [matTooltip]="'common.delete' | translate" (click)="deleteUser(user)">
                <mat-icon>delete</mat-icon>
              </button>
            </td>
          </ng-container>

          <tr mat-header-row *matHeaderRowDef="displayedColumns"></tr>
          <tr mat-row *matRowDef="let row; columns: displayedColumns;"></tr>
        </table>
      </div>

      <!-- Paginator -->
      <mat-paginator
        [length]="totalElements"
        [pageSize]="pageSize"
        [pageIndex]="pageIndex"
        [pageSizeOptions]="[10, 20, 50, 100]"
        (page)="onPageChange($event)"
        [showFirstLastButtons]="true">
      </mat-paginator>
    </div>
  `,
  styles: [`
    .user-list-container {
      padding: 2rem;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 2rem;
    }

    .header h1 {
      margin: 0;
    }

    .search-field {
      width: 100%;
      max-width: 400px;
      margin-bottom: 1rem;
    }

    .table-container {
      overflow-x: auto;
      margin-bottom: 1rem;
    }

    .users-table {
      width: 100%;
    }

    .status-active {
      color: #4caf50;
      font-weight: 500;
    }

    .status-inactive {
      color: #f44336;
      font-weight: 500;
    }

    mat-chip-set {
      gap: 0.5rem;
    }

    @media (max-width: 768px) {
      .user-list-container {
        padding: 1rem;
      }

      .header {
        flex-direction: column;
        align-items: flex-start;
        gap: 1rem;
      }
    }
  `]
})
export class UserListComponent implements OnInit {
  private userService = inject(UserService);
  private router = inject(Router);
  private translate = inject(TranslateService);
  private toastr = inject(ToastrService);
  private dialog = inject(MatDialog);

  users: User[] = [];
  displayedColumns = ['username', 'email', 'name', 'roles', 'status', 'actions'];

  totalElements = 0;
  pageSize = 20;
  pageIndex = 0;

  searchControl = new FormControl('');

  ngOnInit(): void {
    this.loadUsers();
    this.setupSearch();
  }

  /**
   * Setup search with debounce
   */
  private setupSearch(): void {
    this.searchControl.valueChanges
      .pipe(
        debounceTime(300),
        distinctUntilChanged()
      )
      .subscribe(keyword => {
        if (keyword && keyword.trim()) {
          this.searchUsers(keyword.trim());
        } else {
          this.loadUsers();
        }
      });
  }

  /**
   * Load users from API
   */
  private loadUsers(): void {
    this.userService.getUsers(this.pageIndex, this.pageSize).subscribe({
      next: (response) => {
        this.users = response.content;
        this.totalElements = response.totalElements;
      },
      error: (error) => {
        console.error('Error loading users:', error);
      }
    });
  }

  /**
   * Search users
   */
  private searchUsers(keyword: string): void {
    this.userService.searchUsers(keyword, this.pageIndex, this.pageSize).subscribe({
      next: (response) => {
        this.users = response.content;
        this.totalElements = response.totalElements;
      }
    });
  }

  /**
   * Handle page change
   */
  onPageChange(event: PageEvent): void {
    this.pageIndex = event.pageIndex;
    this.pageSize = event.pageSize;
    this.loadUsers();
  }

  /**
   * Create new user
   */
  createUser(): void {
    this.router.navigate(['/users/create']);
  }

  /**
   * View user details
   */
  viewUser(id: number): void {
    this.router.navigate(['/users', id]);
  }

  /**
   * Edit user
   */
  editUser(id: number): void {
    this.router.navigate(['/users', id, 'edit']);
  }

  /**
   * Activate user
   */
  activateUser(user: User): void {
    this.userService.activateUser(user.id).subscribe({
      next: () => {
        this.toastr.success(
          this.translate.instant('user.activated'),
          this.translate.instant('common.success')
        );
        this.loadUsers();
      }
    });
  }

  /**
   * Deactivate user
   */
  deactivateUser(user: User): void {
    if (confirm(this.translate.instant('confirm.action'))) {
      this.userService.deactivateUser(user.id).subscribe({
        next: () => {
          this.toastr.success(
            this.translate.instant('user.deactivated'),
            this.translate.instant('common.success')
          );
          this.loadUsers();
        }
      });
    }
  }

  /**
   * Delete user
   */
  deleteUser(user: User): void {
    const message = this.translate.instant('confirm.delete', { entity: user.username });
    if (confirm(message)) {
      this.userService.deleteUser(user.id).subscribe({
        next: () => {
          this.toastr.success(
            this.translate.instant('user.deleted'),
            this.translate.instant('common.success')
          );
          this.loadUsers();
        }
      });
    }
  }
}
