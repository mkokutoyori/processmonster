import { Component, OnInit, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { FormsModule, ReactiveFormsModule, FormBuilder, FormGroup, Validators } from '@angular/forms';
import { BpmnEditorComponent } from './bpmn-editor.component';
import { ProcessDefinitionService } from '../../core/services/process-definition.service';
import { ProcessCategoryService } from '../../core/services/process-category.service';

/**
 * Process Editor Page
 * Create or edit BPMN process definitions
 */
@Component({
  selector: 'app-process-editor',
  standalone: true,
  imports: [
    CommonModule,
    FormsModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatSnackBarModule,
    MatProgressSpinnerModule,
    BpmnEditorComponent
  ],
  template: `
    <div class="process-editor-container">
      <!-- Header -->
      <div class="header">
        <div class="header-left">
          <button mat-icon-button (click)="goBack()">
            <mat-icon>arrow_back</mat-icon>
          </button>
          <h1>{{ isEditMode ? 'Edit Process' : 'Create Process' }}</h1>
        </div>
        <div class="header-right">
          <button mat-button (click)="goBack()">
            Cancel
          </button>
          <button mat-raised-button color="primary" (click)="saveProcess()" [disabled]="isLoading || !isFormValid()">
            <mat-icon>save</mat-icon>
            {{ isEditMode ? 'Update' : 'Create' }}
          </button>
        </div>
      </div>

      <div class="content">
        <!-- Process Metadata Form -->
        <mat-card class="metadata-card">
          <mat-card-content>
            <form [formGroup]="processForm" class="metadata-form">
              <mat-form-field appearance="outline">
                <mat-label>Process Name</mat-label>
                <input matInput formControlName="name" placeholder="Enter process name" required>
                <mat-error *ngIf="processForm.get('name')?.hasError('required')">
                  Name is required
                </mat-error>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Process Key</mat-label>
                <input matInput formControlName="key" placeholder="process-key" required>
                <mat-hint>Unique identifier (lowercase, hyphenated)</mat-hint>
                <mat-error *ngIf="processForm.get('key')?.hasError('required')">
                  Key is required
                </mat-error>
                <mat-error *ngIf="processForm.get('key')?.hasError('pattern')">
                  Key must be lowercase with hyphens
                </mat-error>
              </mat-form-field>

              <mat-form-field appearance="outline">
                <mat-label>Category</mat-label>
                <mat-select formControlName="categoryId" required>
                  <mat-option *ngFor="let category of categories" [value]="category.id">
                    {{ category.name }}
                  </mat-option>
                </mat-select>
                <mat-error *ngIf="processForm.get('categoryId')?.hasError('required')">
                  Category is required
                </mat-error>
              </mat-form-field>

              <mat-form-field appearance="outline" class="full-width">
                <mat-label>Description</mat-label>
                <textarea matInput formControlName="description" rows="3" placeholder="Enter process description"></textarea>
              </mat-form-field>
            </form>
          </mat-card-content>
        </mat-card>

        <!-- BPMN Editor -->
        <div class="editor-card">
          <app-bpmn-editor
            #bpmnEditor
            [bpmnXml]="currentBpmnXml"
            (bpmnChange)="onBpmnChange($event)"
            (saveRequested)="saveProcess()">
          </app-bpmn-editor>
        </div>
      </div>

      <!-- Loading Spinner -->
      <div class="loading-overlay" *ngIf="isLoading">
        <mat-spinner></mat-spinner>
      </div>
    </div>
  `,
  styles: [`
    .process-editor-container {
      display: flex;
      flex-direction: column;
      height: 100vh;
      overflow: hidden;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 16px 24px;
      background: #ffffff;
      border-bottom: 1px solid #e0e0e0;
      z-index: 100;
    }

    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;
    }

    .header-left h1 {
      margin: 0;
      font-size: 24px;
      font-weight: 500;
    }

    .header-right {
      display: flex;
      gap: 16px;
    }

    .header-right button {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .content {
      display: flex;
      flex-direction: column;
      flex: 1;
      overflow: hidden;
    }

    .metadata-card {
      margin: 16px 24px 0;
    }

    .metadata-form {
      display: grid;
      grid-template-columns: 1fr 1fr 1fr;
      gap: 16px;
    }

    .metadata-form .full-width {
      grid-column: 1 / -1;
    }

    .editor-card {
      flex: 1;
      margin: 16px 24px 24px;
      background: #ffffff;
      border: 1px solid #e0e0e0;
      border-radius: 4px;
      overflow: hidden;
      display: flex;
      flex-direction: column;
    }

    .loading-overlay {
      position: fixed;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(0, 0, 0, 0.3);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 1000;
    }

    app-bpmn-editor {
      flex: 1;
      display: flex;
      flex-direction: column;
    }
  `]
})
export class ProcessEditorComponent implements OnInit {
  @ViewChild('bpmnEditor') bpmnEditor!: BpmnEditorComponent;

  processForm: FormGroup;
  categories: any[] = [];
  currentBpmnXml = '';
  isEditMode = false;
  isLoading = false;
  processId?: number;

  constructor(
    private fb: FormBuilder,
    private route: ActivatedRoute,
    private router: Router,
    private processDefinitionService: ProcessDefinitionService,
    private processCategoryService: ProcessCategoryService,
    private snackBar: MatSnackBar
  ) {
    this.processForm = this.fb.group({
      name: ['', Validators.required],
      key: ['', [Validators.required, Validators.pattern(/^[a-z0-9-]+$/)]],
      categoryId: [null, Validators.required],
      description: ['']
    });
  }

  ngOnInit(): void {
    this.loadCategories();

    // Check if editing existing process
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEditMode = true;
      this.processId = parseInt(id, 10);
      this.loadProcess(this.processId);
    }
  }

  loadCategories(): void {
    this.processCategoryService.getAllCategories(0, 100).subscribe({
      next: (response) => {
        this.categories = response.content || [];
        if (this.categories.length === 0) {
          this.loadMockCategories();
        }
      },
      error: () => {
        this.loadMockCategories();
      }
    });
  }

  loadMockCategories(): void {
    this.categories = [
      { id: 1, name: 'Customer Onboarding', code: 'ONBOARDING' },
      { id: 2, name: 'Loan Processing', code: 'LOAN' },
      { id: 3, name: 'Account Management', code: 'ACCOUNT' },
      { id: 4, name: 'Compliance', code: 'COMPLIANCE' }
    ];
  }

  loadProcess(id: number): void {
    this.isLoading = true;
    this.processDefinitionService.getProcessById(id).subscribe({
      next: (process) => {
        this.processForm.patchValue({
          name: process.name,
          key: process.key,
          categoryId: process.categoryId,
          description: process.description
        });
        this.currentBpmnXml = process.bpmnXml || '';
        this.isLoading = false;
      },
      error: () => {
        this.snackBar.open('Failed to load process', 'Close', { duration: 3000 });
        this.isLoading = false;
      }
    });
  }

  onBpmnChange(xml: string): void {
    this.currentBpmnXml = xml;
  }

  isFormValid(): boolean {
    return this.processForm.valid && this.currentBpmnXml.length > 0;
  }

  async saveProcess(): Promise<void> {
    if (!this.isFormValid()) {
      this.snackBar.open('Please fill all required fields', 'Close', { duration: 3000 });
      return;
    }

    this.isLoading = true;

    try {
      // Get current BPMN XML from editor
      const bpmnXml = await this.bpmnEditor.getXml();

      const processData = {
        ...this.processForm.value,
        bpmnXml: bpmnXml
      };

      if (this.isEditMode && this.processId) {
        // Update existing process
        this.processDefinitionService.updateProcess(this.processId, processData).subscribe({
          next: () => {
            this.snackBar.open('Process updated successfully', 'Close', { duration: 3000 });
            this.isLoading = false;
            this.goBack();
          },
          error: () => {
            this.snackBar.open('Failed to update process', 'Close', { duration: 3000 });
            this.isLoading = false;
          }
        });
      } else {
        // Create new process
        this.processDefinitionService.createProcess(processData).subscribe({
          next: () => {
            this.snackBar.open('Process created successfully', 'Close', { duration: 3000 });
            this.isLoading = false;
            this.goBack();
          },
          error: () => {
            this.snackBar.open('Failed to create process', 'Close', { duration: 3000 });
            this.isLoading = false;
          }
        });
      }
    } catch (error) {
      this.snackBar.open('Failed to save process', 'Close', { duration: 3000 });
      this.isLoading = false;
    }
  }

  goBack(): void {
    this.router.navigate(['/processes']);
  }
}
