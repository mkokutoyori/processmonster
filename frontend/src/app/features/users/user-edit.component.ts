import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { UserService } from '../../core/services/user.service';
import { User } from '../../core/models/user.model';

/**
 * User Edit Page
 */
@Component({
  selector: 'app-user-edit',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSelectModule,
    MatChipsModule,
    MatSnackBarModule,
    MatProgressSpinnerModule
  ],
  template: `
    <div class="user-edit-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <mat-icon>edit</mat-icon>
            Edit User
          </mat-card-title>
          <mat-card-subtitle>Update user information</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <div class="loading-container" *ngIf="isLoadingUser">
            <mat-spinner diameter="50"></mat-spinner>
            <p>Loading user data...</p>
          </div>

          <form [formGroup]="userForm" (ngSubmit)="onSubmit()" *ngIf="!isLoadingUser">
            <div class="form-grid">
              <!-- Username -->
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Username</mat-label>
                <input matInput formControlName="username" required>
                <mat-icon matSuffix>person</mat-icon>
                <mat-error *ngIf="userForm.get('username')?.hasError('required')">
                  Username is required
                </mat-error>
                <mat-error *ngIf="userForm.get('username')?.hasError('minlength')">
                  Minimum 3 characters
                </mat-error>
              </mat-form-field>

              <!-- Email -->
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Email</mat-label>
                <input matInput type="email" formControlName="email" required>
                <mat-icon matSuffix>email</mat-icon>
                <mat-error *ngIf="userForm.get('email')?.hasError('required')">
                  Email is required
                </mat-error>
                <mat-error *ngIf="userForm.get('email')?.hasError('email')">
                  Invalid email format
                </mat-error>
              </mat-form-field>

              <!-- First Name -->
              <mat-form-field appearance="outline">
                <mat-label>First Name</mat-label>
                <input matInput formControlName="firstname" required>
                <mat-error *ngIf="userForm.get('firstname')?.hasError('required')">
                  First name is required
                </mat-error>
              </mat-form-field>

              <!-- Last Name -->
              <mat-form-field appearance="outline">
                <mat-label>Last Name</mat-label>
                <input matInput formControlName="lastname" required>
                <mat-error *ngIf="userForm.get('lastname')?.hasError('required')">
                  Last name is required
                </mat-error>
              </mat-form-field>

              <!-- Phone -->
              <mat-form-field appearance="outline">
                <mat-label>Phone (Optional)</mat-label>
                <input matInput formControlName="phone">
                <mat-icon matSuffix>phone</mat-icon>
              </mat-form-field>

              <!-- Role -->
              <mat-form-field appearance="outline">
                <mat-label>Role</mat-label>
                <mat-select formControlName="roleIds" multiple required>
                  <mat-option [value]="1">Administrator</mat-option>
                  <mat-option [value]="2">Manager</mat-option>
                  <mat-option [value]="3">User</mat-option>
                  <mat-option [value]="4">Analyst</mat-option>
                  <mat-option [value]="5">Auditor</mat-option>
                </mat-select>
                <mat-error *ngIf="userForm.get('roleIds')?.hasError('required')">
                  At least one role is required
                </mat-error>
              </mat-form-field>

              <!-- Active Status -->
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Status</mat-label>
                <mat-select formControlName="active" required>
                  <mat-option [value]="true">Active</mat-option>
                  <mat-option [value]="false">Inactive</mat-option>
                </mat-select>
                <mat-hint>Inactive users cannot log in</mat-hint>
              </mat-form-field>

              <!-- Selected Roles Display -->
              <div class="selected-roles full-width" *ngIf="userForm.get('roleIds')?.value?.length">
                <label>Selected Roles:</label>
                <mat-chip-set>
                  <mat-chip *ngFor="let roleId of userForm.get('roleIds')?.value">
                    {{ getRoleName(roleId) }}
                  </mat-chip>
                </mat-chip-set>
              </div>
            </div>

            <div class="form-actions">
              <button mat-raised-button color="primary" type="submit"
                      [disabled]="userForm.invalid || isLoading">
                <mat-icon>save</mat-icon>
                {{ isLoading ? 'Updating...' : 'Update User' }}
              </button>

              <button mat-button (click)="onCancel()" type="button">
                <mat-icon>cancel</mat-icon>
                Cancel
              </button>

              <button mat-stroked-button color="warn" (click)="onDelete()" type="button"
                      class="delete-button">
                <mat-icon>delete</mat-icon>
                Delete User
              </button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .user-edit-container {
      padding: 24px;
      max-width: 900px;
      margin: 0 auto;
    }

    mat-card-title {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .loading-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 48px;
      gap: 16px;
    }

    .form-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 16px;
      margin-top: 24px;
    }

    .full-width {
      grid-column: 1 / -1;
    }

    .selected-roles {
      padding: 16px;
      background: #f5f5f5;
      border-radius: 4px;
    }

    .selected-roles label {
      display: block;
      margin-bottom: 8px;
      color: #666;
      font-size: 14px;
      font-weight: 500;
    }

    .form-actions {
      display: flex;
      gap: 12px;
      margin-top: 32px;
      padding-top: 24px;
      border-top: 1px solid #e0e0e0;
    }

    .form-actions button {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .delete-button {
      margin-left: auto;
    }
  `]
})
export class UserEditComponent implements OnInit {
  userForm: FormGroup;
  userId: number = 0;
  isLoading = false;
  isLoadingUser = true;

  roleNames: { [key: number]: string } = {
    1: 'Administrator',
    2: 'Manager',
    3: 'User',
    4: 'Analyst',
    5: 'Auditor'
  };

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private router: Router,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar
  ) {
    this.userForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      firstname: ['', Validators.required],
      lastname: ['', Validators.required],
      phone: [''],
      roleIds: [[], Validators.required],
      active: [true, Validators.required]
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      this.userId = +params['id'];
      if (this.userId) {
        this.loadUser();
      } else {
        this.snackBar.open('Invalid user ID', 'Close', { duration: 3000 });
        this.router.navigate(['/users']);
      }
    });
  }

  loadUser(): void {
    this.isLoadingUser = true;
    this.userService.getUserById(this.userId).subscribe({
      next: (user: User) => {
        this.userForm.patchValue({
          username: user.username,
          email: user.email,
          firstname: user.firstname,
          lastname: user.lastname,
          phone: user.phone || '',
          roleIds: user.roles?.map(role => role.id) || [],
          active: user.active !== false
        });
        this.isLoadingUser = false;
      },
      error: (error) => {
        this.isLoadingUser = false;
        const errorMessage = error.error?.message || 'Failed to load user data';
        this.snackBar.open(errorMessage, 'Close', { duration: 5000 });
        this.router.navigate(['/users']);
      }
    });
  }

  getRoleName(roleId: number): string {
    return this.roleNames[roleId] || `Role ${roleId}`;
  }

  onSubmit(): void {
    if (this.userForm.valid) {
      this.isLoading = true;

      const updateUserRequest = {
        ...this.userForm.value,
        id: this.userId
      };

      this.userService.updateUser(this.userId, updateUserRequest).subscribe({
        next: (user) => {
          this.snackBar.open('User updated successfully!', 'Close', { duration: 3000 });
          this.router.navigate(['/users']);
        },
        error: (error) => {
          this.isLoading = false;
          const errorMessage = error.error?.message || 'Failed to update user. Please try again.';
          this.snackBar.open(errorMessage, 'Close', { duration: 5000 });
        }
      });
    }
  }

  onDelete(): void {
    if (confirm(`Are you sure you want to delete this user? This action cannot be undone.`)) {
      this.userService.deleteUser(this.userId).subscribe({
        next: () => {
          this.snackBar.open('User deleted successfully', 'Close', { duration: 3000 });
          this.router.navigate(['/users']);
        },
        error: (error) => {
          const errorMessage = error.error?.message || 'Failed to delete user';
          this.snackBar.open(errorMessage, 'Close', { duration: 5000 });
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/users']);
  }
}
