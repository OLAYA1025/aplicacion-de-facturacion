import { CanActivateChildFn, CanActivateFn, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { inject } from '@angular/core';

function resolveAuthGuard(authService: AuthService, router: Router): boolean | UrlTree {
  if (authService.isAuthenticated()) {
    return true;
  }

  const hadToken = !!authService.getToken();
  authService.logout();

  if (hadToken) {
    return router.createUrlTree([''], { queryParams: { sessionExpired: '1' } });
  }

  return router.createUrlTree(['']);
}

export const authGuard: CanActivateFn = () => {

  const authService = inject(AuthService);
  const router = inject(Router);
  return resolveAuthGuard(authService, router);

};

export const authChildGuard: CanActivateChildFn = () => {
  const authService = inject(AuthService);
  const router = inject(Router);
  return resolveAuthGuard(authService, router);
};
