/**
 * Login Component
 *
 * Handles user authentication with username/email and password.
 * Supports internationalization (FR/EN).
 */
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { ToastrService } from 'ngx-toastr';
import { AuthService } from '../../../core/services/auth.service';
import { environment } from '../../../../environments/environment';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
    MatSelectModule,
    TranslateModule
  ],
  template: `
    <div class="login-container">
      <mat-card class="login-card">
        <mat-card-header>
          <div class="logo-container">
            <h1>🏦 ProcessMonster</h1>
            <p class="subtitle">{{ 'app.title' | translate }}</p>
          </div>
        </mat-card-header>

        <mat-card-content>
          <form [formGroup]="loginForm" (ngSubmit)="onSubmit()">
            <!-- Username/Email Field -->
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>{{ 'auth.username' | translate }}</mat-label>
              <input
                matInput
                type="text"
                formControlName="usernameOrEmail"
                [placeholder]="'auth.username' | translate"
                autocomplete="username"
              />
              <mat-icon matPrefix>person</mat-icon>
              @if (loginForm.get('usernameOrEmail')?.hasError('required') && loginForm.get('usernameOrEmail')?.touched) {
                <mat-error>{{ 'validation.required' | translate: {field: ('auth.username' | translate)} }}</mat-error>
              }
            </mat-form-field>

            <!-- Password Field -->
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>{{ 'auth.password' | translate }}</mat-label>
              <input
                matInput
                [type]="hidePassword ? 'password' : 'text'"
                formControlName="password"
                [placeholder]="'auth.password' | translate"
                autocomplete="current-password"
              />
              <mat-icon matPrefix>lock</mat-icon>
              <button
                mat-icon-button
                matSuffix
                type="button"
                (click)="hidePassword = !hidePassword"
                [attr.aria-label]="'Hide password'"
              >
                <mat-icon>{{ hidePassword ? 'visibility_off' : 'visibility' }}</mat-icon>
              </button>
              @if (loginForm.get('password')?.hasError('required') && loginForm.get('password')?.touched) {
                <mat-error>{{ 'validation.required' | translate: {field: ('auth.password' | translate)} }}</mat-error>
              }
            </mat-form-field>

            <!-- Language Selector -->
            <mat-form-field appearance="outline" class="full-width">
              <mat-label>{{ 'common.language' | translate }}</mat-label>
              <mat-select [(value)]="selectedLanguage" (selectionChange)="changeLanguage($event.value)">
                <mat-option value="en">🇬🇧 English</mat-option>
                <mat-option value="fr">🇫🇷 Français</mat-option>
              </mat-select>
            </mat-form-field>

            <!-- Submit Button -->
            <button
              mat-raised-button
              color="primary"
              type="submit"
              class="full-width login-button"
              [disabled]="loginForm.invalid || loading"
            >
              @if (loading) {
                <mat-spinner diameter="20"></mat-spinner>
              } @else {
                {{ 'auth.login' | translate }}
              }
            </button>

            <!-- Forgot Password Link -->
            <div class="text-center mt-2">
              <a href="#" class="forgot-password-link">
                {{ 'auth.forgotPassword' | translate }}
              </a>
            </div>
          </form>
        </mat-card-content>

        <mat-card-footer>
          <p class="footer-text">
            {{ 'app.name' | translate }} &copy; 2025
          </p>
        </mat-card-footer>
      </mat-card>
    </div>
  `,
  styles: [`
    .login-container {
      display: flex;
      justify-content: center;
      align-items: center;
      min-height: 100vh;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      padding: 1rem;
    }

    .login-card {
      width: 100%;
      max-width: 450px;
      padding: 2rem;
    }

    .logo-container {
      width: 100%;
      text-align: center;
      margin-bottom: 1.5rem;
    }

    .logo-container h1 {
      margin: 0;
      font-size: 2rem;
      color: #333;
    }

    .subtitle {
      margin: 0.5rem 0 0 0;
      color: #666;
      font-size: 0.9rem;
    }

    mat-form-field {
      margin-bottom: 1rem;
    }

    .login-button {
      height: 48px;
      font-size: 1rem;
      margin-top: 1rem;
    }

    .forgot-password-link {
      color: #667eea;
      text-decoration: none;
      font-size: 0.9rem;
    }

    .forgot-password-link:hover {
      text-decoration: underline;
    }

    .footer-text {
      text-align: center;
      color: #999;
      font-size: 0.85rem;
      margin-top: 1rem;
    }

    mat-spinner {
      display: inline-block;
      margin: 0 auto;
    }

    @media (max-width: 600px) {
      .login-card {
        padding: 1.5rem;
      }

      .logo-container h1 {
        font-size: 1.5rem;
      }
    }
  `]
})
export class LoginComponent implements OnInit {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private authService = inject(AuthService);
  private translate = inject(TranslateService);
  private toastr = inject(ToastrService);

  loginForm!: FormGroup;
  hidePassword = true;
  loading = false;
  selectedLanguage = environment.defaultLanguage;

  ngOnInit(): void {
    // Initialize form
    this.loginForm = this.fb.group({
      usernameOrEmail: ['', [Validators.required]],
      password: ['', [Validators.required]]
    });

    // Set current language
    this.selectedLanguage = this.translate.currentLang || environment.defaultLanguage;

    // Check if already authenticated
    if (this.authService.isAuthenticated()) {
      this.router.navigate(['/dashboard']);
    }
  }

  /**
   * Handle form submission
   */
  onSubmit(): void {
    if (this.loginForm.invalid) {
      return;
    }

    this.loading = true;

    this.authService.login(this.loginForm.value).subscribe({
      next: () => {
        this.toastr.success(
          this.translate.instant('auth.loginSuccess'),
          this.translate.instant('common.success')
        );

        // Navigate to redirect URL or dashboard
        const redirectUrl = this.authService.getAndClearRedirectUrl();
        this.router.navigate([redirectUrl]);
      },
      error: (error) => {
        this.loading = false;
        // Error is handled by error interceptor, but we can show additional message
        if (environment.enableDebug) {
          console.error('Login error:', error);
        }
      },
      complete: () => {
        this.loading = false;
      }
    });
  }

  /**
   * Change application language
   */
  changeLanguage(lang: string): void {
    this.translate.use(lang);
    this.selectedLanguage = lang;
  }
}
