import { HttpClient, HttpResponse } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { CrearPedidoRequest } from '../models/crearPedido.model';
import { environment } from 'src/environments/environment';
import { Pedido } from '../../../core/models/pedido.model';
import { catchError, from, map, Observable, of, switchMap, throwError } from 'rxjs';
import { ActualizarPedidoRequest } from '../models/actualizarPedido.model';
import { PedidoUsuarioRequest } from '../models/pedidoUsuario.model';
import { AuthService } from '../../../core/services/auth.service';

export type ImpresionFacturaResultado = { tipo: 'PDF' } | { tipo: 'PARCIAL'; mensaje: string };

@Injectable({ providedIn: 'root' })
export class PedidosService {
  constructor(
    private http: HttpClient,
    private authService: AuthService,
  ) {}

  private baseURL = environment.apiUrl + '/pedido';
  private streamURL = environment.apiUrl + '/pedidos/stream';

  crearPedido(crearPedido: CrearPedidoRequest): Observable<Pedido> {
    return this.http.post<Pedido>(this.baseURL, crearPedido);
  }

  obtenerPedido(id: number): Observable<Pedido> {
    return this.http.get<Pedido>(this.baseURL + '/' + id);
  }

  obtenerPedidosCajaAbierta(): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(this.baseURL + '/caja/abierta');
  }

  nuevoListenerPedidos(): EventSource {
    const token = this.authService.getToken();
    const url = token
      ? `${this.streamURL}?access_token=${encodeURIComponent(token)}`
      : this.streamURL;

    return new EventSource(url);
  }

  obtenerPedidosCaja(idCaja: number): Observable<Pedido[]> {
    return this.http.get<Pedido[]>(this.baseURL + '/caja/' + idCaja);
  }

  actualizarPedido(id: number, actualizarPedido: ActualizarPedidoRequest): Observable<Pedido> {
    return this.http.put<Pedido>(this.baseURL + '/' + id, actualizarPedido);
  }

  cobrarPedido(id: number, pedidoUsuario: PedidoUsuarioRequest): Observable<Pedido> {
    return this.http.post<Pedido>(this.baseURL + '/' + id + '/cobrar', pedidoUsuario);
  }

  cancelarPedido(id: number, pedidoUsuario: PedidoUsuarioRequest): Observable<Pedido> {
    return this.http.post<Pedido>(this.baseURL + '/' + id + '/cancelar', pedidoUsuario);
  }

  imprimirFactura(id: number): Observable<ImpresionFacturaResultado> {
    const openedWindow = window.open('', '_blank');
    if (!openedWindow) {
      return throwError(() => new Error('El navegador bloqueo la ventana emergente del PDF.'));
    }

    this.mostrarCargandoEnVentana(openedWindow);

    return this.http
      .post(
        `${this.baseURL}/${id}/imprimir`,
        {},
        {
          observe: 'response',
          responseType: 'blob',
        },
      )
      .pipe(switchMap((response) => this.procesarRespuestaImpresion(response, id, openedWindow)))
      .pipe(
        catchError((error) => {
          openedWindow.close();
          return throwError(() => error);
        }),
      );
  }

  imprimirPrefactura(id: number): Observable<void> {
    const openedWindow = window.open('', '_blank');
    if (!openedWindow) {
      return throwError(() => new Error('El navegador bloqueo la ventana emergente del PDF.'));
    }

    this.mostrarCargandoEnVentana(openedWindow);

    return this.http
      .get(`${this.baseURL}/${id}/tirilla/pdf`, {
        observe: 'response',
        responseType: 'blob',
      })
      .pipe(
        map((response) => {
          const pdfBlob = response.body;
          if (!pdfBlob) {
            throw new Error('El servidor no devolvio el PDF de la prefactura.');
          }

          const nombreArchivo = this.extraerNombreArchivo(
            response.headers.get('content-disposition'),
            `prefactura-${id}.pdf`,
          );

          const pdfFile = new File([pdfBlob], nombreArchivo, {
            type: pdfBlob.type || 'application/pdf',
          });

          this.abrirPdfEnVentana(openedWindow, pdfFile, nombreArchivo);
        }),
      )
      .pipe(
        catchError((error) => {
          openedWindow.close();
          return throwError(() => error);
        }),
      );
  }

  private procesarRespuestaImpresion(
    response: HttpResponse<Blob>,
    id: number,
    openedWindow: Window,
  ): Observable<ImpresionFacturaResultado> {
    const responseBody = response.body;
    if (!responseBody) {
      throw new Error('El servidor no devolvio una respuesta valida para la impresion.');
    }

    const contentType = (response.headers.get('content-type') ?? '').toLowerCase();
    const esRespuestaJson = response.status === 202 || contentType.includes('application/json');

    if (esRespuestaJson) {
      return from(this.extraerMensajeRespuestaParcial(responseBody)).pipe(
        map((mensaje) => {
          openedWindow.close();
          return {
            tipo: 'PARCIAL',
            mensaje,
          };
        }),
      );
    }

    const nombreArchivo = this.extraerNombreArchivo(
      response.headers.get('content-disposition'),
      `factura-${id}.pdf`,
    );

    const pdfFile = new File([responseBody], nombreArchivo, {
      type: responseBody.type || 'application/pdf',
    });
    this.abrirPdfEnVentana(openedWindow, pdfFile, nombreArchivo);

    return of({ tipo: 'PDF' });
  }

  private async extraerMensajeRespuestaParcial(blob: Blob): Promise<string> {
    const fallback = 'La factura esta en proceso. Intenta imprimirla nuevamente en unos segundos.';
    const contenido = (await blob.text()).trim();

    if (!contenido) {
      return fallback;
    }

    try {
      const payload = JSON.parse(contenido) as Record<string, unknown>;
      return this.obtenerMensajeDesdePayload(payload) ?? fallback;
    } catch {
      return contenido;
    }
  }

  private obtenerMensajeDesdePayload(payload: Record<string, unknown>): string | null {
    const camposPosibles = ['message', 'mensaje', 'error', 'detail', 'title'];

    for (const campo of camposPosibles) {
      const valor = payload[campo];
      if (typeof valor === 'string' && valor.trim().length > 0) {
        return valor;
      }
    }

    return null;
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
      { once: true },
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

    const filenameMatch = contentDisposition.match(
      /filename\s*=\s*"([^"]+)"|filename\s*=\s*([^;]+)/i,
    );
    const filename = filenameMatch?.[1] ?? filenameMatch?.[2];
    if (!filename) {
      return fallback;
    }

    return filename
      .trim()
      .replace(/^"|"$/g, '')
      .replace(/[\\/:*?"<>|]/g, '_');
  }
}
