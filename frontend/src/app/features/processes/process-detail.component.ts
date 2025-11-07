import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCardModule } from '@angular/material/card';
import { MatChipsModule } from '@angular/material/chips';
import { MatTabsModule } from '@angular/material/tabs';
import { MatDividerModule } from '@angular/material/divider';
import { MatTooltipModule } from '@angular/material/tooltip';
import { ProcessDefinitionService } from '../../core/services/process-definition.service';
import { ProcessDefinitionDetail } from '../../core/models/process.model';
import { BpmnViewerComponent } from '../../shared/components/bpmn-viewer/bpmn-viewer.component';

/**
 * Component for viewing process definition details
 */
@Component({
  selector: 'app-process-detail',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatCardModule,
    MatChipsModule,
    MatTabsModule,
    MatDividerModule,
    MatTooltipModule,
    BpmnViewerComponent
  ],
  template: `
    <div class="process-detail-container" *ngIf="process">
      <div class="header">
        <div class="title-section">
          <button mat-icon-button (click)="goBack()" matTooltip="Back to list">
            <mat-icon>arrow_back</mat-icon>
          </button>
          <div class="title-info">
            <h1>{{ process.name }}</h1>
            <div class="meta-info">
              <code>{{ process.processKey }}:v{{ process.version }}</code>
              <mat-chip *ngIf="process.isLatestVersion" class="chip-latest">
                <mat-icon>check_circle</mat-icon>
                Latest
              </mat-chip>
            </div>
          </div>
        </div>
        <div class="action-buttons">
          <button mat-raised-button color="primary" (click)="startProcess()" *ngIf="process.published && process.active">
            <mat-icon>play_arrow</mat-icon>
            Start Process
          </button>
          <button mat-stroked-button (click)="editProcess()">
            <mat-icon>edit</mat-icon>
            Edit
          </button>
          <button mat-stroked-button (click)="downloadBpmn()">
            <mat-icon>download</mat-icon>
            Download BPMN
          </button>
        </div>
      </div>

      <mat-tab-group>
        <!-- Overview Tab -->
        <mat-tab label="Overview">
          <div class="tab-content">
            <mat-card class="info-card">
              <mat-card-header>
                <mat-card-title>Process Information</mat-card-title>
              </mat-card-header>
              <mat-card-content>
                <div class="info-grid">
                  <div class="info-item">
                    <label>Process Key</label>
                    <code>{{ process.processKey }}</code>
                  </div>
                  <div class="info-item">
                    <label>Version</label>
                    <span>{{ process.version }}</span>
                  </div>
                  <div class="info-item">
                    <label>Category</label>
                    <mat-chip *ngIf="process.categoryName">
                      {{ process.categoryName }}
                    </mat-chip>
                    <span *ngIf="!process.categoryName" class="no-value">-</span>
                  </div>
                  <div class="info-item">
                    <label>Status</label>
                    <div class="status-badges">
                      <mat-chip [class.published]="process.published" [class.draft]="!process.published">
                        {{ process.published ? 'Published' : 'Draft' }}
                      </mat-chip>
                      <mat-chip [class.active]="process.active" [class.inactive]="!process.active">
                        {{ process.active ? 'Active' : 'Inactive' }}
                      </mat-chip>
                    </div>
                  </div>
                  <div class="info-item full-width" *ngIf="process.description">
                    <label>Description</label>
                    <p>{{ process.description }}</p>
                  </div>
                  <div class="info-item full-width" *ngIf="process.tags && process.tags.length > 0">
                    <label>Tags</label>
                    <div class="tags">
                      <mat-chip *ngFor="let tag of process.tags">{{ tag }}</mat-chip>
                    </div>
                  </div>
                  <div class="info-item">
                    <label>Created</label>
                    <span>{{ process.createdAt | date:'medium' }}</span>
                  </div>
                  <div class="info-item">
                    <label>Created By</label>
                    <span>{{ process.createdBy }}</span>
                  </div>
                  <div class="info-item">
                    <label>Last Updated</label>
                    <span>{{ process.updatedAt | date:'medium' }}</span>
                  </div>
                  <div class="info-item">
                    <label>Updated By</label>
                    <span>{{ process.updatedBy }}</span>
                  </div>
                </div>
              </mat-card-content>
            </mat-card>
          </div>
        </mat-tab>

        <!-- BPMN Diagram Tab -->
        <mat-tab label="BPMN Diagram">
          <div class="tab-content">
            <mat-card class="diagram-card">
              <mat-card-header>
                <mat-card-title>Process Diagram</mat-card-title>
              </mat-card-header>
              <mat-card-content>
                <app-bpmn-viewer
                  [bpmnXml]="process.bpmnXml"
                  [height]="'600px'"
                  [fitViewport]="true">
                </app-bpmn-viewer>
              </mat-card-content>
            </mat-card>
          </div>
        </mat-tab>

        <!-- Versions Tab -->
        <mat-tab label="Versions">
          <div class="tab-content">
            <mat-card class="versions-card">
              <mat-card-header>
                <mat-card-title>Version History</mat-card-title>
              </mat-card-header>
              <mat-card-content>
                <p class="info-message">
                  <mat-icon>info</mat-icon>
                  Version history will be displayed here. Click on a version to view its details.
                </p>
                <!-- TODO: Add version list when backend endpoint is ready -->
              </mat-card-content>
            </mat-card>
          </div>
        </mat-tab>
      </mat-tab-group>
    </div>

    <div class="loading-container" *ngIf="!process && !error">
      <mat-icon class="spinning">refresh</mat-icon>
      <p>Loading process details...</p>
    </div>

    <div class="error-container" *ngIf="error">
      <mat-icon color="warn">error</mat-icon>
      <h2>Error loading process</h2>
      <p>{{ error }}</p>
      <button mat-raised-button (click)="goBack()">Back to list</button>
    </div>
  `,
  styles: [`
    .process-detail-container {
      padding: 24px;
      max-width: 1400px;
      margin: 0 auto;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 24px;
      gap: 16px;
    }

    .title-section {
      display: flex;
      align-items: center;
      gap: 16px;
      flex: 1;
    }

    .title-info {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .title-info h1 {
      margin: 0;
      font-size: 28px;
      font-weight: 500;
    }

    .meta-info {
      display: flex;
      align-items: center;
      gap: 12px;
    }

    code {
      background: #f5f5f5;
      padding: 4px 8px;
      border-radius: 4px;
      font-family: 'Courier New', monospace;
      font-size: 14px;
    }

    .chip-latest {
      background-color: #4caf50 !important;
      color: white !important;
    }

    .chip-latest mat-icon {
      font-size: 16px;
      width: 16px;
      height: 16px;
    }

    .action-buttons {
      display: flex;
      gap: 12px;
      flex-shrink: 0;
    }

    .tab-content {
      padding: 24px 0;
    }

    .info-card, .diagram-card, .versions-card {
      margin-bottom: 24px;
    }

    .info-grid {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 24px;
      margin-top: 16px;
    }

    .info-item {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .info-item.full-width {
      grid-column: 1 / -1;
    }

    .info-item label {
      font-weight: 500;
      color: #666;
      font-size: 14px;
    }

    .info-item span, .info-item p {
      font-size: 16px;
      color: #333;
    }

    .info-item p {
      margin: 0;
      line-height: 1.6;
    }

    .no-value {
      color: #999;
      font-style: italic;
    }

    .status-badges {
      display: flex;
      gap: 8px;
    }

    mat-chip.published {
      background-color: #4caf50 !important;
      color: white !important;
    }

    mat-chip.draft {
      background-color: #ff9800 !important;
      color: white !important;
    }

    mat-chip.active {
      background-color: #2196f3 !important;
      color: white !important;
    }

    mat-chip.inactive {
      background-color: #9e9e9e !important;
      color: white !important;
    }

    .tags {
      display: flex;
      flex-wrap: wrap;
      gap: 8px;
    }

    .info-message {
      display: flex;
      align-items: center;
      gap: 12px;
      padding: 16px;
      background: #e3f2fd;
      border-radius: 4px;
      color: #1976d2;
      margin: 16px 0;
    }

    .loading-container, .error-container {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 64px;
      text-align: center;
    }

    .loading-container mat-icon.spinning {
      font-size: 48px;
      width: 48px;
      height: 48px;
      animation: spin 1s linear infinite;
      color: #3f51b5;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }

    .error-container mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
    }

    .error-container h2 {
      margin: 16px 0 8px;
    }

    .error-container p {
      color: #666;
      margin-bottom: 24px;
    }
  `]
})
export class ProcessDetailComponent implements OnInit {
  process: ProcessDefinitionDetail | null = null;
  error: string | null = null;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private processService: ProcessDefinitionService
  ) {}

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadProcessDetail(+id);
    }
  }

  loadProcessDetail(id: number): void {
    this.processService.getProcessById(id).subscribe({
      next: (process) => {
        this.process = process;
      },
      error: (error) => {
        console.error('Error loading process detail:', error);
        this.error = error.error?.message || 'Failed to load process details';
      }
    });
  }

  goBack(): void {
    this.router.navigate(['/processes']);
  }

  startProcess(): void {
    if (this.process) {
      this.router.navigate(['/processes', this.process.id, 'start']);
    }
  }

  editProcess(): void {
    if (this.process) {
      this.router.navigate(['/processes', this.process.id, 'edit']);
    }
  }

  downloadBpmn(): void {
    if (this.process) {
      this.processService.downloadBpmnXml(this.process.id, this.process.processKey);
    }
  }
}
