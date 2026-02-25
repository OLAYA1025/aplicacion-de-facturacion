import { Component, OnInit, inject, signal } from '@angular/core';
import { LoginRequest } from '../../models/login-request.model';
import { AuthService } from '../../../../core/services/auth.service';
import { ActivatedRoute, Router } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { CommonModule } from '@angular/common';
import { finalize, TimeoutError } from 'rxjs';
import { ThemeService } from '../../../../core/services/theme.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent implements OnInit {
  username = '';
  password = '';
  errorMessage = signal('');
  loading = signal(false);
  theme = inject(ThemeService);
  themeAnimating = false;

  constructor(
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute
  ) {}

  ngOnInit(): void {
    if (this.route.snapshot.queryParamMap.get('sessionExpired') === '1') {
      this.errorMessage.set('Tu sesion expiro. Inicia sesion nuevamente.');
    }
  }

  toggleTheme(): void {
    this.themeAnimating = true;
    setTimeout(() => (this.themeAnimating = false), 350);
    this.theme.toggleTheme();
  }

  onSubmit(): void {
    if (this.loading()) {
      return;
    }

    this.errorMessage.set('');

    if (!this.username || !this.password) {
      this.errorMessage.set('Todos los campos son obligatorios');
      return;
    }

    const loginRequest: LoginRequest = {
      username: this.username.trim(),
      password: this.password
    };

    this.loading.set(true);

    this.authService
      .login(loginRequest)
      .pipe(finalize(() => this.loading.set(false)))
      .subscribe({
        next: () => {
          this.router.navigate(['/caja']);
        },
        error: (err) => {
          if (err instanceof TimeoutError || err?.name === 'TimeoutError') {
            this.errorMessage.set('El servidor tardó demasiado en responder. Intenta de nuevo.');
            return;
          }

          if (err?.status === 401) {
            this.errorMessage.set('Credenciales incorrectas');
            return;
          }

          if (err?.status === 0) {
            this.errorMessage.set('No se pudo conectar con el servidor de autenticación');
            return;
          }

          this.errorMessage.set(err?.error?.message || 'Error al iniciar sesión');
        }
      });
  }
}
