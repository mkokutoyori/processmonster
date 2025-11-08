import { Component, Input, Output, EventEmitter, OnInit, OnChanges, OnDestroy, SimpleChanges } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, FormControl, ReactiveFormsModule, Validators, AbstractControl } from '@angular/forms';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatRadioModule } from '@angular/material/radio';
import { MatDatepickerModule } from '@angular/material/datepicker';
import { MatNativeDateModule } from '@angular/material/core';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { Subject } from 'rxjs';
import { takeUntil } from 'rxjs/operators';

interface FieldCondition {
  fieldKey: string;
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
  options?: string[];
  defaultValue?: any;
  validation?: {
    minLength?: number;
    maxLength?: number;
    min?: number;
    max?: number;
    pattern?: string;
  };
  conditions?: FieldCondition[];
  order: number;
}

interface FormDefinition {
  id?: number;
  name: string;
  key: string;
  description?: string;
  fields: FormField[];
}

/**
 * Form Renderer Component
 * Dynamically renders forms based on form definition
 */
@Component({
  selector: 'app-form-renderer',
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
    MatRadioModule,
    MatDatepickerModule,
    MatNativeDateModule,
    MatSnackBarModule
  ],
  template: `
    <div class="form-renderer-container">
      <mat-card *ngIf="formDefinition">
        <mat-card-header>
          <mat-card-title>{{ formDefinition.name }}</mat-card-title>
          <mat-card-subtitle *ngIf="formDefinition.description">
            {{ formDefinition.description }}
          </mat-card-subtitle>
        </mat-card-header>

        <mat-card-content>
          <form [formGroup]="dynamicForm" (ngSubmit)="onSubmit()" *ngIf="dynamicForm">
            <div class="form-fields">
              <div *ngFor="let field of sortedFields" class="form-field" [hidden]="!isFieldVisible(field)">
                <!-- Text Input -->
                <mat-form-field appearance="outline" class="full-width"
                                *ngIf="field.type === 'text' || field.type === 'email'">
                  <mat-label>
                    {{ field.label }}
                    <span *ngIf="field.required" class="required-star">*</span>
                  </mat-label>
                  <input matInput
                         [type]="field.type"
                         [formControlName]="field.key"
                         [placeholder]="field.placeholder || ''">
                  <mat-error *ngIf="getControl(field.key)?.hasError('required')">
                    {{ field.label }} is required
                  </mat-error>
                  <mat-error *ngIf="getControl(field.key)?.hasError('email')">
                    Invalid email format
                  </mat-error>
                  <mat-error *ngIf="getControl(field.key)?.hasError('minlength')">
                    Minimum {{ field.validation?.minLength }} characters required
                  </mat-error>
                  <mat-error *ngIf="getControl(field.key)?.hasError('maxlength')">
                    Maximum {{ field.validation?.maxLength }} characters allowed
                  </mat-error>
                  <mat-error *ngIf="getControl(field.key)?.hasError('pattern')">
                    Invalid format
                  </mat-error>
                </mat-form-field>

                <!-- Number Input -->
                <mat-form-field appearance="outline" class="full-width" *ngIf="field.type === 'number'">
                  <mat-label>
                    {{ field.label }}
                    <span *ngIf="field.required" class="required-star">*</span>
                  </mat-label>
                  <input matInput
                         type="number"
                         [formControlName]="field.key"
                         [placeholder]="field.placeholder || ''">
                  <mat-error *ngIf="getControl(field.key)?.hasError('required')">
                    {{ field.label }} is required
                  </mat-error>
                  <mat-error *ngIf="getControl(field.key)?.hasError('min')">
                    Minimum value is {{ field.validation?.min }}
                  </mat-error>
                  <mat-error *ngIf="getControl(field.key)?.hasError('max')">
                    Maximum value is {{ field.validation?.max }}
                  </mat-error>
                </mat-form-field>

                <!-- Date Input -->
                <mat-form-field appearance="outline" class="full-width" *ngIf="field.type === 'date'">
                  <mat-label>
                    {{ field.label }}
                    <span *ngIf="field.required" class="required-star">*</span>
                  </mat-label>
                  <input matInput
                         [matDatepicker]="picker"
                         [formControlName]="field.key">
                  <mat-datepicker-toggle matSuffix [for]="picker"></mat-datepicker-toggle>
                  <mat-datepicker #picker></mat-datepicker>
                  <mat-error *ngIf="getControl(field.key)?.hasError('required')">
                    {{ field.label }} is required
                  </mat-error>
                </mat-form-field>

                <!-- Select Dropdown -->
                <mat-form-field appearance="outline" class="full-width" *ngIf="field.type === 'select'">
                  <mat-label>
                    {{ field.label }}
                    <span *ngIf="field.required" class="required-star">*</span>
                  </mat-label>
                  <mat-select [formControlName]="field.key">
                    <mat-option *ngFor="let option of field.options" [value]="option">
                      {{ option }}
                    </mat-option>
                  </mat-select>
                  <mat-error *ngIf="getControl(field.key)?.hasError('required')">
                    {{ field.label }} is required
                  </mat-error>
                </mat-form-field>

                <!-- Textarea -->
                <mat-form-field appearance="outline" class="full-width" *ngIf="field.type === 'textarea'">
                  <mat-label>
                    {{ field.label }}
                    <span *ngIf="field.required" class="required-star">*</span>
                  </mat-label>
                  <textarea matInput
                            rows="4"
                            [formControlName]="field.key"
                            [placeholder]="field.placeholder || ''"></textarea>
                  <mat-error *ngIf="getControl(field.key)?.hasError('required')">
                    {{ field.label }} is required
                  </mat-error>
                  <mat-error *ngIf="getControl(field.key)?.hasError('minlength')">
                    Minimum {{ field.validation?.minLength }} characters required
                  </mat-error>
                  <mat-error *ngIf="getControl(field.key)?.hasError('maxlength')">
                    Maximum {{ field.validation?.maxLength }} characters allowed
                  </mat-error>
                </mat-form-field>

                <!-- Checkbox -->
                <div class="checkbox-field" *ngIf="field.type === 'checkbox'">
                  <mat-checkbox [formControlName]="field.key">
                    {{ field.label }}
                    <span *ngIf="field.required" class="required-star">*</span>
                  </mat-checkbox>
                  <mat-error *ngIf="getControl(field.key)?.hasError('required') && getControl(field.key)?.touched">
                    {{ field.label }} is required
                  </mat-error>
                </div>

                <!-- Radio Group -->
                <div class="radio-field" *ngIf="field.type === 'radio'">
                  <label class="radio-label">
                    {{ field.label }}
                    <span *ngIf="field.required" class="required-star">*</span>
                  </label>
                  <mat-radio-group [formControlName]="field.key">
                    <mat-radio-button *ngFor="let option of field.options" [value]="option">
                      {{ option }}
                    </mat-radio-button>
                  </mat-radio-group>
                  <mat-error *ngIf="getControl(field.key)?.hasError('required') && getControl(field.key)?.touched">
                    {{ field.label }} is required
                  </mat-error>
                </div>
              </div>
            </div>

            <!-- Form Actions -->
            <div class="form-actions">
              <button mat-raised-button color="primary" type="submit" [disabled]="!dynamicForm.valid || isSubmitting">
                <mat-icon>{{ submitIcon }}</mat-icon>
                {{ isSubmitting ? 'Submitting...' : submitButtonText }}
              </button>
              <button mat-button type="button" (click)="onCancel()" *ngIf="showCancelButton">
                Cancel
              </button>
              <button mat-button type="button" (click)="onReset()" *ngIf="showResetButton">
                Reset
              </button>
            </div>

            <!-- Debug Info (Development Only) -->
            <div class="debug-info" *ngIf="debugMode">
              <h4>Form Values (Debug)</h4>
              <pre>{{ dynamicForm.value | json }}</pre>
              <h4>Form Valid: {{ dynamicForm.valid }}</h4>
              <h4>Form Touched: {{ dynamicForm.touched }}</h4>
            </div>
          </form>
        </mat-card-content>
      </mat-card>

      <!-- Loading State -->
      <mat-card *ngIf="!formDefinition && !errorMessage">
        <mat-card-content>
          <div class="loading-state">
            <mat-icon>hourglass_empty</mat-icon>
            <p>Loading form...</p>
          </div>
        </mat-card-content>
      </mat-card>

      <!-- Error State -->
      <mat-card *ngIf="errorMessage">
        <mat-card-content>
          <div class="error-state">
            <mat-icon color="warn">error</mat-icon>
            <p>{{ errorMessage }}</p>
          </div>
        </mat-card-content>
      </mat-card>
    </div>
  `,
  styles: [`
    .form-renderer-container {
      padding: 24px;
      max-width: 800px;
      margin: 0 auto;
    }

    .form-fields {
      display: flex;
      flex-direction: column;
      gap: 16px;
      margin: 24px 0;
    }

    .form-field {
      width: 100%;
    }

    .full-width {
      width: 100%;
    }

    .required-star {
      color: #f44336;
      margin-left: 4px;
    }

    .checkbox-field {
      padding: 8px 0;
    }

    .radio-field {
      padding: 8px 0;
    }

    .radio-label {
      display: block;
      margin-bottom: 12px;
      color: rgba(0, 0, 0, 0.87);
      font-size: 14px;
      font-weight: 500;
    }

    .radio-field mat-radio-group {
      display: flex;
      flex-direction: column;
      gap: 12px;
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

    .debug-info {
      margin-top: 32px;
      padding: 16px;
      background: #f5f5f5;
      border-radius: 4px;
      font-family: monospace;
      font-size: 12px;
    }

    .debug-info h4 {
      margin: 8px 0;
      color: #666;
    }

    .debug-info pre {
      background: #fff;
      padding: 12px;
      border-radius: 4px;
      overflow-x: auto;
    }

    .loading-state,
    .error-state {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      padding: 64px 24px;
      text-align: center;
    }

    .loading-state mat-icon,
    .error-state mat-icon {
      font-size: 64px;
      width: 64px;
      height: 64px;
      margin-bottom: 16px;
    }

    .loading-state mat-icon {
      color: #1976d2;
    }
  `]
})
export class FormRendererComponent implements OnInit, OnChanges, OnDestroy {
  @Input() formDefinition: FormDefinition | null = null;
  @Input() initialValues: any = {};
  @Input() submitButtonText = 'Submit';
  @Input() submitIcon = 'send';
  @Input() showCancelButton = false;
  @Input() showResetButton = true;
  @Input() debugMode = false;

  @Output() formSubmit = new EventEmitter<any>();
  @Output() formCancel = new EventEmitter<void>();

  dynamicForm!: FormGroup;
  sortedFields: FormField[] = [];
  isSubmitting = false;
  errorMessage = '';

  // Conditional visibility tracking
  private destroy$ = new Subject<void>();
  fieldVisibility = new Map<string, boolean>();

  constructor(
    private fb: FormBuilder,
    private snackBar: MatSnackBar
  ) {}

  ngOnInit(): void {
    if (this.formDefinition) {
      this.buildForm();
    }
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['formDefinition'] && !changes['formDefinition'].firstChange) {
      this.buildForm();
    }
    if (changes['initialValues'] && !changes['initialValues'].firstChange) {
      this.patchFormValues();
    }
  }

  ngOnDestroy(): void {
    this.destroy$.next();
    this.destroy$.complete();
  }

  buildForm(): void {
    if (!this.formDefinition) {
      this.errorMessage = 'No form definition provided';
      return;
    }

    this.sortedFields = [...this.formDefinition.fields].sort((a, b) => a.order - b.order);

    const group: any = {};

    this.sortedFields.forEach(field => {
      const validators = this.buildValidators(field);
      const defaultValue = this.getDefaultValue(field);

      group[field.key] = new FormControl(
        { value: defaultValue, disabled: field.disabled },
        validators
      );

      // Initialize field visibility
      this.fieldVisibility.set(field.key, true);
    });

    this.dynamicForm = this.fb.group(group);

    // Patch initial values if provided
    if (this.initialValues && Object.keys(this.initialValues).length > 0) {
      this.patchFormValues();
    }

    // Setup conditional visibility listeners
    this.setupConditionalVisibility();
  }

  buildValidators(field: FormField): any[] {
    const validators: any[] = [];

    if (field.required) {
      validators.push(Validators.required);
    }

    if (field.type === 'email') {
      validators.push(Validators.email);
    }

    if (field.validation) {
      if (field.validation.minLength) {
        validators.push(Validators.minLength(field.validation.minLength));
      }
      if (field.validation.maxLength) {
        validators.push(Validators.maxLength(field.validation.maxLength));
      }
      if (field.validation.min !== undefined) {
        validators.push(Validators.min(field.validation.min));
      }
      if (field.validation.max !== undefined) {
        validators.push(Validators.max(field.validation.max));
      }
      if (field.validation.pattern) {
        validators.push(Validators.pattern(field.validation.pattern));
      }
    }

    return validators;
  }

  getDefaultValue(field: FormField): any {
    if (field.defaultValue !== undefined) {
      return field.defaultValue;
    }

    switch (field.type) {
      case 'checkbox':
        return false;
      case 'number':
        return null;
      case 'select':
      case 'radio':
        return '';
      default:
        return '';
    }
  }

  patchFormValues(): void {
    if (this.dynamicForm && this.initialValues) {
      this.dynamicForm.patchValue(this.initialValues);
    }
  }

  getControl(key: string): AbstractControl | null {
    return this.dynamicForm?.get(key) || null;
  }

  onSubmit(): void {
    if (this.dynamicForm.valid) {
      this.isSubmitting = true;
      const formValues = this.dynamicForm.getRawValue(); // getRawValue includes disabled fields
      this.formSubmit.emit(formValues);

      // Reset submitting state after a delay (parent component should handle this)
      setTimeout(() => {
        this.isSubmitting = false;
      }, 1000);
    } else {
      this.dynamicForm.markAllAsTouched();
      this.snackBar.open('Please fill all required fields', 'Close', { duration: 3000 });
    }
  }

  onCancel(): void {
    this.formCancel.emit();
  }

  onReset(): void {
    if (confirm('Reset form to default values?')) {
      this.dynamicForm.reset();
      if (this.initialValues && Object.keys(this.initialValues).length > 0) {
        this.patchFormValues();
      }
      this.snackBar.open('Form reset', 'Close', { duration: 2000 });
    }
  }

  // Conditional Visibility Methods
  setupConditionalVisibility(): void {
    // Listen to form value changes
    this.dynamicForm.valueChanges
      .pipe(takeUntil(this.destroy$))
      .subscribe(() => {
        this.updateFieldVisibility();
      });

    // Initial visibility check
    this.updateFieldVisibility();
  }

  updateFieldVisibility(): void {
    this.sortedFields.forEach(field => {
      if (field.conditions && field.conditions.length > 0) {
        // Field has conditions - evaluate them
        const isVisible = this.evaluateConditions(field.conditions);
        this.fieldVisibility.set(field.key, isVisible);

        // If field becomes invisible, clear its value and disable validation
        const control = this.getControl(field.key);
        if (!isVisible && control) {
          control.setValue(null, { emitEvent: false });
          control.clearValidators();
          control.updateValueAndValidity({ emitEvent: false });
        } else if (isVisible && control) {
          // Re-apply validators when field becomes visible
          const validators = this.buildValidators(field);
          control.setValidators(validators);
          control.updateValueAndValidity({ emitEvent: false });
        }
      } else {
        // No conditions - always visible
        this.fieldVisibility.set(field.key, true);
      }
    });
  }

  evaluateConditions(conditions: FieldCondition[]): boolean {
    // All conditions must be true (AND logic)
    return conditions.every(condition => this.evaluateCondition(condition));
  }

  evaluateCondition(condition: FieldCondition): boolean {
    const control = this.getControl(condition.fieldKey);
    if (!control) {
      return false;
    }

    const fieldValue = control.value;

    switch (condition.operator) {
      case 'equals':
        return fieldValue == condition.value;

      case 'notEquals':
        return fieldValue != condition.value;

      case 'contains':
        if (typeof fieldValue === 'string' && typeof condition.value === 'string') {
          return fieldValue.toLowerCase().includes(condition.value.toLowerCase());
        }
        return false;

      case 'greaterThan':
        return Number(fieldValue) > Number(condition.value);

      case 'lessThan':
        return Number(fieldValue) < Number(condition.value);

      case 'isEmpty':
        return !fieldValue || fieldValue === '' || fieldValue === null || fieldValue === undefined;

      case 'isNotEmpty':
        return fieldValue && fieldValue !== '' && fieldValue !== null && fieldValue !== undefined;

      default:
        return false;
    }
  }

  isFieldVisible(field: FormField): boolean {
    return this.fieldVisibility.get(field.key) ?? true;
  }
}
