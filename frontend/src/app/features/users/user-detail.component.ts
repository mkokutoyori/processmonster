import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatDividerModule } from '@angular/material/divider';
import { MatListModule } from '@angular/material/list';
import { UserService } from '../../core/services/user.service';
import { User } from '../../core/models/user.model';

/**
 * User Detail Page (Read-Only View)
 */
@Component({
  selector: 'app-user-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatChipsModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    MatDividerModule,
    MatListModule
  ],
  template: `
    <div class="user-detail-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <mat-icon>person</mat-icon>
            User Details
          </mat-card-title>
          <mat-card-subtitle *ngIf="user">{{ user.username }}</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <div class="loading-container" *ngIf="isLoading">
            <mat-spinner diameter="50"></mat-spinner>
            <p>Loading user data...</p>
          </div>

          <div class="user-details" *ngIf="!isLoading && user">
            <!-- User Information -->
            <div class="detail-section">
              <h3>
                <mat-icon>info</mat-icon>
                Basic Information
              </h3>
              <mat-divider></mat-divider>

              <mat-list>
                <mat-list-item>
                  <mat-icon matListItemIcon>person</mat-icon>
                  <div matListItemTitle>Username</div>
                  <div matListItemLine>{{ user.username }}</div>
                </mat-list-item>

                <mat-list-item>
                  <mat-icon matListItemIcon>email</mat-icon>
                  <div matListItemTitle>Email</div>
                  <div matListItemLine>{{ user.email }}</div>
                </mat-list-item>

                <mat-list-item>
                  <mat-icon matListItemIcon>badge</mat-icon>
                  <div matListItemTitle>Full Name</div>
                  <div matListItemLine>{{ user.firstname }} {{ user.lastname }}</div>
                </mat-list-item>

                <mat-list-item *ngIf="user.phone">
                  <mat-icon matListItemIcon>phone</mat-icon>
                  <div matListItemTitle>Phone</div>
                  <div matListItemLine>{{ user.phone }}</div>
                </mat-list-item>
              </mat-list>
            </div>

            <!-- Roles & Permissions -->
            <div class="detail-section">
              <h3>
                <mat-icon>security</mat-icon>
                Roles & Permissions
              </h3>
              <mat-divider></mat-divider>

              <div class="roles-container">
                <mat-chip-set>
                  <mat-chip *ngFor="let role of user.roles" color="primary">
                    <mat-icon matChipAvatar>{{ getRoleIcon(role.name) }}</mat-icon>
                    {{ role.name }}
                  </mat-chip>
                </mat-chip-set>

                <div class="permissions-list" *ngIf="user.roles && user.roles.length > 0">
                  <h4>Permissions:</h4>
                  <ul>
                    <li *ngFor="let role of user.roles">
                      <strong>{{ role.name }}:</strong>
                      <span *ngIf="role.permissions && role.permissions.length > 0">
                        {{ role.permissions.join(', ') }}
                      </span>
                      <span *ngIf="!role.permissions || role.permissions.length === 0">
                        No specific permissions
                      </span>
                    </li>
                  </ul>
                </div>
              </div>
            </div>

            <!-- Account Status -->
            <div class="detail-section">
              <h3>
                <mat-icon>timeline</mat-icon>
                Account Status
              </h3>
              <mat-divider></mat-divider>

              <mat-list>
                <mat-list-item>
                  <mat-icon matListItemIcon>toggle_on</mat-icon>
                  <div matListItemTitle>Status</div>
                  <div matListItemLine>
                    <mat-chip [color]="user.active ? 'primary' : 'warn'">
                      {{ user.active ? 'Active' : 'Inactive' }}
                    </mat-chip>
                  </div>
                </mat-list-item>

                <mat-list-item *ngIf="user.createdAt">
                  <mat-icon matListItemIcon>calendar_today</mat-icon>
                  <div matListItemTitle>Created</div>
                  <div matListItemLine>{{ user.createdAt | date:'medium' }}</div>
                </mat-list-item>

                <mat-list-item *ngIf="user.lastModifiedAt">
                  <mat-icon matListItemIcon>update</mat-icon>
                  <div matListItemTitle>Last Modified</div>
                  <div matListItemLine>{{ user.lastModifiedAt | date:'medium' }}</div>
                </mat-list-item>

                <mat-list-item *ngIf="user.lastLoginAt">
                  <mat-icon matListItemIcon>login</mat-icon>
                  <div matListItemTitle>Last Login</div>
                  <div matListItemLine>{{ user.lastLoginAt | date:'medium' }}</div>
                </mat-list-item>
              </mat-list>
            </div>

            <!-- Statistics (if available) -->
            <div class="detail-section" *ngIf="hasStatistics()">
              <h3>
                <mat-icon>analytics</mat-icon>
                Statistics
              </h3>
              <mat-divider></mat-divider>

              <div class="stats-grid">
                <div class="stat-card" *ngIf="user.processesCreated !== undefined">
                  <div class="stat-value">{{ user.processesCreated || 0 }}</div>
                  <div class="stat-label">Processes Created</div>
                </div>

                <div class="stat-card" *ngIf="user.instancesStarted !== undefined">
                  <div class="stat-value">{{ user.instancesStarted || 0 }}</div>
                  <div class="stat-label">Instances Started</div>
                </div>

                <div class="stat-card" *ngIf="user.tasksCompleted !== undefined">
                  <div class="stat-value">{{ user.tasksCompleted || 0 }}</div>
                  <div class="stat-label">Tasks Completed</div>
                </div>
              </div>
            </div>
          </div>
        </mat-card-content>

        <mat-card-actions *ngIf="!isLoading">
          <button mat-raised-button color="primary" (click)="onEdit()">
            <mat-icon>edit</mat-icon>
            Edit User
          </button>

          <button mat-button (click)="onBack()">
            <mat-icon>arrow_back</mat-icon>
            Back to List
          </button>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [`
    .user-detail-container {
      padding: 24px;
      max-width: 1000px;
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

    .user-details {
      margin-top: 24px;
    }

    .detail-section {
      margin-bottom: 32px;
    }

    .detail-section h3 {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 16px;
      color: #1976d2;
    }

    .detail-section h4 {
      margin: 16px 0 8px 0;
      color: #666;
      font-size: 14px;
      font-weight: 500;
    }

    mat-list-item {
      height: auto;
      min-height: 48px;
      padding: 12px 0;
    }

    .roles-container {
      padding: 16px 0;
    }

    .permissions-list {
      margin-top: 16px;
      padding: 16px;
      background: #f5f5f5;
      border-radius: 4px;
    }

    .permissions-list ul {
      list-style: none;
      padding: 0;
      margin: 8px 0 0 0;
    }

    .permissions-list li {
      padding: 8px 0;
      border-bottom: 1px solid #e0e0e0;
    }

    .permissions-list li:last-child {
      border-bottom: none;
    }

    .stats-grid {
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
      gap: 16px;
      margin-top: 16px;
    }

    .stat-card {
      background: #f5f5f5;
      padding: 24px;
      border-radius: 8px;
      text-align: center;
    }

    .stat-value {
      font-size: 32px;
      font-weight: 500;
      color: #1976d2;
      margin-bottom: 8px;
    }

    .stat-label {
      color: #666;
      font-size: 14px;
    }

    mat-card-actions {
      display: flex;
      gap: 12px;
      padding: 16px;
      border-top: 1px solid #e0e0e0;
    }

    mat-card-actions button {
      display: flex;
      align-items: center;
      gap: 8px;
    }
  `]
})
export class UserDetailComponent implements OnInit {
  user: User | null = null;
  userId: number = 0;
  isLoading = true;

  constructor(
    private userService: UserService,
    private router: Router,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar
  ) {}

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
    this.isLoading = true;
    this.userService.getUserById(this.userId).subscribe({
      next: (user: User) => {
        this.user = user;
        this.isLoading = false;
      },
      error: (error) => {
        this.isLoading = false;
        const errorMessage = error.error?.message || 'Failed to load user data';
        this.snackBar.open(errorMessage, 'Close', { duration: 5000 });
        this.router.navigate(['/users']);
      }
    });
  }

  getRoleIcon(roleName: string): string {
    const roleIcons: { [key: string]: string } = {
      'ADMIN': 'admin_panel_settings',
      'ADMINISTRATOR': 'admin_panel_settings',
      'MANAGER': 'manage_accounts',
      'USER': 'person',
      'ANALYST': 'analytics',
      'AUDITOR': 'fact_check'
    };
    return roleIcons[roleName.toUpperCase()] || 'badge';
  }

  hasStatistics(): boolean {
    if (!this.user) return false;
    return this.user.processesCreated !== undefined ||
           this.user.instancesStarted !== undefined ||
           this.user.tasksCompleted !== undefined;
  }

  onEdit(): void {
    this.router.navigate(['/users', this.userId, 'edit']);
  }

  onBack(): void {
    this.router.navigate(['/users']);
  }
}
