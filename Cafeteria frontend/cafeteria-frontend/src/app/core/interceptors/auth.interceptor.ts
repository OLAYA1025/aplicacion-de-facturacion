import { inject } from '@angular/core';
import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { Router } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { catchError, throwError } from 'rxjs';

function redirectToLogin(router: Router): void {
  if (router.url === '/' || router.url.startsWith('/?')) {
    return;
  }

  void router.navigate([''], { queryParams: { sessionExpired: '1' } });
}

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  if (req.url.includes('/auth/login')) {
    return next(req);
  }

  const authService = inject(AuthService);
  const router = inject(Router);
  const token = authService.getToken();

  let request = req;
  if (token) {
    if (authService.isTokenExpired(token)) {
      authService.logout();
      redirectToLogin(router);
    } else {
      request = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
    }
  }

  return next(request).pipe(
    catchError((error: HttpErrorResponse) => {
      if (error.status === 401) {
        authService.logout();
        redirectToLogin(router);
      }

      return throwError(() => error);
    })
  );
};
