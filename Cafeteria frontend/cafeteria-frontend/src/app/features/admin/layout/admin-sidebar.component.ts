import { ChangeDetectionStrategy, Component, input, output } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

@Component({
  selector: 'app-admin-sidebar',
  imports: [RouterLink, RouterLinkActive],
  templateUrl: './admin-sidebar.component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AdminSidebarComponent {
  readonly collapsed = input(false);
  readonly mobileOpen = input(false);

  readonly toggleSidebar = output<void>();
  readonly closeMobile = output<void>();
}
