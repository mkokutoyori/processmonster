/**
 * Error HTTP Interceptor
 *
 * Handles all HTTP errors globally and displays user-friendly messages.
 * Uses ngx-toastr for error notifications.
 */
import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { TranslateService } from '@ngx-translate/core';
import { ToastrService } from 'ngx-toastr';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const translate = inject(TranslateService);
  const toastr = inject(ToastrService);

  return next(req).pipe(
    catchError((error) => {
      let errorMessage = translate.instant('error.generic');

      // Handle different error types
      if (error.error instanceof ErrorEvent) {
        // Client-side error
        errorMessage = translate.instant('error.network');
      } else {
        // Server-side error
        switch (error.status) {
          case 400:
            errorMessage = error.error?.message || translate.instant('error.400');
            break;
          case 401:
            errorMessage = translate.instant('error.401');
            break;
          case 403:
            errorMessage = translate.instant('error.403');
            break;
          case 404:
            errorMessage = error.error?.message || translate.instant('error.404');
            break;
          case 500:
            errorMessage = translate.instant('error.500');
            break;
          default:
            if (error.error?.message) {
              errorMessage = error.error.message;
            }
        }
      }

      // Display error toast
      toastr.error(errorMessage, translate.instant('common.error'));

      return throwError(() => error);
    })
  );
};
