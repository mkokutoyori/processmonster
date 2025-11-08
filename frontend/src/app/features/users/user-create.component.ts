import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSelectModule } from '@angular/material/select';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { UserService } from '../../core/services/user.service';

/**
 * User Creation Page
 */
@Component({
  selector: 'app-user-create',
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
    MatSnackBarModule
  ],
  template: `
    <div class="user-create-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <mat-icon>person_add</mat-icon>
            Create New User
          </mat-card-title>
          <mat-card-subtitle>Add a new user to the system</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <form [formGroup]="userForm" (ngSubmit)="onSubmit()">
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

              <!-- Password -->
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Password</mat-label>
                <input matInput [type]="hidePassword ? 'password' : 'text'"
                       formControlName="password" required>
                <button mat-icon-button matSuffix (click)="hidePassword = !hidePassword"
                        type="button">
                  <mat-icon>{{ hidePassword ? 'visibility_off' : 'visibility' }}</mat-icon>
                </button>
                <mat-error *ngIf="userForm.get('password')?.hasError('required')">
                  Password is required
                </mat-error>
                <mat-error *ngIf="userForm.get('password')?.hasError('minlength')">
                  Password must be at least 8 characters
                </mat-error>
                <mat-hint>At least 8 characters with uppercase, lowercase, digit and special character</mat-hint>
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
                {{ isLoading ? 'Creating...' : 'Create User' }}
              </button>

              <button mat-button (click)="onCancel()" type="button">
                <mat-icon>cancel</mat-icon>
                Cancel
              </button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .user-create-container {
      padding: 24px;
      max-width: 900px;
      margin: 0 auto;
    }

    mat-card-title {
      display: flex;
      align-items: center;
      gap: 8px;
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
  `]
})
export class UserCreateComponent implements OnInit {
  userForm: FormGroup;
  hidePassword = true;
  isLoading = false;

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
    private snackBar: MatSnackBar
  ) {
    this.userForm = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
      firstname: ['', Validators.required],
      lastname: ['', Validators.required],
      phone: [''],
      password: ['', [Validators.required, Validators.minLength(8)]],
      roleIds: [[], Validators.required]
    });
  }

  ngOnInit(): void {
    // Set default role to USER
    this.userForm.patchValue({ roleIds: [3] });
  }

  getRoleName(roleId: number): string {
    return this.roleNames[roleId] || `Role ${roleId}`;
  }

  onSubmit(): void {
    if (this.userForm.valid) {
      this.isLoading = true;

      const createUserRequest = this.userForm.value;

      this.userService.createUser(createUserRequest).subscribe({
        next: (user) => {
          this.snackBar.open('User created successfully!', 'Close', { duration: 3000 });
          this.router.navigate(['/users']);
        },
        error: (error) => {
          this.isLoading = false;
          const errorMessage = error.error?.message || 'Failed to create user. Please try again.';
          this.snackBar.open(errorMessage, 'Close', { duration: 5000 });
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/users']);
  }
}
