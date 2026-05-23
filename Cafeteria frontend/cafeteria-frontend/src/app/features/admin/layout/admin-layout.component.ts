import { ChangeDetectionStrategy, Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AdminSidebarComponent } from './admin-sidebar.component';
import { AdminTopbarComponent } from './admin-topbar.component';

@Component({
  selector: 'app-admin-layout',
  imports: [RouterOutlet, AdminSidebarComponent, AdminTopbarComponent],
  templateUrl: './admin-layout.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminLayoutComponent {
  readonly sidebarCollapsed = signal(false);
  readonly mobileOpen = signal(false);

  toggleSidebar(): void {
    this.sidebarCollapsed.update((value) => !value);
  }

  toggleMobile(): void {
    this.mobileOpen.update((value) => !value);
  }
}
