import { CommonModule } from '@angular/common';
import { ChangeDetectionStrategy, Component, OnInit, inject, output } from '@angular/core';
import { ActivatedRoute, NavigationEnd, Router } from '@angular/router';
import { filter } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth.service';
import { ThemeService } from '../../../core/services/theme.service';

@Component({
  selector: 'app-admin-topbar',
  imports: [CommonModule],
  templateUrl: './admin-topbar.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminTopbarComponent implements OnInit {
  readonly menuClick = output<void>();
  readonly collapseClick = output<void>();

  pageTitle = 'Panel de plataforma';
  username = 'usuario';
  email = 'usuario@gmail.com';
  iniciales = 'U';
  isOpen = false;
  themeAnimating = false;

  private readonly router = inject(Router);
  private readonly activatedRoute = inject(ActivatedRoute);
  private readonly authService = inject(AuthService);
  readonly theme = inject(ThemeService);

  ngOnInit(): void {
    this.updateTitle();
    this.loadUser();

    this.router.events.pipe(filter((event) => event instanceof NavigationEnd)).subscribe(() => {
      this.updateTitle();
    });
  }

  toggleTheme(): void {
    this.themeAnimating = true;
    setTimeout(() => (this.themeAnimating = false), 350);
    this.theme.toggleTheme();
  }

  toggleDropdown(): void {
    this.isOpen = !this.isOpen;
  }

  closeDropdown(): void {
    this.isOpen = false;
  }

  logout(): void {
    this.authService.logout();
    void this.router.navigate(['']);
  }

  private updateTitle(): void {
    let route = this.activatedRoute;
    while (route.firstChild) {
      route = route.firstChild;
    }
    this.pageTitle = route.snapshot.data['title'] || 'Panel de plataforma';
  }

  private loadUser(): void {
    const usuario = this.authService.getUsuario();
    if (!usuario) {
      return;
    }

    this.username = usuario.nombre || usuario.username || 'Usuario';
    this.email = usuario.email || 'usuario@gmail.com';
    this.iniciales = usuario.nombre ? usuario.nombre.charAt(0).toUpperCase() : 'U';
  }
}
