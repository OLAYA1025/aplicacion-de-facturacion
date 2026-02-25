import { Component, Output, EventEmitter, inject, OnInit } from '@angular/core';
import { Router, NavigationEnd, ActivatedRoute } from  '@angular/router';
import { filter } from 'rxjs/operators';
import { AuthService } from '../../core/services/auth.service';
import { CommonModule } from '@angular/common';
import { ThemeService } from '../../core/services/theme.service';

@Component({
  selector: 'app-topbar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './topbar.component.html',
})
export class TopbarComponent implements OnInit {

  @Output() menuClick = new EventEmitter<void>();
  @Output() collapseClick = new EventEmitter<void>();

    
    pageTitle : string = '';
    username : string = 'usuario';
    email : string = 'usuario@gmail.com';
    iniciales : string = 'U';

    router : Router = inject(Router);
    activatedRoute : ActivatedRoute = inject(ActivatedRoute);
    authService: AuthService = inject(AuthService);


    isOpen = false;

    theme = inject(ThemeService);

    themeAnimating = false;

    toggleTheme() {
      this.themeAnimating = true;
      setTimeout(() => (this.themeAnimating = false), 350);

      this.theme.toggleTheme(); // aquí ocurre el cambio real
    }

    toggleDropdown() {
      this.isOpen = !this.isOpen;
    }

    closeDropdown() {
      this.isOpen = false;
    }

  ngOnInit() {
    // Obtener título inicial
    this.updateTitle();

    // Cargar información del usuario
    this.loadUser();



    // Escuchar cambios de ruta
    this.router.events.pipe(
      filter(event => event instanceof NavigationEnd)
    ).subscribe(() => {
      this.updateTitle();
    });
  }

  private updateTitle(): void {
    let route = this.activatedRoute;
    
    // Navegar hasta la ruta hija más profunda
    while (route.firstChild) {
      route = route.firstChild;
    }
    
    // Obtener el título de data o usar 'Caja' por defecto
    this.pageTitle = route.snapshot.data['title'] || 'Caja';
  }

  private loadUser(){   
    const usuario = this.authService.getUsuario();
    if(usuario){
        this.username = usuario.nombre || usuario.username || 'Usuario';
        this.email = usuario.email || 'usuario@gmail.com';
        this.iniciales = usuario.nombre ? usuario.nombre.charAt(0).toUpperCase() : 'U';
        }
    }

     logout(): void {
    this.authService.logout();
    this.router.navigate(['']);
    }

    goToProfile(): void {
      void this.router.navigate(['/perfil']);
    }
}
