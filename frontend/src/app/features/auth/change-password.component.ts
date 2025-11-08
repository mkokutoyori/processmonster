import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { UserService } from '../../core/services/user.service';
import { AuthService } from '../../core/services/auth.service';

/**
 * Change Password Page
 */
@Component({
  selector: 'app-change-password',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule
  ],
  template: `
    <div class="change-password-container">
      <mat-card class="change-password-card">
        <mat-card-header>
          <mat-card-title>Change Password</mat-card-title>
          <mat-card-subtitle>Update your account password</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <form [formGroup]="changePasswordForm" (ngSubmit)="onSubmit()">
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Current Password</mat-label>
              <input matInput [type]="hideCurrentPassword ? 'password' : 'text'"
                     formControlName="currentPassword" required>
              <button mat-icon-button matSuffix
                      (click)="hideCurrentPassword = !hideCurrentPassword"
                      type="button">
                <mat-icon>{{ hideCurrentPassword ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              <mat-error *ngIf="changePasswordForm.get('currentPassword')?.hasError('required')">
                Current password is required
              </mat-error>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>New Password</mat-label>
              <input matInput [type]="hideNewPassword ? 'password' : 'text'"
                     formControlName="newPassword" required>
              <button mat-icon-button matSuffix
                      (click)="hideNewPassword = !hideNewPassword"
                      type="button">
                <mat-icon>{{ hideNewPassword ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              <mat-error *ngIf="changePasswordForm.get('newPassword')?.hasError('required')">
                New password is required
              </mat-error>
              <mat-error *ngIf="changePasswordForm.get('newPassword')?.hasError('minlength')">
                Password must be at least 8 characters
              </mat-error>
              <mat-hint>At least 8 characters with uppercase, lowercase, digit and special character</mat-hint>
            </mat-form-field>

            <mat-form-field appearance="outline" class="full-width">
              <mat-label>Confirm New Password</mat-label>
              <input matInput [type]="hideConfirmPassword ? 'password' : 'text'"
                     formControlName="confirmPassword" required>
              <button mat-icon-button matSuffix
                      (click)="hideConfirmPassword = !hideConfirmPassword"
                      type="button">
                <mat-icon>{{ hideConfirmPassword ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              <mat-error *ngIf="changePasswordForm.get('confirmPassword')?.hasError('required')">
                Please confirm your new password
              </mat-error>
              <mat-error *ngIf="changePasswordForm.hasError('passwordMismatch')">
                Passwords do not match
              </mat-error>
            </mat-form-field>

            <div class="password-requirements">
              <h4>Password Requirements:</h4>
              <ul>
                <li [class.met]="hasMinLength">At least 8 characters</li>
                <li [class.met]="hasUppercase">At least one uppercase letter</li>
                <li [class.met]="hasLowercase">At least one lowercase letter</li>
                <li [class.met]="hasDigit">At least one digit</li>
                <li [class.met]="hasSpecialChar">At least one special character</li>
              </ul>
            </div>

            <div class="form-actions">
              <button mat-raised-button color="primary" type="submit"
                      [disabled]="changePasswordForm.invalid || isLoading" class="full-width">
                {{ isLoading ? 'Updating...' : 'Update Password' }}
              </button>

              <button mat-button (click)="onCancel()" type="button" class="full-width">
                Cancel
              </button>
            </div>
          </form>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .change-password-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background: #f5f5f5;
      padding: 24px;
    }

    .change-password-card {
      max-width: 500px;
      width: 100%;
    }

    .full-width {
      width: 100%;
      margin-bottom: 16px;
    }

    .password-requirements {
      background: #f9f9f9;
      border-left: 4px solid #667eea;
      padding: 16px;
      margin: 24px 0;
      border-radius: 4px;
    }

    .password-requirements h4 {
      margin: 0 0 12px 0;
      color: #333;
      font-size: 14px;
      font-weight: 500;
    }

    .password-requirements ul {
      margin: 0;
      padding-left: 20px;
      list-style-type: none;
    }

    .password-requirements li {
      margin: 8px 0;
      color: #666;
      position: relative;
      font-size: 13px;
    }

    .password-requirements li::before {
      content: '○';
      position: absolute;
      left: -20px;
      color: #999;
    }

    .password-requirements li.met {
      color: #4caf50;
    }

    .password-requirements li.met::before {
      content: '✓';
      color: #4caf50;
      font-weight: bold;
    }

    .form-actions {
      margin-top: 24px;
    }
  `]
})
export class ChangePasswordComponent implements OnInit {
  changePasswordForm: FormGroup;
  hideCurrentPassword = true;
  hideNewPassword = true;
  hideConfirmPassword = true;
  isLoading = false;
  currentUserId?: number;

  // Password validation flags
  hasMinLength = false;
  hasUppercase = false;
  hasLowercase = false;
  hasDigit = false;
  hasSpecialChar = false;

  constructor(
    private fb: FormBuilder,
    private userService: UserService,
    private authService: AuthService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.changePasswordForm = this.fb.group({
      currentPassword: ['', Validators.required],
      newPassword: ['', [Validators.required, Validators.minLength(8)]],
      confirmPassword: ['', Validators.required]
    }, { validators: this.passwordMatchValidator });
  }

  ngOnInit(): void {
    // Get current user ID
    this.authService.getCurrentUser().subscribe({
      next: (user) => {
        this.currentUserId = user.id;
      }
    });

    // Subscribe to new password changes to validate requirements
    this.changePasswordForm.get('newPassword')?.valueChanges.subscribe(value => {
      this.validatePasswordRequirements(value);
    });
  }

  passwordMatchValidator(form: FormGroup) {
    const newPassword = form.get('newPassword');
    const confirmPassword = form.get('confirmPassword');

    if (newPassword && confirmPassword && newPassword.value !== confirmPassword.value) {
      return { passwordMismatch: true };
    }
    return null;
  }

  validatePasswordRequirements(password: string): void {
    this.hasMinLength = password.length >= 8;
    this.hasUppercase = /[A-Z]/.test(password);
    this.hasLowercase = /[a-z]/.test(password);
    this.hasDigit = /\d/.test(password);
    this.hasSpecialChar = /[!@#$%^&*(),.?":{}|<>]/.test(password);
  }

  onSubmit(): void {
    if (this.changePasswordForm.valid && this.currentUserId) {
      this.isLoading = true;

      const formValue = this.changePasswordForm.value;

      this.userService.updateUserPassword(this.currentUserId, formValue.newPassword).subscribe({
        next: () => {
          this.isLoading = false;
          this.snackBar.open('Password updated successfully!', 'Close', { duration: 5000 });
          this.router.navigate(['/dashboard']);
        },
        error: (error) => {
          this.isLoading = false;
          const errorMessage = error.error?.message || 'Failed to update password. Please try again.';
          this.snackBar.open(errorMessage, 'Close', { duration: 5000 });
        }
      });
    }
  }

  onCancel(): void {
    this.router.navigate(['/dashboard']);
  }
}
