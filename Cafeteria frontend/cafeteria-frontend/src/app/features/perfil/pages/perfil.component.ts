import { CommonModule } from '@angular/common';
import { ChangeDetectorRef, Component, OnInit, inject } from '@angular/core';
import { UsuarioResponse } from '../../../core/models/usuario.model';
import { AuthService } from '../../../core/services/auth.service';
import { UsuarioService } from '../../../core/services/usuario.service';

@Component({
  selector: 'app-perfil',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './perfil.component.html',
})
export class PerfilComponent implements OnInit {
  private readonly authService = inject(AuthService);
  private readonly usuarioService = inject(UsuarioService);
  private readonly cdr = inject(ChangeDetectorRef);

  usuario: UsuarioResponse | null = null;
  loading = true;
  errorMensaje = '';

  ngOnInit(): void {
    this.cargarPerfil();
  }

  private cargarPerfil(): void {
    const usuarioSesion = this.authService.getUsuario();
    if (!usuarioSesion?.id) {
      this.loading = false;
      this.errorMensaje = 'No se encontró una sesión de usuario activa.';
      this.cdr.detectChanges();
      return;
    }

    this.usuario = usuarioSesion;
    this.loading = true;
    this.errorMensaje = '';

    this.usuarioService.obtenerUsuario(usuarioSesion.id).subscribe({
      next: (usuario) => {
        this.usuario = usuario;
        this.loading = false;
        this.cdr.detectChanges();
      },
      error: () => {
        this.loading = false;
        this.errorMensaje = 'No se pudo cargar el perfil desde el servidor.';
        this.cdr.detectChanges();
      },
    });
  }
}
