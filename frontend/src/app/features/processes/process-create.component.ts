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
import { MatStepperModule } from '@angular/material/stepper';
import { MatRadioModule } from '@angular/material/radio';
import { ProcessService } from '../../core/services/process.service';

/**
 * Process Creation Page
 * Multi-step wizard for creating a new BPM process
 */
@Component({
  selector: 'app-process-create',
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
    MatStepperModule,
    MatRadioModule
  ],
  template: `
    <div class="process-create-container">
      <mat-card>
        <mat-card-header>
          <mat-card-title>
            <mat-icon>add_circle</mat-icon>
            Create New Process
          </mat-card-title>
          <mat-card-subtitle>Define a new business process</mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <mat-stepper linear #stepper>
            <!-- Step 1: Basic Information -->
            <mat-step [stepControl]="basicInfoForm">
              <form [formGroup]="basicInfoForm">
                <ng-template matStepLabel>Basic Information</ng-template>

                <div class="step-content">
                  <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Process Name</mat-label>
                    <input matInput formControlName="name" required
                           placeholder="e.g., Loan Application Process">
                    <mat-icon matSuffix>text_fields</mat-icon>
                    <mat-error *ngIf="basicInfoForm.get('name')?.hasError('required')">
                      Process name is required
                    </mat-error>
                    <mat-error *ngIf="basicInfoForm.get('name')?.hasError('minlength')">
                      Minimum 3 characters
                    </mat-error>
                  </mat-form-field>

                  <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Process Key</mat-label>
                    <input matInput formControlName="processKey" required
                           placeholder="e.g., loan-application">
                    <mat-icon matSuffix>vpn_key</mat-icon>
                    <mat-hint>Unique identifier (lowercase, hyphens allowed)</mat-hint>
                    <mat-error *ngIf="basicInfoForm.get('processKey')?.hasError('required')">
                      Process key is required
                    </mat-error>
                    <mat-error *ngIf="basicInfoForm.get('processKey')?.hasError('pattern')">
                      Only lowercase letters, numbers, and hyphens allowed
                    </mat-error>
                  </mat-form-field>

                  <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Description</mat-label>
                    <textarea matInput formControlName="description" rows="4"
                              placeholder="Describe the purpose of this process..."></textarea>
                    <mat-icon matSuffix>description</mat-icon>
                  </mat-form-field>

                  <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Category</mat-label>
                    <mat-select formControlName="category" required>
                      <mat-option value="BANKING">Banking</mat-option>
                      <mat-option value="LOAN">Loan Management</mat-option>
                      <mat-option value="ACCOUNT">Account Management</mat-option>
                      <mat-option value="COMPLIANCE">Compliance</mat-option>
                      <mat-option value="HR">Human Resources</mat-option>
                      <mat-option value="OTHER">Other</mat-option>
                    </mat-select>
                    <mat-error *ngIf="basicInfoForm.get('category')?.hasError('required')">
                      Category is required
                    </mat-error>
                  </mat-form-field>

                  <div class="step-actions">
                    <button mat-raised-button color="primary" matStepperNext
                            [disabled]="basicInfoForm.invalid">
                      Next
                      <mat-icon>arrow_forward</mat-icon>
                    </button>
                  </div>
                </div>
              </form>
            </mat-step>

            <!-- Step 2: Process Type & Template -->
            <mat-step [stepControl]="processTypeForm">
              <form [formGroup]="processTypeForm">
                <ng-template matStepLabel>Process Type</ng-template>

                <div class="step-content">
                  <h3>Choose Process Creation Method</h3>

                  <mat-radio-group formControlName="creationMethod" class="creation-method-group">
                    <mat-radio-button value="BLANK">
                      <div class="method-option">
                        <mat-icon>description</mat-icon>
                        <div>
                          <strong>Start from Blank</strong>
                          <p>Create a new process from scratch using the BPMN editor</p>
                        </div>
                      </div>
                    </mat-radio-button>

                    <mat-radio-button value="TEMPLATE">
                      <div class="method-option">
                        <mat-icon>content_copy</mat-icon>
                        <div>
                          <strong>Use Template</strong>
                          <p>Start from a pre-defined process template</p>
                        </div>
                      </div>
                    </mat-radio-button>

                    <mat-radio-button value="IMPORT">
                      <div class="method-option">
                        <mat-icon>upload_file</mat-icon>
                        <div>
                          <strong>Import BPMN File</strong>
                          <p>Upload an existing BPMN 2.0 XML file</p>
                        </div>
                      </div>
                    </mat-radio-button>
                  </mat-radio-group>

                  <!-- Template Selection (conditional) -->
                  <mat-form-field appearance="outline" class="full-width"
                                  *ngIf="processTypeForm.get('creationMethod')?.value === 'TEMPLATE'">
                    <mat-label>Select Template</mat-label>
                    <mat-select formControlName="templateId">
                      <mat-option [value]="1">Simple Approval Process</mat-option>
                      <mat-option [value]="2">Multi-Level Approval</mat-option>
                      <mat-option [value]="3">Customer Onboarding</mat-option>
                      <mat-option [value]="4">Loan Application</mat-option>
                    </mat-select>
                  </mat-form-field>

                  <!-- File Upload (conditional) -->
                  <div class="file-upload" *ngIf="processTypeForm.get('creationMethod')?.value === 'IMPORT'">
                    <input type="file" #fileInput accept=".bpmn,.xml"
                           (change)="onFileSelected($event)" style="display: none">
                    <button mat-stroked-button (click)="fileInput.click()">
                      <mat-icon>cloud_upload</mat-icon>
                      Choose BPMN File
                    </button>
                    <span class="file-name" *ngIf="selectedFileName">
                      {{ selectedFileName }}
                    </span>
                  </div>

                  <div class="step-actions">
                    <button mat-button matStepperPrevious>
                      <mat-icon>arrow_back</mat-icon>
                      Back
                    </button>
                    <button mat-raised-button color="primary" matStepperNext
                            [disabled]="processTypeForm.invalid">
                      Next
                      <mat-icon>arrow_forward</mat-icon>
                    </button>
                  </div>
                </div>
              </form>
            </mat-step>

            <!-- Step 3: Configuration -->
            <mat-step [stepControl]="configForm">
              <form [formGroup]="configForm">
                <ng-template matStepLabel>Configuration</ng-template>

                <div class="step-content">
                  <h3>Process Configuration</h3>

                  <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Version</mat-label>
                    <input matInput formControlName="version" placeholder="1.0.0">
                    <mat-icon matSuffix>tag</mat-icon>
                    <mat-hint>Semantic versioning (e.g., 1.0.0)</mat-hint>
                  </mat-form-field>

                  <mat-form-field appearance="outline" class="full-width">
                    <mat-label>Execution Listeners</mat-label>
                    <mat-select formControlName="executionListeners" multiple>
                      <mat-option value="AUDIT_LOGGER">Audit Logger</mat-option>
                      <mat-option value="EMAIL_NOTIFIER">Email Notifier</mat-option>
                      <mat-option value="WEBHOOK_TRIGGER">Webhook Trigger</mat-option>
                      <mat-option value="METRICS_COLLECTOR">Metrics Collector</mat-option>
                    </mat-select>
                    <mat-hint>Select listeners to execute during process lifecycle</mat-hint>
                  </mat-form-field>

                  <div class="config-options">
                    <h4>Process Options</h4>

                    <div class="option-item">
                      <mat-icon>history</mat-icon>
                      <div>
                        <strong>History Level</strong>
                        <mat-form-field appearance="outline">
                          <mat-select formControlName="historyLevel">
                            <mat-option value="NONE">None</mat-option>
                            <mat-option value="ACTIVITY">Activity</mat-option>
                            <mat-option value="FULL">Full</mat-option>
                            <mat-option value="AUDIT">Audit</mat-option>
                          </mat-select>
                        </mat-form-field>
                      </div>
                    </div>

                    <div class="option-item">
                      <mat-icon>priority_high</mat-icon>
                      <div>
                        <strong>Default Priority</strong>
                        <mat-form-field appearance="outline">
                          <input matInput type="number" formControlName="defaultPriority"
                                 min="0" max="100">
                          <mat-hint>0-100 (higher is more important)</mat-hint>
                        </mat-form-field>
                      </div>
                    </div>
                  </div>

                  <div class="step-actions">
                    <button mat-button matStepperPrevious>
                      <mat-icon>arrow_back</mat-icon>
                      Back
                    </button>
                    <button mat-raised-button color="primary" matStepperNext
                            [disabled]="configForm.invalid">
                      Next
                      <mat-icon>arrow_forward</mat-icon>
                    </button>
                  </div>
                </div>
              </form>
            </mat-step>

            <!-- Step 4: Review & Create -->
            <mat-step>
              <ng-template matStepLabel>Review</ng-template>

              <div class="step-content">
                <h3>Review Process Details</h3>

                <div class="review-section">
                  <h4>Basic Information</h4>
                  <dl>
                    <dt>Name:</dt>
                    <dd>{{ basicInfoForm.get('name')?.value }}</dd>
                    <dt>Process Key:</dt>
                    <dd><code>{{ basicInfoForm.get('processKey')?.value }}</code></dd>
                    <dt>Category:</dt>
                    <dd><mat-chip>{{ basicInfoForm.get('category')?.value }}</mat-chip></dd>
                    <dt>Description:</dt>
                    <dd>{{ basicInfoForm.get('description')?.value || 'N/A' }}</dd>
                  </dl>
                </div>

                <div class="review-section">
                  <h4>Process Type</h4>
                  <dl>
                    <dt>Creation Method:</dt>
                    <dd><mat-chip>{{ processTypeForm.get('creationMethod')?.value }}</mat-chip></dd>
                  </dl>
                </div>

                <div class="review-section">
                  <h4>Configuration</h4>
                  <dl>
                    <dt>Version:</dt>
                    <dd>{{ configForm.get('version')?.value }}</dd>
                    <dt>History Level:</dt>
                    <dd>{{ configForm.get('historyLevel')?.value }}</dd>
                    <dt>Default Priority:</dt>
                    <dd>{{ configForm.get('defaultPriority')?.value }}</dd>
                  </dl>
                </div>

                <div class="step-actions">
                  <button mat-button matStepperPrevious>
                    <mat-icon>arrow_back</mat-icon>
                    Back
                  </button>
                  <button mat-raised-button color="primary" (click)="onSubmit()"
                          [disabled]="isLoading">
                    <mat-icon>save</mat-icon>
                    {{ isLoading ? 'Creating...' : 'Create Process' }}
                  </button>
                </div>
              </div>
            </mat-step>
          </mat-stepper>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .process-create-container {
      padding: 24px;
      max-width: 900px;
      margin: 0 auto;
    }

    mat-card-title {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .step-content {
      padding: 24px 0;
    }

    .step-content h3 {
      margin: 0 0 24px 0;
      color: #333;
    }

    .step-content h4 {
      margin: 16px 0 8px 0;
      color: #666;
      font-size: 14px;
      font-weight: 500;
    }

    .full-width {
      width: 100%;
      margin-bottom: 16px;
    }

    .step-actions {
      display: flex;
      gap: 12px;
      margin-top: 32px;
      padding-top: 24px;
      border-top: 1px solid #e0e0e0;
    }

    .step-actions button {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .creation-method-group {
      display: flex;
      flex-direction: column;
      gap: 16px;
      margin: 24px 0;
    }

    .creation-method-group mat-radio-button {
      padding: 16px;
      border: 2px solid #e0e0e0;
      border-radius: 8px;
      transition: all 0.2s;
    }

    .creation-method-group mat-radio-button:hover {
      border-color: #1976d2;
      background: #f5f5f5;
    }

    .method-option {
      display: flex;
      align-items: flex-start;
      gap: 16px;
      margin-left: 8px;
    }

    .method-option mat-icon {
      font-size: 32px;
      width: 32px;
      height: 32px;
      color: #1976d2;
    }

    .method-option strong {
      display: block;
      margin-bottom: 4px;
      color: #333;
    }

    .method-option p {
      margin: 0;
      color: #666;
      font-size: 14px;
    }

    .file-upload {
      margin: 24px 0;
      padding: 24px;
      border: 2px dashed #e0e0e0;
      border-radius: 8px;
      text-align: center;
    }

    .file-name {
      display: block;
      margin-top: 12px;
      color: #666;
      font-size: 14px;
    }

    .config-options {
      margin: 24px 0;
    }

    .option-item {
      display: flex;
      align-items: flex-start;
      gap: 16px;
      padding: 16px;
      background: #f5f5f5;
      border-radius: 8px;
      margin-bottom: 16px;
    }

    .option-item mat-icon {
      color: #1976d2;
      margin-top: 8px;
    }

    .option-item > div {
      flex: 1;
    }

    .option-item strong {
      display: block;
      margin-bottom: 8px;
      color: #333;
    }

    .option-item mat-form-field {
      width: 100%;
    }

    .review-section {
      margin-bottom: 24px;
      padding: 16px;
      background: #f5f5f5;
      border-radius: 8px;
    }

    .review-section dl {
      margin: 0;
    }

    .review-section dt {
      font-weight: 500;
      color: #666;
      margin: 8px 0 4px 0;
    }

    .review-section dd {
      margin: 0 0 12px 0;
      color: #333;
    }

    code {
      background: #fff;
      padding: 4px 8px;
      border-radius: 4px;
      font-family: 'Courier New', monospace;
      font-size: 12px;
    }
  `]
})
export class ProcessCreateComponent implements OnInit {
  basicInfoForm: FormGroup;
  processTypeForm: FormGroup;
  configForm: FormGroup;
  isLoading = false;
  selectedFileName: string = '';

  constructor(
    private fb: FormBuilder,
    private processService: ProcessService,
    private router: Router,
    private snackBar: MatSnackBar
  ) {
    this.basicInfoForm = this.fb.group({
      name: ['', [Validators.required, Validators.minLength(3)]],
      processKey: ['', [Validators.required, Validators.pattern(/^[a-z0-9-]+$/)]],
      description: [''],
      category: ['', Validators.required]
    });

    this.processTypeForm = this.fb.group({
      creationMethod: ['BLANK', Validators.required],
      templateId: [null],
      bpmnFile: [null]
    });

    this.configForm = this.fb.group({
      version: ['1.0.0'],
      executionListeners: [[]],
      historyLevel: ['FULL'],
      defaultPriority: [50, [Validators.min(0), Validators.max(100)]]
    });
  }

  ngOnInit(): void {}

  onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.selectedFileName = file.name;
      this.processTypeForm.patchValue({ bpmnFile: file });
    }
  }

  onSubmit(): void {
    if (this.basicInfoForm.valid && this.processTypeForm.valid && this.configForm.valid) {
      this.isLoading = true;

      const processData = {
        ...this.basicInfoForm.value,
        ...this.processTypeForm.value,
        ...this.configForm.value
      };

      this.processService.createProcess(processData).subscribe({
        next: (process) => {
          this.snackBar.open('Process created successfully!', 'Close', { duration: 3000 });

          // Navigate to process editor or detail page
          if (processData.creationMethod === 'BLANK') {
            this.router.navigate(['/processes', process.id, 'edit']);
          } else {
            this.router.navigate(['/processes', process.id]);
          }
        },
        error: (error) => {
          this.isLoading = false;
          const errorMessage = error.error?.message || 'Failed to create process';
          this.snackBar.open(errorMessage, 'Close', { duration: 5000 });
        }
      });
    }
  }
}
