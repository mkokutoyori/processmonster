import { Injectable } from '@angular/core';

/**
 * Notification service for displaying toast messages
 * Wrapper around ngx-toastr for consistent notifications
 */
@Injectable({
  providedIn: 'root'
})
export class NotificationService {

  /**
   * Show success notification
   */
  success(message: string, title?: string): void {
    // Using native browser notifications for now
    // TODO: Integrate ngx-toastr when Material theme is configured
    console.log('✅ SUCCESS:', title || 'Success', '-', message);
    this.showNotification(message, 'success');
  }

  /**
   * Show error notification
   */
  error(message: string, title?: string): void {
    console.error('❌ ERROR:', title || 'Error', '-', message);
    this.showNotification(message, 'error');
  }

  /**
   * Show warning notification
   */
  warning(message: string, title?: string): void {
    console.warn('⚠️ WARNING:', title || 'Warning', '-', message);
    this.showNotification(message, 'warning');
  }

  /**
   * Show info notification
   */
  info(message: string, title?: string): void {
    console.info('ℹ️ INFO:', title || 'Info', '-', message);
    this.showNotification(message, 'info');
  }

  /**
   * Show simple notification (fallback to browser notification)
   */
  private showNotification(message: string, type: 'success' | 'error' | 'warning' | 'info'): void {
    // Simple fallback using browser APIs
    // In production, this would use ngx-toastr
    if ('Notification' in window && Notification.permission === 'granted') {
      new Notification(this.getTitle(type), {
        body: message,
        icon: this.getIcon(type)
      });
    }
  }

  private getTitle(type: string): string {
    const titles: { [key: string]: string } = {
      'success': 'Success',
      'error': 'Error',
      'warning': 'Warning',
      'info': 'Information'
    };
    return titles[type] || 'Notification';
  }

  private getIcon(type: string): string {
    const icons: { [key: string]: string } = {
      'success': '✅',
      'error': '❌',
      'warning': '⚠️',
      'info': 'ℹ️'
    };
    return icons[type] || '📢';
  }

  /**
   * Request notification permission
   */
  requestPermission(): void {
    if ('Notification' in window && Notification.permission === 'default') {
      Notification.requestPermission();
    }
  }
}
