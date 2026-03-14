import { CanActivateChildFn, CanActivateFn, Router, UrlTree } from '@angular/router';
import { AuthService } from '../services/auth.service';
import { inject } from '@angular/core';

const CAJERO_ALLOWED_EXACT_PATHS = ['/caja', '/pedidos', '/facturas', '/clientes', '/perfil'] as const;
const CAJERO_ALLOWED_PREFIX_PATHS = ['/productos/inventariables', '/productos/preparaciones'] as const;

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

function resolveCajeroRouteGuard(
  authService: AuthService,
  router: Router,
  targetUrl: string
): boolean | UrlTree {
  const rolUsuario = authService.getUsuario()?.rol?.toUpperCase();
  if (rolUsuario !== 'CAJERO') {
    return true;
  }

  const urlWithoutQuery = targetUrl.split('?')[0];
  const hasExactAccess = CAJERO_ALLOWED_EXACT_PATHS.includes(
    urlWithoutQuery as (typeof CAJERO_ALLOWED_EXACT_PATHS)[number]
  );
  const hasPrefixAccess = CAJERO_ALLOWED_PREFIX_PATHS.some(
    (allowedPath) => urlWithoutQuery === allowedPath || urlWithoutQuery.startsWith(`${allowedPath}/`)
  );
  const hasAccess = hasExactAccess || hasPrefixAccess;

  if (hasAccess) {
    return true;
  }

  return router.createUrlTree(['/caja']);
}

export const authGuard: CanActivateFn = () => {

  const authService = inject(AuthService);
  const router = inject(Router);
  return resolveAuthGuard(authService, router);

};

export const authChildGuard: CanActivateChildFn = (_childRoute, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);
  const authResult = resolveAuthGuard(authService, router);
  if (authResult !== true) {
    return authResult;
  }

  return resolveCajeroRouteGuard(authService, router, state.url);
};
