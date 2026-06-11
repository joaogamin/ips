import { Injectable } from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor,
  HttpErrorResponse
} from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { ApiErrorResponse } from '../models/api-error-response.model';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        const apiError: ApiErrorResponse = {
          status: error.status,
          error: error.error?.error ?? 'Unknown Error',
          message: error.error?.message ?? 'Erro inesperado. Tente novamente.',
          path: error.url ?? '',
          timestamp: new Date().toISOString(),
          fieldErrors: error.error?.fieldErrors
        };
        return throwError(() => apiError);
      })
    );
  }
}
