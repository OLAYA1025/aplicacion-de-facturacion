import { Injectable } from '@angular/core';

type Theme = 'light' | 'dark';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  private storageKey = 'theme';

  initTheme() {
    const saved = (localStorage.getItem(this.storageKey) as Theme | null);

    const prefersDark = window.matchMedia?.('(prefers-color-scheme: dark)').matches;
    const theme: Theme = saved ?? (prefersDark ? 'dark' : 'light');

    // IMPORTANTE: al iniciar no animamos (para que no “parpadee”)
    this.applyTheme(theme, { animate: false });
  }

  toggleTheme() {
    const isDark = document.documentElement.classList.contains('dark');
    this.applyTheme(isDark ? 'light' : 'dark', { animate: true });
  }

  setTheme(theme: Theme) {
    this.applyTheme(theme, { animate: true });
  }

  private applyTheme(theme: Theme, opts: { animate: boolean } = { animate: true }) {
    const root = document.documentElement;

    if (opts.animate) {
      root.classList.add('theme-transition');
      window.setTimeout(() => root.classList.remove('theme-transition'), 350);
    }

    root.classList.toggle('dark', theme === 'dark');
    localStorage.setItem(this.storageKey, theme);
  }
}