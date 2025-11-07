/**
 * Development Environment Configuration
 */
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api/v1',
  defaultLanguage: 'en',
  supportedLanguages: ['en', 'fr'],
  enableDebug: true,
  cacheTimeout: 300000, // 5 minutes
  tokenRefreshThreshold: 60000, // 1 minute before expiry
  maxUploadSize: 10485760, // 10MB
  dateFormat: 'yyyy-MM-dd',
  dateTimeFormat: 'yyyy-MM-dd HH:mm:ss',
  paginationPageSize: 20,
  paginationPageSizeOptions: [10, 20, 50, 100],
};
