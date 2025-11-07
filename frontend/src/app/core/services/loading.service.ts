/**
 * Loading Service
 *
 * Manages global loading state for HTTP requests and async operations.
 * Uses a counter to handle multiple concurrent requests.
 */
import { Injectable, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LoadingService {
  private loadingCounter = 0;
  public loading = signal<boolean>(false);

  /**
   * Show loading indicator
   */
  show(): void {
    this.loadingCounter++;
    this.loading.set(true);
  }

  /**
   * Hide loading indicator
   */
  hide(): void {
    this.loadingCounter--;
    if (this.loadingCounter <= 0) {
      this.loadingCounter = 0;
      this.loading.set(false);
    }
  }

  /**
   * Reset loading state
   */
  reset(): void {
    this.loadingCounter = 0;
    this.loading.set(false);
  }
}
