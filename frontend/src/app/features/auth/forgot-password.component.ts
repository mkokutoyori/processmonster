import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';

/**
 * Forgot Password Page
 */
@Component({
  selector: 'app-forgot-password',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatSnackBarModule
  ],
  template: `
    <div class="forgot-password-container">
      <mat-card class="forgot-password-card">
        <mat-card-header>
          <mat-card-title>Forgot Password</mat-card-title>
          <mat-card-subtitle>
            {{ emailSent ? 'Check your email' : 'Reset your password' }}
          </mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <div *ngIf="!emailSent">
            <p class="instructions">
              Enter your email address and we'll send you instructions to reset your password.
            </p>

            <form [formGroup]="forgotPasswordForm" (ngSubmit)="onSubmit()">
              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Email Address</mat-label>
                <input matInput type="email" formControlName="email" required
                       placeholder="you@example.com">
                <mat-icon matSuffix>email</mat-icon>
                <mat-error *ngIf="forgotPasswordForm.get('email')?.hasError('required')">
                  Email is required
                </mat-error>
                <mat-error *ngIf="forgotPasswordForm.get('email')?.hasError('email')">
                  Invalid email format
                </mat-error>
              </mat-form-field>

              <div class="form-actions">
                <button mat-raised-button color="primary" type="submit"
                        [disabled]="forgotPasswordForm.invalid || isLoading" class="full-width">
                  {{ isLoading ? 'Sending...' : 'Send Reset Link' }}
                </button>

                <button mat-button routerLink="/login" type="button" class="full-width">
                  Back to Login
                </button>
              </div>
            </form>
          </div>

          <div *ngIf="emailSent" class="success-message">
            <mat-icon class="success-icon">check_circle</mat-icon>
            <h3>Email Sent!</h3>
            <p>
              We've sent a password reset link to <strong>{{ submittedEmail }}</strong>.
              Please check your inbox and follow the instructions.
            </p>
            <p class="note">
              Didn't receive the email? Check your spam folder or
              <a href="#" (click)="resendEmail($event)">resend the link</a>.
            </p>
            <button mat-raised-button color="primary" routerLink="/login" class="full-width">
              Return to Login
            </button>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .forgot-password-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 24px;
    }

    .forgot-password-card {
      max-width: 450px;
      width: 100%;
    }

    .full-width {
      width: 100%;
      margin-bottom: 12px;
    }

    .instructions {
      color: #666;
      margin-bottom: 24px;
      line-height: 1.6;
    }

    .form-actions {
      margin-top: 24px;
    }

    .success-message {
      text-align: center;
      padding: 24px 0;
    }

    .success-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      color: #4caf50;
      margin-bottom: 16px;
    }

    .success-message h3 {
      margin: 16px 0;
      color: #333;
    }

    .success-message p {
      color: #666;
      margin: 12px 0;
      line-height: 1.6;
    }

    .success-message strong {
      color: #667eea;
    }

    .note {
      font-size: 14px;
      margin-top: 24px !important;
    }

    .note a {
      color: #667eea;
      text-decoration: none;
      font-weight: 500;
    }

    .note a:hover {
      text-decoration: underline;
    }
  `]
})
export class ForgotPasswordComponent {
  forgotPasswordForm: FormGroup;
  isLoading = false;
  emailSent = false;
  submittedEmail = '';

  constructor(
    private fb: FormBuilder,
    private snackBar: MatSnackBar
  ) {
    this.forgotPasswordForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]]
    });
  }

  onSubmit(): void {
    if (this.forgotPasswordForm.valid) {
      this.isLoading = true;
      this.submittedEmail = this.forgotPasswordForm.value.email;

      // TODO: Implement actual password reset API call
      setTimeout(() => {
        this.isLoading = false;
        this.emailSent = true;
      }, 1500);

      // Actual implementation would be:
      // this.authService.forgotPassword(this.submittedEmail).subscribe({
      //   next: () => {
      //     this.isLoading = false;
      //     this.emailSent = true;
      //   },
      //   error: (error) => {
      //     this.isLoading = false;
      //     this.snackBar.open('Failed to send reset email. Please try again.', 'Close', {
      //       duration: 5000
      //     });
      //   }
      // });
    }
  }

  resendEmail(event: Event): void {
    event.preventDefault();
    this.isLoading = true;

    // TODO: Implement actual resend API call
    setTimeout(() => {
      this.isLoading = false;
      this.snackBar.open('Reset email sent again!', 'Close', { duration: 3000 });
    }, 1000);
  }
}
