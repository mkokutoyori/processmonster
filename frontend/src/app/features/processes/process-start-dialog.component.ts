import { Component, Inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormArray } from '@angular/forms';
import { MatDialogModule, MatDialogRef, MAT_DIALOG_DATA } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatIconModule } from '@angular/material/icon';
import { MatChipsModule } from '@angular/material/chips';
import { ProcessDefinition, StartProcessInstanceRequest } from '../../core/models/process.model';

export interface ProcessStartDialogData {
  process: ProcessDefinition;
}

export interface ProcessStartResult {
  businessKey?: string;
  variables: { [key: string]: any };
}

/**
 * Dialog component for starting a process instance
 */
@Component({
  selector: 'app-process-start-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatIconModule,
    MatChipsModule
  ],
  template: `
    <h2 mat-dialog-title>Start Process Instance</h2>

    <mat-dialog-content>
      <div class="process-info">
        <div class="info-row">
          <label>Process:</label>
          <span>{{ data.process.name }}</span>
        </div>
        <div class="info-row">
          <label>Key:</label>
          <code>{{ data.process.processKey }}:v{{ data.process.version }}</code>
        </div>
        <div class="info-row" *ngIf="data.process.description">
          <label>Description:</label>
          <p>{{ data.process.description }}</p>
        </div>
      </div>

      <form [formGroup]="startForm">
        <mat-form-field appearance="outline" class="full-width">
          <mat-label>Business Key (Optional)</mat-label>
          <input matInput formControlName="businessKey" placeholder="e.g., LOAN-12345">
          <mat-icon matSuffix>key</mat-icon>
          <mat-hint>Unique identifier for correlating this instance</mat-hint>
        </mat-form-field>

        <div class="variables-section">
          <div class="section-header">
            <h3>Initial Variables</h3>
            <button mat-icon-button type="button" (click)="addVariable()" matTooltip="Add variable">
              <mat-icon>add</mat-icon>
            </button>
          </div>

          <div formArrayName="variables">
            <div *ngFor="let variableForm of variablesArray.controls; let i = index"
                 [formGroupName]="i"
                 class="variable-row">
              <mat-form-field appearance="outline" class="variable-name">
                <mat-label>Variable Name</mat-label>
                <input matInput formControlName="name" placeholder="e.g., amount">
              </mat-form-field>

              <mat-form-field appearance="outline" class="variable-type">
                <mat-label>Type</mat-label>
                <mat-select formControlName="type">
                  <mat-option value="string">String</mat-option>
                  <mat-option value="number">Number</mat-option>
                  <mat-option value="boolean">Boolean</mat-option>
                  <mat-option value="json">JSON</mat-option>
                </mat-select>
              </mat-form-field>

              <mat-form-field appearance="outline" class="variable-value">
                <mat-label>Value</mat-label>
                <input matInput
                       *ngIf="variableForm.get('type')?.value !== 'boolean'"
                       formControlName="value"
                       [type]="variableForm.get('type')?.value === 'number' ? 'number' : 'text'">
                <mat-select *ngIf="variableForm.get('type')?.value === 'boolean'" formControlName="value">
                  <mat-option [value]="true">True</mat-option>
                  <mat-option [value]="false">False</mat-option>
                </mat-select>
              </mat-form-field>

              <button mat-icon-button type="button" (click)="removeVariable(i)" matTooltip="Remove variable" color="warn">
                <mat-icon>delete</mat-icon>
              </button>
            </div>
          </div>

          <p *ngIf="variablesArray.length === 0" class="no-variables">
            No variables defined. Click + to add initial variables.
          </p>
        </div>
      </form>
    </mat-dialog-content>

    <mat-dialog-actions align="end">
      <button mat-button (click)="onCancel()">Cancel</button>
      <button mat-raised-button color="primary" (click)="onStart()" [disabled]="!canStart()">
        <mat-icon>play_arrow</mat-icon>
        Start Process
      </button>
    </mat-dialog-actions>
  `,
  styles: [`
    mat-dialog-content {
      min-width: 600px;
      max-width: 800px;
      padding: 24px !important;
    }

    .process-info {
      background: #f5f5f5;
      padding: 16px;
      border-radius: 8px;
      margin-bottom: 24px;
    }

    .info-row {
      display: flex;
      gap: 12px;
      margin-bottom: 8px;
    }

    .info-row:last-child {
      margin-bottom: 0;
    }

    .info-row label {
      font-weight: 500;
      color: #666;
      min-width: 100px;
    }

    .info-row span, .info-row p {
      color: #333;
    }

    .info-row p {
      margin: 0;
      line-height: 1.5;
    }

    code {
      background: white;
      padding: 4px 8px;
      border-radius: 4px;
      font-family: 'Courier New', monospace;
      font-size: 14px;
    }

    .full-width {
      width: 100%;
      margin-bottom: 16px;
    }

    .variables-section {
      margin-top: 24px;
    }

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
    }

    .section-header h3 {
      margin: 0;
      font-size: 18px;
      font-weight: 500;
      color: #333;
    }

    .variable-row {
      display: grid;
      grid-template-columns: 2fr 1fr 2fr auto;
      gap: 12px;
      margin-bottom: 12px;
      align-items: start;
    }

    .variable-name, .variable-type, .variable-value {
      margin: 0 !important;
    }

    .no-variables {
      text-align: center;
      color: #999;
      padding: 32px;
      font-style: italic;
      background: #fafafa;
      border-radius: 8px;
      margin: 16px 0;
    }

    mat-dialog-actions {
      padding: 16px 24px !important;
      gap: 12px;
    }

    @media (max-width: 768px) {
      mat-dialog-content {
        min-width: 0;
        width: 100%;
      }

      .variable-row {
        grid-template-columns: 1fr;
        gap: 8px;
      }
    }
  `]
})
export class ProcessStartDialogComponent implements OnInit {
  startForm: FormGroup;

  constructor(
    private fb: FormBuilder,
    public dialogRef: MatDialogRef<ProcessStartDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: ProcessStartDialogData
  ) {
    this.startForm = this.fb.group({
      businessKey: [''],
      variables: this.fb.array([])
    });
  }

  ngOnInit(): void {
    // Add one empty variable by default
    this.addVariable();
  }

  get variablesArray(): FormArray {
    return this.startForm.get('variables') as FormArray;
  }

  addVariable(): void {
    const variableGroup = this.fb.group({
      name: ['', Validators.required],
      type: ['string', Validators.required],
      value: ['', Validators.required]
    });

    this.variablesArray.push(variableGroup);
  }

  removeVariable(index: number): void {
    this.variablesArray.removeAt(index);
  }

  canStart(): boolean {
    // Can start if form is valid or if there are no variables
    if (this.variablesArray.length === 0) {
      return true;
    }

    // Check if all variables have name and value
    for (let i = 0; i < this.variablesArray.length; i++) {
      const variableGroup = this.variablesArray.at(i) as FormGroup;
      if (!variableGroup.get('name')?.value || variableGroup.get('value')?.value === '') {
        return false;
      }
    }

    return true;
  }

  onStart(): void {
    if (!this.canStart()) {
      return;
    }

    const businessKey = this.startForm.get('businessKey')?.value || undefined;
    const variables: { [key: string]: any } = {};

    // Convert variables array to object
    this.variablesArray.controls.forEach((control) => {
      const variableGroup = control as FormGroup;
      const name = variableGroup.get('name')?.value;
      const type = variableGroup.get('type')?.value;
      let value = variableGroup.get('value')?.value;

      if (name && value !== '') {
        // Convert value based on type
        if (type === 'number') {
          value = Number(value);
        } else if (type === 'boolean') {
          value = value === 'true' || value === true;
        } else if (type === 'json') {
          try {
            value = JSON.parse(value);
          } catch (e) {
            console.error('Invalid JSON value:', value);
          }
        }

        variables[name] = value;
      }
    });

    const result: ProcessStartResult = {
      businessKey,
      variables: Object.keys(variables).length > 0 ? variables : {}
    };

    this.dialogRef.close(result);
  }

  onCancel(): void {
    this.dialogRef.close(null);
  }
}
