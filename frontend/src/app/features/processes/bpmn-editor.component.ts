import { Component, OnInit, OnDestroy, ViewChild, ElementRef, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatTooltipModule } from '@angular/material/tooltip';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatDividerModule } from '@angular/material/divider';
import BpmnModeler from 'bpmn-js/lib/Modeler';

/**
 * BPMN Editor Component
 * Visual BPMN 2.0 process editor using bpmn-js
 */
@Component({
  selector: 'app-bpmn-editor',
  standalone: true,
  imports: [
    CommonModule,
    MatButtonModule,
    MatIconModule,
    MatToolbarModule,
    MatTooltipModule,
    MatSnackBarModule,
    MatDividerModule
  ],
  template: `
    <div class="bpmn-editor-container">
      <!-- Toolbar -->
      <mat-toolbar class="editor-toolbar">
        <div class="toolbar-left">
          <button mat-icon-button matTooltip="New Process" (click)="createNew()">
            <mat-icon>add</mat-icon>
          </button>
          <button mat-icon-button matTooltip="Import BPMN" (click)="fileInput.click()">
            <mat-icon>upload_file</mat-icon>
          </button>
          <input #fileInput type="file" accept=".bpmn,.xml" style="display: none" (change)="onFileSelected($event)">
          <button mat-icon-button matTooltip="Export BPMN" (click)="exportBpmn()">
            <mat-icon>download</mat-icon>
          </button>
          <button mat-icon-button matTooltip="Save" (click)="save()" [disabled]="!hasChanges">
            <mat-icon>save</mat-icon>
          </button>
        </div>

        <mat-divider [vertical]="true"></mat-divider>

        <div class="toolbar-center">
          <button mat-icon-button matTooltip="Undo" (click)="undo()">
            <mat-icon>undo</mat-icon>
          </button>
          <button mat-icon-button matTooltip="Redo" (click)="redo()">
            <mat-icon>redo</mat-icon>
          </button>
        </div>

        <mat-divider [vertical]="true"></mat-divider>

        <div class="toolbar-right">
          <button mat-icon-button matTooltip="Zoom In" (click)="zoomIn()">
            <mat-icon>zoom_in</mat-icon>
          </button>
          <button mat-icon-button matTooltip="Zoom Out" (click)="zoomOut()">
            <mat-icon>zoom_out</mat-icon>
          </button>
          <button mat-icon-button matTooltip="Zoom to Fit" (click)="zoomToFit()">
            <mat-icon>fit_screen</mat-icon>
          </button>
          <button mat-icon-button matTooltip="Validate" (click)="validate()">
            <mat-icon>check_circle</mat-icon>
          </button>
        </div>
      </mat-toolbar>

      <!-- BPMN Canvas -->
      <div #canvas class="bpmn-canvas"></div>

      <!-- Properties Panel Placeholder -->
      <div class="properties-panel">
        <h3>Properties</h3>
        <p class="properties-placeholder">Select an element to view properties</p>
      </div>
    </div>
  `,
  styles: [`
    .bpmn-editor-container {
      display: flex;
      flex-direction: column;
      height: 100%;
      width: 100%;
      position: relative;
    }

    .editor-toolbar {
      background: #ffffff;
      border-bottom: 1px solid #e0e0e0;
      height: 56px;
      display: flex;
      gap: 16px;
      padding: 0 16px;
    }

    .toolbar-left,
    .toolbar-center,
    .toolbar-right {
      display: flex;
      gap: 8px;
      align-items: center;
    }

    .toolbar-center {
      flex: 1;
    }

    mat-divider {
      height: 32px;
    }

    .bpmn-canvas {
      flex: 1;
      position: relative;
      background: #f5f5f5;
      overflow: hidden;
    }

    .properties-panel {
      position: absolute;
      right: 0;
      top: 56px;
      bottom: 0;
      width: 300px;
      background: #ffffff;
      border-left: 1px solid #e0e0e0;
      padding: 16px;
      overflow-y: auto;
      z-index: 10;
    }

    .properties-panel h3 {
      margin: 0 0 16px 0;
      font-size: 16px;
      font-weight: 500;
    }

    .properties-placeholder {
      color: #999;
      font-size: 14px;
      text-align: center;
      margin-top: 32px;
    }

    /* BPMN.js default styles */
    :host ::ng-deep .bjs-container {
      width: 100%;
      height: 100%;
    }

    :host ::ng-deep .djs-palette {
      background: #ffffff;
      border: 1px solid #e0e0e0;
      border-radius: 4px;
    }

    :host ::ng-deep .djs-context-pad {
      background: #ffffff;
      border: 1px solid #e0e0e0;
      border-radius: 4px;
    }
  `]
})
export class BpmnEditorComponent implements OnInit, OnDestroy {
  @ViewChild('canvas', { static: true }) canvasElement!: ElementRef;
  @Input() bpmnXml?: string;
  @Output() bpmnChange = new EventEmitter<string>();
  @Output() saveRequested = new EventEmitter<string>();

  private modeler!: BpmnModeler;
  hasChanges = false;

  private defaultBpmnXml = `<?xml version="1.0" encoding="UTF-8"?>
<bpmn:definitions xmlns:bpmn="http://www.omg.org/spec/BPMN/20100524/MODEL"
                  xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
                  xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
                  xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
                  id="Definitions_1"
                  targetNamespace="http://bpmn.io/schema/bpmn">
  <bpmn:process id="Process_1" isExecutable="true">
    <bpmn:startEvent id="StartEvent_1" name="Start">
      <bpmn:outgoing>Flow_1</bpmn:outgoing>
    </bpmn:startEvent>
    <bpmn:endEvent id="EndEvent_1" name="End">
      <bpmn:incoming>Flow_1</bpmn:incoming>
    </bpmn:endEvent>
    <bpmn:sequenceFlow id="Flow_1" sourceRef="StartEvent_1" targetRef="EndEvent_1" />
  </bpmn:process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
      <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1">
        <dc:Bounds x="179" y="159" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="185" y="202" width="24" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNShape id="EndEvent_1_di" bpmnElement="EndEvent_1">
        <dc:Bounds x="432" y="159" width="36" height="36" />
        <bpmndi:BPMNLabel>
          <dc:Bounds x="440" y="202" width="20" height="14" />
        </bpmndi:BPMNLabel>
      </bpmndi:BPMNShape>
      <bpmndi:BPMNEdge id="Flow_1_di" bpmnElement="Flow_1">
        <di:waypoint x="215" y="177" />
        <di:waypoint x="432" y="177" />
      </bpmndi:BPMNEdge>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</bpmn:definitions>`;

  constructor(private snackBar: MatSnackBar) {}

  ngOnInit(): void {
    this.initializeModeler();
    this.loadBpmn(this.bpmnXml || this.defaultBpmnXml);
  }

  ngOnDestroy(): void {
    if (this.modeler) {
      this.modeler.destroy();
    }
  }

  private initializeModeler(): void {
    this.modeler = new BpmnModeler({
      container: this.canvasElement.nativeElement,
      keyboard: {
        bindTo: document
      }
    });

    // Listen to changes
    this.modeler.on('commandStack.changed', () => {
      this.hasChanges = true;
      this.emitBpmnXml();
    });

    // Listen to selection changes (for properties panel)
    this.modeler.on('selection.changed', (event: any) => {
      const selection = event.newSelection;
      if (selection && selection.length > 0) {
        // TODO: Update properties panel
        console.log('Selected element:', selection[0]);
      }
    });
  }

  private loadBpmn(xml: string): void {
    this.modeler.importXML(xml).then(() => {
      const canvas = this.modeler.get('canvas');
      canvas.zoom('fit-viewport');
      this.hasChanges = false;
    }).catch((err: any) => {
      console.error('Error loading BPMN diagram:', err);
      this.snackBar.open('Error loading BPMN diagram', 'Close', { duration: 3000 });
    });
  }

  private emitBpmnXml(): void {
    this.modeler.saveXML({ format: true }).then((result: any) => {
      this.bpmnChange.emit(result.xml);
    });
  }

  createNew(): void {
    if (this.hasChanges && !confirm('You have unsaved changes. Create a new process?')) {
      return;
    }
    this.loadBpmn(this.defaultBpmnXml);
    this.snackBar.open('New process created', 'Close', { duration: 2000 });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    if (input.files && input.files.length > 0) {
      const file = input.files[0];
      const reader = new FileReader();
      reader.onload = (e) => {
        const xml = e.target?.result as string;
        this.loadBpmn(xml);
        this.snackBar.open('BPMN file imported', 'Close', { duration: 2000 });
      };
      reader.readAsText(file);
      input.value = ''; // Reset input
    }
  }

  exportBpmn(): void {
    this.modeler.saveXML({ format: true }).then((result: any) => {
      const blob = new Blob([result.xml], { type: 'application/xml' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'process.bpmn';
      link.click();
      window.URL.revokeObjectURL(url);
      this.snackBar.open('BPMN exported', 'Close', { duration: 2000 });
    });
  }

  save(): void {
    this.modeler.saveXML({ format: true }).then((result: any) => {
      this.saveRequested.emit(result.xml);
      this.hasChanges = false;
      this.snackBar.open('Process saved', 'Close', { duration: 2000 });
    });
  }

  undo(): void {
    const commandStack = this.modeler.get('commandStack');
    if (commandStack.canUndo()) {
      commandStack.undo();
    }
  }

  redo(): void {
    const commandStack = this.modeler.get('commandStack');
    if (commandStack.canRedo()) {
      commandStack.redo();
    }
  }

  zoomIn(): void {
    const canvas = this.modeler.get('canvas');
    const currentZoom = canvas.zoom();
    canvas.zoom(currentZoom + 0.1);
  }

  zoomOut(): void {
    const canvas = this.modeler.get('canvas');
    const currentZoom = canvas.zoom();
    canvas.zoom(Math.max(0.1, currentZoom - 0.1));
  }

  zoomToFit(): void {
    const canvas = this.modeler.get('canvas');
    canvas.zoom('fit-viewport');
  }

  validate(): void {
    this.modeler.saveXML({ format: true }).then((result: any) => {
      // Basic validation: check if XML is well-formed
      const parser = new DOMParser();
      const xmlDoc = parser.parseFromString(result.xml, 'text/xml');
      const parseError = xmlDoc.querySelector('parsererror');

      if (parseError) {
        this.snackBar.open('BPMN validation failed: Invalid XML', 'Close', { duration: 5000 });
      } else {
        // Additional validation: check for required elements
        const processes = xmlDoc.getElementsByTagNameNS('http://www.omg.org/spec/BPMN/20100524/MODEL', 'process');
        if (processes.length === 0) {
          this.snackBar.open('BPMN validation failed: No process found', 'Close', { duration: 5000 });
        } else {
          this.snackBar.open('BPMN validation successful', 'Close', { duration: 3000 });
        }
      }
    }).catch((err: any) => {
      this.snackBar.open('BPMN validation failed: ' + err.message, 'Close', { duration: 5000 });
    });
  }

  /**
   * Load BPMN XML from external source
   */
  loadXml(xml: string): void {
    this.loadBpmn(xml);
  }

  /**
   * Get current BPMN XML
   */
  async getXml(): Promise<string> {
    const result = await this.modeler.saveXML({ format: true });
    return result.xml;
  }
}
