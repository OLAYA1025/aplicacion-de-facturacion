import { Component, inject } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { ThemeService } from './core/services/theme.service';
import { ModalMensajeGlobalComponent } from './shared/components/modal-mensaje-global/modal-mensaje-global.component';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, ModalMensajeGlobalComponent],
  templateUrl: './app.html',
})
export class AppComponent {
  private theme = inject(ThemeService);

  ngOnInit() {
    this.theme.initTheme();
  }

}
