import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { AuthResponse } from '../../features/auth/models/auth-response.model';
import { LoginRequest } from '../../features/auth/models/login-request.model';
import { Observable, tap, timeout } from 'rxjs';
import { UsuarioResponse } from '../models/usuario.model';
import { environment } from 'src/environments/environment';


@Injectable({
  providedIn: 'root',
})
export class AuthService {
    private baseUrl = `${environment.apiUrl}/auth`;
    private readonly tokenKey = 'token';
    private readonly tokenTypeKey = 'tipo';
    private readonly userKey = 'usuario';
    private readonly tokenExpiryKey = 'token_exp';
    private readonly expiryBufferMs = 5000;

    constructor(private http: HttpClient) {

     }

      login(loginRequest: LoginRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.baseUrl}/login`, loginRequest)
        .pipe(
          timeout(10000),
          tap(response =>
            {
              localStorage.setItem(this.tokenKey, response.token);
              localStorage.setItem(this.tokenTypeKey, response.tipo);
              localStorage.setItem(this.userKey, JSON.stringify(response.usuario));

              const expiresAt = this.normalizeExpirationEpoch(response.expiracionEpochMillis);
              if (expiresAt) {
                localStorage.setItem(this.tokenExpiryKey, String(expiresAt));
              } else {
                localStorage.removeItem(this.tokenExpiryKey);
              }
            }
            )
          );
      }

      getToken(): string | null {
        return localStorage.getItem(this.tokenKey);
      }

      getTipoToken(): string | null {
        return localStorage.getItem(this.tokenTypeKey);
      }

      getUsuario(): UsuarioResponse | null {
        const usuario = localStorage.getItem(this.userKey);
        return usuario ? JSON.parse(usuario) : null;
      }

      isAuthenticated(): boolean {
        const token = this.getToken();
        return !!token && !this.isTokenExpired(token);
      }

      isTokenExpired(token: string | null = this.getToken()): boolean {
        if (!token) {
          return true;
        }

        const expirationEpochMillis = this.getTokenExpirationEpochMillis(token);
        if (!expirationEpochMillis) {
          return true;
        }

        return expirationEpochMillis <= Date.now() + this.expiryBufferMs;
      }

      logout(): void {
        localStorage.removeItem(this.tokenKey);
        localStorage.removeItem(this.tokenTypeKey);
        localStorage.removeItem(this.userKey);
        localStorage.removeItem(this.tokenExpiryKey);
      }

      private getTokenExpirationEpochMillis(token: string): number | null {
        const storedExpirationRaw = localStorage.getItem(this.tokenExpiryKey);
        if (storedExpirationRaw) {
          const storedExpiration = this.normalizeExpirationEpoch(Number(storedExpirationRaw));
          if (storedExpiration) {
            return storedExpiration;
          }
        }

        const payload = this.parseJwtPayload(token);
        if (!payload) {
          return null;
        }

        return this.normalizeExpirationEpoch(payload['exp']);
      }

      private parseJwtPayload(token: string): Record<string, unknown> | null {
        const tokenParts = token.split('.');
        if (tokenParts.length !== 3) {
          return null;
        }

        try {
          const payload = tokenParts[1].replace(/-/g, '+').replace(/_/g, '/');
          const paddedPayload = payload + '='.repeat((4 - (payload.length % 4)) % 4);
          const decodedPayload = atob(paddedPayload);
          return JSON.parse(decodedPayload) as Record<string, unknown>;
        } catch {
          return null;
        }
      }

      private normalizeExpirationEpoch(value: unknown): number | null {
        if (typeof value !== 'number' || !Number.isFinite(value) || value <= 0) {
          return null;
        }

        return value > 1_000_000_000_000 ? value : value * 1000;
      }
}

