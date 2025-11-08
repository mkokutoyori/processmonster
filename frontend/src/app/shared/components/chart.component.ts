import { Component, Input, OnInit, OnChanges, SimpleChanges, ViewChild } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';

/**
 * Reusable Chart Component
 * Wrapper for Chart.js with ng2-charts
 */
@Component({
  selector: 'app-chart',
  standalone: true,
  imports: [CommonModule, BaseChartDirective],
  template: `
    <div class="chart-container">
      <canvas baseChart
              [data]="chartData"
              [options]="chartOptions"
              [type]="chartType">
      </canvas>
    </div>
  `,
  styles: [`
    .chart-container {
      position: relative;
      height: 100%;
      width: 100%;
    }
  `]
})
export class ChartComponent implements OnInit, OnChanges {
  @Input() chartType: ChartType = 'line';
  @Input() chartData!: ChartData;
  @Input() chartOptions: ChartConfiguration['options'] = {};
  @Input() height: string = '300px';

  @ViewChild(BaseChartDirective) chart?: BaseChartDirective;

  ngOnInit(): void {
    this.setupDefaultOptions();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['chartData'] && this.chart) {
      this.chart.update();
    }
  }

  setupDefaultOptions(): void {
    if (!this.chartOptions) {
      this.chartOptions = {};
    }

    // Default responsive options
    this.chartOptions = {
      responsive: true,
      maintainAspectRatio: true,
      ...this.chartOptions
    };
  }
}
