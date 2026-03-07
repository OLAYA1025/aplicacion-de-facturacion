import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { catchError, map, Observable, throwError } from 'rxjs';
import { environment } from 'src/environments/environment';
import { FacturaListItem } from '../models/factura-list-item.model';
import { FacturaListadoParams } from '../models/factura-listado-params.model';
import { PageResponse } from '../models/page-response.model';

@Injectable({
  providedIn: 'root',
})
export class FacturasService {
  private readonly baseUrl = `${environment.apiUrl}/documentos-fiscales`;

  constructor(private readonly http: HttpClient) {}

  listFacturas(params: FacturaListadoParams = {}): Observable<PageResponse<FacturaListItem>> {
    let httpParams = new HttpParams()
      .set('page', params.page ?? 0)
      .set('size', params.size ?? 20)
      .set('q', params.q ?? '')
      .set('sort', params.sort ?? 'createdAt,desc');

    if (params.from) {
      httpParams = httpParams.set('from', params.from);
    }

    if (params.to) {
      httpParams = httpParams.set('to', params.to);
    }

    for (const estado of params.estados ?? []) {
      httpParams = httpParams.append('estado', estado);
    }

    return this.http.get<PageResponse<FacturaListItem>>(this.baseUrl, { params: httpParams });
  }

  imprimirFactura(id: number): Observable<void> {
    const openedWindow = window.open('', '_blank');
    if (!openedWindow) {
      throw new Error('El navegador bloqueo la ventana emergente del PDF.');
    }

    this.mostrarCargandoEnVentana(openedWindow);

    //el cambio esta aca
    return this.http
      .get(`${this.baseUrl}/${id}/tirilla/pdf`, {
        observe: 'response',
        responseType: 'blob',
      })
      .pipe(
        map((response) => {
          const pdfBlob = response.body;
          if (!pdfBlob) {
            throw new Error('El servidor no devolvio el PDF de la factura.');
          }

          const nombreArchivo = this.extraerNombreArchivo(
            response.headers.get('content-disposition'),
            `factura-${id}.pdf`
          );

          const pdfFile = new File([pdfBlob], nombreArchivo, {
            type: pdfBlob.type || 'application/pdf',
          });
          this.abrirPdfEnVentana(openedWindow, pdfFile, nombreArchivo);
        })
      )
      .pipe(
        catchError((error) => {
          openedWindow.close();
          return throwError(() => error);
        })
      );
  }

  reenviarFactura(id: number): Observable<void> {
    return this.http.post<void>(`${this.baseUrl}/${id}/reenviar`, {});
  }

  obtenerMensajeErrorFactura(id: number): Observable<string> {
    return this.http
      .get(`${this.baseUrl}/${id}/mensaje-error`, {
        responseType: 'text',
      })
      .pipe(map((mensaje) => mensaje.trim().replace(/^"|"$/g, '')));
  }

  private abrirPdfEnVentana(openedWindow: Window, pdfFile: File, nombreArchivo: string): void {
    const blobUrl = URL.createObjectURL(pdfFile);

    openedWindow.document.title = nombreArchivo;
    openedWindow.document.body.style.margin = '0';
    openedWindow.document.body.innerHTML = '';

    const visorPdf = openedWindow.document.createElement('iframe');
    visorPdf.src = blobUrl;
    visorPdf.title = nombreArchivo;
    visorPdf.style.border = '0';
    visorPdf.style.width = '100vw';
    visorPdf.style.height = '100vh';
    visorPdf.addEventListener(
      'load',
      () => {
        try {
          visorPdf.contentWindow?.focus();
          visorPdf.contentWindow?.print();
        } catch {
          openedWindow.focus();
          openedWindow.print();
        }
      },
      { once: true }
    );

    openedWindow.document.body.appendChild(visorPdf);

    setTimeout(() => URL.revokeObjectURL(blobUrl), 60_000);
  }

  private mostrarCargandoEnVentana(openedWindow: Window): void {
    openedWindow.document.title = 'Preparando PDF...';
    openedWindow.document.body.style.margin = '0';
    openedWindow.document.body.style.display = 'grid';
    openedWindow.document.body.style.placeItems = 'center';
    openedWindow.document.body.style.minHeight = '100vh';
    openedWindow.document.body.style.fontFamily = 'system-ui, sans-serif';
    openedWindow.document.body.innerHTML =
      '<p style="font-size:14px;color:#374151;">Preparando PDF para imprimir...</p>';
  }

  private extraerNombreArchivo(contentDisposition: string | null, fallback: string): string {
    if (!contentDisposition) {
      return fallback;
    }

    const filenameStarMatch = contentDisposition.match(/filename\*\s*=\s*([^;]+)/i);
    if (filenameStarMatch?.[1]) {
      const rawValue = filenameStarMatch[1].trim().replace(/^"|"$/g, '');
      const encodedPart = rawValue.includes("''") ? rawValue.split("''")[1] : rawValue;

      if (encodedPart) {
        try {
          return decodeURIComponent(encodedPart).replace(/[\\/:*?"<>|]/g, '_');
        } catch {
          return encodedPart.replace(/[\\/:*?"<>|]/g, '_');
        }
      }
    }

    const filenameMatch = contentDisposition.match(/filename\s*=\s*"([^"]+)"|filename\s*=\s*([^;]+)/i);
    const filename = filenameMatch?.[1] ?? filenameMatch?.[2];
    if (!filename) {
      return fallback;
    }

    return filename.trim().replace(/^"|"$/g, '').replace(/[\\/:*?"<>|]/g, '_');
  }
}
