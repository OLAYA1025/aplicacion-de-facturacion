import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { SidebarComponent } from './sidebar/sidebar.component';
import { TopbarComponent } from './topbar/topbar.component';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [RouterOutlet, SidebarComponent, TopbarComponent],
  templateUrl: './layout.component.html',
})
export class LayoutComponent {
 sidebarCollapsed = signal(false);
  mobileOpen = signal(false);

  toggleSidebar() {
    this.sidebarCollapsed.update(v => !v);
  }

  toggleMobile() {
    this.mobileOpen.update(v => !v);
  }

}