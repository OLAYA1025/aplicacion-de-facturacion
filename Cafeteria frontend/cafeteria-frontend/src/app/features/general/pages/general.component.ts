import { CommonModule } from '@angular/common';
import { Component, inject } from '@angular/core';
import { CajaService } from '../../caja/services/caja.service';


@Component({
  selector: 'app-general',
  imports: [CommonModule],
  standalone: true,
  template: `
    <h1>Panel General</h1>

    <div 
      class="card"
      [class.abierta]="cajaActiva()"
      [class.cerrada]="!cajaActiva()"
    >
      <h3>Estado de Caja</h3>
      <p *ngIf="cajaActiva()">Caja abierta</p>
      <p *ngIf="!cajaActiva()">No hay caja abierta</p>
    </div>
  `,
  styles: [`
    :host {
      color: #111827;
    }

    .card {
      padding: 20px;
      border-top: 6px solid;
      border-radius: 6px;
      background: white;
      color: inherit;
      width: 300px;
    }

    :host-context(.dark) {
      color: #f3f4f6;
    }

    :host-context(.dark) .card {
      background: #111827;
      box-shadow: inset 0 0 0 1px #374151;
    }

    .abierta {
      border-color: green;
    }
    .cerrada {
      border-color: red;
    }
  `]
})
export class GeneralComponent {

    private cajaService = inject(CajaService);
    cajaActiva = this.cajaService.cajaActiva;
  
}
