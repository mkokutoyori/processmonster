/**
 * Root Application Component
 *
 * Main component that bootstraps the entire Angular application.
 * Handles:
 * - i18n initialization
 * - Layout rendering
 * - Global navigation
 */
import { Component, OnInit, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterOutlet } from '@angular/router';
import { TranslateService } from '@ngx-translate/core';
import { environment } from '../environments/environment';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [
    CommonModule,
    RouterOutlet
  ],
  template: `
    <div class="app-container">
      <router-outlet></router-outlet>
    </div>
  `,
  styles: [`
    .app-container {
      width: 100%;
      height: 100vh;
      overflow: hidden;
    }
  `]
})
export class AppComponent implements OnInit {
  private translate = inject(TranslateService);

  ngOnInit(): void {
    this.initializeI18n();
  }

  /**
   * Initializes internationalization
   * Sets up supported languages and detects browser language
   */
  private initializeI18n(): void {
    // Set supported languages
    this.translate.addLangs(environment.supportedLanguages);

    // Set default language
    this.translate.setDefaultLang(environment.defaultLanguage);

    // Try to detect browser language
    const browserLang = this.translate.getBrowserLang();
    const langToUse = browserLang && environment.supportedLanguages.includes(browserLang)
      ? browserLang
      : environment.defaultLanguage;

    // Use detected or default language
    this.translate.use(langToUse);

    if (environment.enableDebug) {
      console.log(`🌍 Language initialized: ${langToUse}`);
    }
  }
}
