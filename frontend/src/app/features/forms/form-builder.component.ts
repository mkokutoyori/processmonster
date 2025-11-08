import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatChipsModule } from '@angular/material/chips';
import { MatListModule } from '@angular/material/list';
import { MatDividerModule } from '@angular/material/divider';
import { MatExpansionModule } from '@angular/material/expansion';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { CdkDragDrop, DragDropModule, moveItemInArray } from '@angular/cdk/drag-drop';
import { FormService } from '../../core/services/form.service';
import { Subject } from 'rxjs';
import { debounceTime, distinctUntilChanged } from 'rxjs/operators';

interface FieldCondition {
  fieldKey: string;  // Field to watch
  operator: 'equals' | 'notEquals' | 'contains' | 'greaterThan' | 'lessThan' | 'isEmpty' | 'isNotEmpty';
  value?: any;
}

interface FormField {
  id: string;
  type: 'text' | 'number' | 'email' | 'date' | 'select' | 'checkbox' | 'textarea' | 'radio';
  label: string;
  key: string;
  placeholder?: string;
  required: boolean;
  disabled: boolean;
  options?: string[]; // For select/radio
  defaultValue?: any;
  validation?: {
    minLength?: number;
    maxLength?: number;
    min?: number;
    max?: number;
    pattern?: string;
  };
  conditions?: FieldCondition[];  // Conditional visibility
  order: number;
}

/**
 * Form Builder Component
 * Allows creating dynamic forms with drag-and-drop interface
 */
@Component({
  selector: 'app-form-builder',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatCheckboxModule,
    MatSlideToggleModule,
    MatChipsModule,
    MatListModule,
    MatDividerModule,
    MatExpansionModule,
    MatSnackBarModule,
    MatTooltipModule,
    DragDropModule
  ],
  template: `
    <div class="form-builder-container">
      <div class="header">
        <div>
          <button mat-icon-button (click)="goBack()" matTooltip="Back">
            <mat-icon>arrow_back</mat-icon>
          </button>
          <h1>{{ isEditMode ? 'Edit Form' : 'Create Form' }}</h1>
        </div>
        <div class="header-actions">
          <button mat-button (click)="previewForm()">
            <mat-icon>visibility</mat-icon>
            Preview
          </button>
          <button mat-raised-button color="primary" (click)="saveForm()" [disabled]="formDefinitionForm.invalid">
            <mat-icon>save</mat-icon>
            Save Form
          </button>
        </div>
      </div>

      <div class="builder-layout">
        <!-- Left Panel: Field Palette -->
        <mat-card class="palette-panel">
          <mat-card-header>
            <mat-card-title>Field Types</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="field-palette">
              <button mat-stroked-button class="palette-item" (click)="addField('text')">
                <mat-icon>text_fields</mat-icon>
                <span>Text Input</span>
              </button>
              <button mat-stroked-button class="palette-item" (click)="addField('number')">
                <mat-icon>pin</mat-icon>
                <span>Number</span>
              </button>
              <button mat-stroked-button class="palette-item" (click)="addField('email')">
                <mat-icon>email</mat-icon>
                <span>Email</span>
              </button>
              <button mat-stroked-button class="palette-item" (click)="addField('date')">
                <mat-icon>calendar_today</mat-icon>
                <span>Date</span>
              </button>
              <button mat-stroked-button class="palette-item" (click)="addField('select')">
                <mat-icon>arrow_drop_down_circle</mat-icon>
                <span>Dropdown</span>
              </button>
              <button mat-stroked-button class="palette-item" (click)="addField('checkbox')">
                <mat-icon>check_box</mat-icon>
                <span>Checkbox</span>
              </button>
              <button mat-stroked-button class="palette-item" (click)="addField('textarea')">
                <mat-icon>notes</mat-icon>
                <span>Text Area</span>
              </button>
              <button mat-stroked-button class="palette-item" (click)="addField('radio')">
                <mat-icon>radio_button_checked</mat-icon>
                <span>Radio Group</span>
              </button>
            </div>
          </mat-card-content>
        </mat-card>

        <!-- Center Panel: Form Definition -->
        <div class="main-panel">
          <mat-card class="form-info-card">
            <mat-card-content>
              <form [formGroup]="formDefinitionForm">
                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Form Name</mat-label>
                  <input matInput formControlName="name" required>
                  <mat-error>Form name is required</mat-error>
                </mat-form-field>

                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Form Key</mat-label>
                  <input matInput formControlName="key" required placeholder="unique-form-key">
                  <mat-hint>Unique identifier (lowercase, hyphens)</mat-hint>
                  <mat-error>Form key is required</mat-error>
                </mat-form-field>

                <mat-form-field appearance="outline" class="full-width">
                  <mat-label>Description</mat-label>
                  <textarea matInput formControlName="description" rows="2"></textarea>
                </mat-form-field>
              </form>
            </mat-card-content>
          </mat-card>

          <!-- Form Fields Builder -->
          <mat-card class="fields-card">
            <mat-card-header>
              <mat-card-title>Form Fields</mat-card-title>
              <mat-card-subtitle>{{ fields.length }} field(s)</mat-card-subtitle>
            </mat-card-header>
            <mat-card-content>
              <div class="empty-state" *ngIf="fields.length === 0">
                <mat-icon>add_circle_outline</mat-icon>
                <p>No fields added yet</p>
                <p class="hint">Click on field types to add them to your form</p>
              </div>

              <div cdkDropList class="fields-list" (cdkDropListDropped)="onFieldDrop($event)">
                <mat-expansion-panel *ngFor="let field of fields; let i = index"
                                     cdkDrag class="field-item">
                  <mat-expansion-panel-header>
                    <mat-panel-title>
                      <mat-icon cdkDragHandle class="drag-handle">drag_indicator</mat-icon>
                      <mat-icon class="field-type-icon">{{ getFieldIcon(field.type) }}</mat-icon>
                      <span>{{ field.label || 'Untitled Field' }}</span>
                      <mat-chip *ngIf="field.required" class="required-chip">Required</mat-chip>
                    </mat-panel-title>
                    <mat-panel-description>
                      <span class="field-type">{{ field.type }}</span>
                      <button mat-icon-button (click)="deleteField(i); $event.stopPropagation()"
                              matTooltip="Delete field">
                        <mat-icon color="warn">delete</mat-icon>
                      </button>
                    </mat-panel-description>
                  </mat-expansion-panel-header>

                  <!-- Field Configuration -->
                  <div class="field-config">
                    <mat-form-field appearance="outline" class="full-width">
                      <mat-label>Label</mat-label>
                      <input matInput [(ngModel)]="field.label" (ngModelChange)="updateFieldKey(field)">
                    </mat-form-field>

                    <mat-form-field appearance="outline" class="full-width">
                      <mat-label>Field Key</mat-label>
                      <input matInput [(ngModel)]="field.key">
                      <mat-hint>Variable name for this field</mat-hint>
                    </mat-form-field>

                    <mat-form-field appearance="outline" class="full-width"
                                    *ngIf="field.type !== 'checkbox'">
                      <mat-label>Placeholder</mat-label>
                      <input matInput [(ngModel)]="field.placeholder">
                    </mat-form-field>

                    <div class="field-options">
                      <mat-slide-toggle [(ngModel)]="field.required">
                        Required
                      </mat-slide-toggle>
                      <mat-slide-toggle [(ngModel)]="field.disabled">
                        Disabled
                      </mat-slide-toggle>
                    </div>

                    <!-- Options for Select/Radio -->
                    <div class="options-section" *ngIf="field.type === 'select' || field.type === 'radio'">
                      <label>Options (one per line)</label>
                      <textarea class="options-textarea"
                                [value]="field.options?.join('\\n')"
                                (input)="updateFieldOptions(field, $event)"
                                placeholder="Option 1&#10;Option 2&#10;Option 3"></textarea>
                    </div>

                    <!-- Conditional Visibility -->
                    <mat-expansion-panel class="conditional-panel">
                      <mat-expansion-panel-header>
                        <mat-panel-title>
                          <mat-icon>visibility</mat-icon>
                          Conditional Visibility
                          <mat-chip *ngIf="field.conditions && field.conditions.length > 0" class="condition-badge">
                            {{ field.conditions.length }}
                          </mat-chip>
                        </mat-panel-title>
                      </mat-expansion-panel-header>

                      <div class="conditional-config">
                        <p class="hint">Show this field only when conditions are met</p>

                        <div *ngFor="let condition of field.conditions || []; let condIndex = index" class="condition-item">
                          <mat-form-field appearance="outline">
                            <mat-label>When field</mat-label>
                            <mat-select [(ngModel)]="condition.fieldKey">
                              <mat-option *ngFor="let otherField of getAvailableFields(field)" [value]="otherField.key">
                                {{ otherField.label }}
                              </mat-option>
                            </mat-select>
                          </mat-form-field>

                          <mat-form-field appearance="outline">
                            <mat-label>Operator</mat-label>
                            <mat-select [(ngModel)]="condition.operator">
                              <mat-option value="equals">Equals</mat-option>
                              <mat-option value="notEquals">Not Equals</mat-option>
                              <mat-option value="contains">Contains</mat-option>
                              <mat-option value="greaterThan">Greater Than</mat-option>
                              <mat-option value="lessThan">Less Than</mat-option>
                              <mat-option value="isEmpty">Is Empty</mat-option>
                              <mat-option value="isNotEmpty">Is Not Empty</mat-option>
                            </mat-select>
                          </mat-form-field>

                          <mat-form-field appearance="outline" *ngIf="!['isEmpty', 'isNotEmpty'].includes(condition.operator)">
                            <mat-label>Value</mat-label>
                            <input matInput [(ngModel)]="condition.value">
                          </mat-form-field>

                          <button mat-icon-button color="warn" (click)="removeCondition(field, condIndex)" matTooltip="Remove condition">
                            <mat-icon>delete</mat-icon>
                          </button>
                        </div>

                        <button mat-stroked-button (click)="addCondition(field)">
                          <mat-icon>add</mat-icon>
                          Add Condition
                        </button>
                      </div>
                    </mat-expansion-panel>

                    <!-- Validation Rules -->
                    <mat-expansion-panel class="validation-panel">
                      <mat-expansion-panel-header>
                        <mat-panel-title>Validation Rules</mat-panel-title>
                      </mat-expansion-panel-header>

                      <div class="validation-config">
                        <mat-form-field appearance="outline" *ngIf="field.type === 'text' || field.type === 'textarea'">
                          <mat-label>Min Length</mat-label>
                          <input matInput type="number" [(ngModel)]="field.validation!.minLength">
                        </mat-form-field>

                        <mat-form-field appearance="outline" *ngIf="field.type === 'text' || field.type === 'textarea'">
                          <mat-label>Max Length</mat-label>
                          <input matInput type="number" [(ngModel)]="field.validation!.maxLength">
                        </mat-form-field>

                        <mat-form-field appearance="outline" *ngIf="field.type === 'number'">
                          <mat-label>Min Value</mat-label>
                          <input matInput type="number" [(ngModel)]="field.validation!.min">
                        </mat-form-field>

                        <mat-form-field appearance="outline" *ngIf="field.type === 'number'">
                          <mat-label>Max Value</mat-label>
                          <input matInput type="number" [(ngModel)]="field.validation!.max">
                        </mat-form-field>

                        <mat-form-field appearance="outline" *ngIf="field.type === 'text'" class="full-width">
                          <mat-label>Pattern (RegEx)</mat-label>
                          <input matInput [(ngModel)]="field.validation!.pattern">
                          <mat-hint>Regular expression for validation</mat-hint>
                        </mat-form-field>
                      </div>
                    </mat-expansion-panel>
                  </div>
                </mat-expansion-panel>
              </div>
            </mat-card-content>
          </mat-card>
        </div>

        <!-- Right Panel: Preview -->
        <mat-card class="preview-panel">
          <mat-card-header>
            <mat-card-title>Preview</mat-card-title>
          </mat-card-header>
          <mat-card-content>
            <div class="form-preview">
              <h3>{{ formDefinitionForm.get('name')?.value || 'Untitled Form' }}</h3>
              <p class="preview-description">{{ formDefinitionForm.get('description')?.value }}</p>

              <mat-divider></mat-divider>

              <div class="preview-fields">
                <div *ngFor="let field of fields" class="preview-field">
                  <!-- Text Input Preview -->
                  <mat-form-field appearance="outline" class="full-width"
                                  *ngIf="field.type === 'text' || field.type === 'email'">
                    <mat-label>{{ field.label }}<span *ngIf="field.required" class="required-star">*</span></mat-label>
                    <input matInput [type]="field.type" [placeholder]="field.placeholder || ''" [disabled]="field.disabled">
                  </mat-form-field>

                  <!-- Number Input Preview -->
                  <mat-form-field appearance="outline" class="full-width" *ngIf="field.type === 'number'">
                    <mat-label>{{ field.label }}<span *ngIf="field.required" class="required-star">*</span></mat-label>
                    <input matInput type="number" [placeholder]="field.placeholder || ''" [disabled]="field.disabled">
                  </mat-form-field>

                  <!-- Date Input Preview -->
                  <mat-form-field appearance="outline" class="full-width" *ngIf="field.type === 'date'">
                    <mat-label>{{ field.label }}<span *ngIf="field.required" class="required-star">*</span></mat-label>
                    <input matInput type="date" [disabled]="field.disabled">
                  </mat-form-field>

                  <!-- Select Preview -->
                  <mat-form-field appearance="outline" class="full-width" *ngIf="field.type === 'select'">
                    <mat-label>{{ field.label }}<span *ngIf="field.required" class="required-star">*</span></mat-label>
                    <mat-select [disabled]="field.disabled">
                      <mat-option *ngFor="let option of field.options" [value]="option">
                        {{ option }}
                      </mat-option>
                    </mat-select>
                  </mat-form-field>

                  <!-- Textarea Preview -->
                  <mat-form-field appearance="outline" class="full-width" *ngIf="field.type === 'textarea'">
                    <mat-label>{{ field.label }}<span *ngIf="field.required" class="required-star">*</span></mat-label>
                    <textarea matInput rows="3" [placeholder]="field.placeholder || ''" [disabled]="field.disabled"></textarea>
                  </mat-form-field>

                  <!-- Checkbox Preview -->
                  <div *ngIf="field.type === 'checkbox'" class="checkbox-preview">
                    <mat-checkbox [disabled]="field.disabled">
                      {{ field.label }}<span *ngIf="field.required" class="required-star">*</span>
                    </mat-checkbox>
                  </div>

                  <!-- Radio Preview -->
                  <div *ngIf="field.type === 'radio'" class="radio-preview">
                    <label>{{ field.label }}<span *ngIf="field.required" class="required-star">*</span></label>
                    <mat-radio-group>
                      <mat-radio-button *ngFor="let option of field.options" [value]="option" [disabled]="field.disabled">
                        {{ option }}
                      </mat-radio-button>
                    </mat-radio-group>
                  </div>
                </div>
              </div>
            </div>
          </mat-card-content>
        </mat-card>
      </div>
    </div>
  `,
  styles: [`
    .form-builder-container {
      padding: 24px;
    }

    .header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 24px;
    }

    .header > div:first-child {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .header h1 {
      margin: 0;
    }

    .header-actions {
      display: flex;
      gap: 12px;
    }

    .header-actions button {
      display: flex;
      align-items: center;
      gap: 8px;
    }

    .builder-layout {
      display: grid;
      grid-template-columns: 250px 1fr 350px;
      gap: 16px;
      align-items: start;
    }

    /* Palette Panel */
    .palette-panel {
      position: sticky;
      top: 24px;
    }

    .field-palette {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .palette-item {
      display: flex;
      align-items: center;
      gap: 12px;
      justify-content: flex-start;
      padding: 12px;
      text-align: left;
    }

    .palette-item mat-icon {
      color: #1976d2;
    }

    /* Main Panel */
    .main-panel {
      display: flex;
      flex-direction: column;
      gap: 16px;
    }

    .full-width {
      width: 100%;
    }

    .form-info-card {
      margin-bottom: 0;
    }

    /* Fields List */
    .fields-card {
      min-height: 400px;
    }

    .empty-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 64px 24px;
      color: #999;
      text-align: center;
    }

    .empty-state mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      margin-bottom: 16px;
    }

    .empty-state .hint {
      font-size: 12px;
      margin-top: 8px;
    }

    .fields-list {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    .field-item {
      cursor: move;
    }

    .field-item.cdk-drag-preview {
      box-shadow: 0 5px 15px rgba(0,0,0,0.3);
    }

    .field-item.cdk-drag-animating {
      transition: transform 250ms cubic-bezier(0, 0, 0.2, 1);
    }

    .drag-handle {
      cursor: grab;
      margin-right: 8px;
      color: #999;
    }

    .field-type-icon {
      margin-right: 8px;
      color: #1976d2;
    }

    .required-chip {
      margin-left: 8px;
      font-size: 10px;
      min-height: 20px;
      padding: 0 8px;
    }

    .field-type {
      text-transform: capitalize;
      color: #666;
      font-size: 12px;
      margin-right: 8px;
    }

    .field-config {
      padding: 16px 0;
    }

    .field-options {
      display: flex;
      gap: 24px;
      margin: 16px 0;
    }

    .options-section {
      margin: 16px 0;
    }

    .options-section label {
      display: block;
      margin-bottom: 8px;
      color: #666;
      font-size: 14px;
    }

    .options-textarea {
      width: 100%;
      min-height: 100px;
      padding: 12px;
      border: 1px solid #ddd;
      border-radius: 4px;
      font-family: inherit;
      font-size: 14px;
      resize: vertical;
    }

    .conditional-panel {
      margin-top: 16px;
    }

    .conditional-config {
      padding: 16px 0;
    }

    .conditional-config .hint {
      margin: 0 0 16px 0;
      color: #666;
      font-size: 13px;
    }

    .condition-item {
      display: grid;
      grid-template-columns: 1fr 1fr 1fr auto;
      gap: 12px;
      align-items: start;
      margin-bottom: 12px;
      padding: 12px;
      background: #f5f5f5;
      border-radius: 4px;
    }

    .condition-badge {
      margin-left: 8px;
      font-size: 10px;
      min-height: 20px;
      background-color: #4caf50 !important;
      color: white !important;
    }

    .validation-panel {
      margin-top: 16px;
    }

    .validation-config {
      display: grid;
      grid-template-columns: repeat(2, 1fr);
      gap: 16px;
      padding: 16px 0;
    }

    /* Preview Panel */
    .preview-panel {
      position: sticky;
      top: 24px;
      max-height: calc(100vh - 100px);
      overflow-y: auto;
    }

    .form-preview h3 {
      margin: 0 0 8px 0;
      color: #333;
    }

    .preview-description {
      margin: 0 0 16px 0;
      color: #666;
      font-size: 14px;
    }

    .preview-fields {
      margin-top: 24px;
    }

    .preview-field {
      margin-bottom: 16px;
    }

    .required-star {
      color: #f44336;
      margin-left: 4px;
    }

    .checkbox-preview,
    .radio-preview {
      padding: 8px 0;
    }

    .radio-preview label {
      display: block;
      margin-bottom: 12px;
      color: #333;
      font-size: 14px;
      font-weight: 500;
    }

    .radio-preview mat-radio-group {
      display: flex;
      flex-direction: column;
      gap: 8px;
    }

    @media (max-width: 1400px) {
      .builder-layout {
        grid-template-columns: 1fr;
      }

      .palette-panel,
      .preview-panel {
        position: static;
      }
    }
  `]
})
export class FormBuilderComponent implements OnInit, OnDestroy {
  formDefinitionForm: FormGroup;
  fields: FormField[] = [];
  isEditMode = false;
  formId: number | null = null;
  private fieldCounter = 0;
  private autoSaveSubject = new Subject<void>();
  private isAutoSaving = false;

  constructor(
    private fb: FormBuilder,
    private formService: FormService,
    private router: Router,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar
  ) {
    this.formDefinitionForm = this.fb.group({
      name: ['', Validators.required],
      key: ['', Validators.required],
      description: ['']
    });

    // Setup auto-save with debounce
    this.autoSaveSubject.pipe(
      debounceTime(2000),  // Wait 2 seconds after last change
      distinctUntilChanged()
    ).subscribe(() => {
      this.performAutoSave();
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.formId = +params['id'];
        this.isEditMode = true;
        this.loadForm();
      }
    });

    // Watch for form changes to trigger auto-save
    this.formDefinitionForm.valueChanges.subscribe(() => {
      this.triggerAutoSave();
    });
  }

  ngOnDestroy(): void {
    this.autoSaveSubject.complete();
  }

  loadForm(): void {
    if (this.formId) {
      this.formService.getFormById(this.formId).subscribe({
        next: (form) => {
          this.formDefinitionForm.patchValue({
            name: form.name,
            key: form.key,
            description: form.description
          });
          this.fields = form.fields || [];
        },
        error: () => {
          this.snackBar.open('Failed to load form', 'Close', { duration: 3000 });
        }
      });
    }
  }

  addField(type: FormField['type']): void {
    this.fieldCounter++;
    const newField: FormField = {
      id: `field_${this.fieldCounter}`,
      type: type,
      label: `${this.capitalizeFirst(type)} Field`,
      key: `${type}_${this.fieldCounter}`,
      placeholder: '',
      required: false,
      disabled: false,
      options: type === 'select' || type === 'radio' ? ['Option 1', 'Option 2'] : undefined,
      validation: {},
      order: this.fields.length
    };
    this.fields.push(newField);
  }

  deleteField(index: number): void {
    if (confirm('Delete this field?')) {
      this.fields.splice(index, 1);
      this.reorderFields();
    }
  }

  onFieldDrop(event: CdkDragDrop<FormField[]>): void {
    moveItemInArray(this.fields, event.previousIndex, event.currentIndex);
    this.reorderFields();
  }

  reorderFields(): void {
    this.fields.forEach((field, index) => {
      field.order = index;
    });
  }

  updateFieldKey(field: FormField): void {
    if (field.label) {
      field.key = field.label
        .toLowerCase()
        .replace(/[^a-z0-9]+/g, '_')
        .replace(/^_|_$/g, '');
    }
  }

  updateFieldOptions(field: FormField, event: any): void {
    const text = event.target.value;
    field.options = text.split('\n').filter((opt: string) => opt.trim());
  }

  getFieldIcon(type: string): string {
    const icons: { [key: string]: string } = {
      'text': 'text_fields',
      'number': 'pin',
      'email': 'email',
      'date': 'calendar_today',
      'select': 'arrow_drop_down_circle',
      'checkbox': 'check_box',
      'textarea': 'notes',
      'radio': 'radio_button_checked'
    };
    return icons[type] || 'help';
  }

  capitalizeFirst(str: string): string {
    return str.charAt(0).toUpperCase() + str.slice(1);
  }

  previewForm(): void {
    // TODO: Open preview in dialog or new route
    this.snackBar.open('Preview in right panel', 'Close', { duration: 2000 });
  }

  saveForm(): void {
    if (this.formDefinitionForm.valid) {
      const formData = {
        ...this.formDefinitionForm.value,
        fields: this.fields
      };

      const request = this.isEditMode && this.formId
        ? this.formService.updateForm(this.formId, formData)
        : this.formService.createForm(formData);

      request.subscribe({
        next: () => {
          this.snackBar.open('Form saved successfully!', 'Close', { duration: 3000 });
          this.router.navigate(['/forms']);
        },
        error: () => {
          this.snackBar.open('Failed to save form', 'Close', { duration: 3000 });
        }
      });
    }
  }

  goBack(): void {
    this.router.navigate(['/forms']);
  }

  // Conditional Fields Methods
  addCondition(field: FormField): void {
    if (!field.conditions) {
      field.conditions = [];
    }
    field.conditions.push({
      fieldKey: '',
      operator: 'equals',
      value: ''
    });
    this.triggerAutoSave();
  }

  removeCondition(field: FormField, index: number): void {
    if (field.conditions) {
      field.conditions.splice(index, 1);
      this.triggerAutoSave();
    }
  }

  getAvailableFields(currentField: FormField): FormField[] {
    // Return all fields except the current one
    return this.fields.filter(field => field.id !== currentField.id);
  }

  // Auto-save Methods
  triggerAutoSave(): void {
    if (this.isEditMode && this.formId) {
      this.autoSaveSubject.next();
    }
  }

  performAutoSave(): void {
    if (this.isAutoSaving || !this.formDefinitionForm.valid) {
      return;
    }

    this.isAutoSaving = true;
    const formData = {
      ...this.formDefinitionForm.value,
      fields: this.fields
    };

    if (this.formId) {
      this.formService.updateForm(this.formId, formData).subscribe({
        next: () => {
          this.isAutoSaving = false;
          // Silent auto-save - don't show snackbar for every auto-save
          console.log('Form auto-saved');
        },
        error: () => {
          this.isAutoSaving = false;
          console.error('Auto-save failed');
        }
      });
    }
  }
}
