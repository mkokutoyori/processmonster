import { Component, Input, OnInit, OnDestroy, ViewChild, ElementRef, AfterViewInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import BpmnViewer from 'bpmn-js/lib/Viewer';

/**
 * BPMN Viewer Component - Read-only BPMN diagram viewer
 * Uses bpmn-js library to display BPMN 2.0 diagrams
 */
@Component({
  selector: 'app-bpmn-viewer',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="bpmn-viewer-container">
      <div #bpmnContainer class="bpmn-canvas"></div>
      <div *ngIf="loading" class="loading-overlay">
        <div class="spinner"></div>
      </div>
      <div *ngIf="error" class="error-overlay">
        <p>{{ error }}</p>
      </div>
    </div>
  `,
  styles: [`
    .bpmn-viewer-container {
      position: relative;
      width: 100%;
      height: 100%;
      min-height: 400px;
      background: #fafafa;
      border: 1px solid #ddd;
      border-radius: 4px;
    }

    .bpmn-canvas {
      width: 100%;
      height: 100%;
    }

    .loading-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(255, 255, 255, 0.9);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 10;
    }

    .spinner {
      border: 4px solid #f3f3f3;
      border-top: 4px solid #3f51b5;
      border-radius: 50%;
      width: 40px;
      height: 40px;
      animation: spin 1s linear infinite;
    }

    @keyframes spin {
      0% { transform: rotate(0deg); }
      100% { transform: rotate(360deg); }
    }

    .error-overlay {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      background: rgba(244, 67, 54, 0.1);
      display: flex;
      align-items: center;
      justify-content: center;
      z-index: 10;
    }

    .error-overlay p {
      color: #f44336;
      font-weight: 500;
      padding: 16px;
      background: white;
      border-radius: 4px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.2);
    }
  `]
})
export class BpmnViewerComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild('bpmnContainer', { static: false }) private bpmnContainer!: ElementRef;

  /**
   * BPMN XML content to display
   */
  @Input() bpmnXml: string = '';

  /**
   * Optional height for the viewer (default: 400px)
   */
  @Input() height: string = '400px';

  /**
   * Whether to fit diagram to viewport on load
   */
  @Input() fitViewport: boolean = true;

  loading = false;
  error: string | null = null;
  private viewer: BpmnViewer | null = null;

  ngOnInit(): void {
    // Component initialization
  }

  ngAfterViewInit(): void {
    if (this.bpmnXml) {
      this.initializeViewer();
    }
  }

  ngOnDestroy(): void {
    if (this.viewer) {
      this.viewer.destroy();
    }
  }

  /**
   * Initialize BPMN viewer and load diagram
   */
  private initializeViewer(): void {
    try {
      this.loading = true;
      this.error = null;

      // Create viewer instance
      this.viewer = new BpmnViewer({
        container: this.bpmnContainer.nativeElement,
        height: this.height
      });

      // Import BPMN diagram
      this.viewer.importXML(this.bpmnXml).then(() => {
        if (this.fitViewport && this.viewer) {
          const canvas = this.viewer.get('canvas');
          canvas.zoom('fit-viewport');
        }
        this.loading = false;
      }).catch((err: Error) => {
        console.error('Error importing BPMN diagram:', err);
        this.error = 'Failed to load BPMN diagram: ' + err.message;
        this.loading = false;
      });
    } catch (err) {
      console.error('Error initializing BPMN viewer:', err);
      this.error = 'Failed to initialize BPMN viewer';
      this.loading = false;
    }
  }

  /**
   * Reload diagram with new BPMN XML
   */
  loadDiagram(bpmnXml: string): void {
    this.bpmnXml = bpmnXml;
    if (this.viewer) {
      this.viewer.destroy();
    }
    this.initializeViewer();
  }

  /**
   * Zoom to fit viewport
   */
  zoomToFit(): void {
    if (this.viewer) {
      const canvas = this.viewer.get('canvas');
      canvas.zoom('fit-viewport');
    }
  }

  /**
   * Zoom in
   */
  zoomIn(): void {
    if (this.viewer) {
      const canvas = this.viewer.get('canvas');
      const currentZoom = canvas.zoom();
      canvas.zoom(currentZoom + 0.1);
    }
  }

  /**
   * Zoom out
   */
  zoomOut(): void {
    if (this.viewer) {
      const canvas = this.viewer.get('canvas');
      const currentZoom = canvas.zoom();
      canvas.zoom(currentZoom - 0.1);
    }
  }

  /**
   * Reset zoom to 1:1
   */
  resetZoom(): void {
    if (this.viewer) {
      const canvas = this.viewer.get('canvas');
      canvas.zoom(1.0);
    }
  }
}
