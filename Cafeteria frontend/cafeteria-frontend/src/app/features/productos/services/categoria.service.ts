import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { environment } from 'src/environments/environment';
import { Categoria, CrearCategoriaRequest } from '../models/categoria.model';

@Injectable({
  providedIn: 'root',
})
export class CategoriaService {
  private readonly baseURL = `${environment.apiUrl}/categoria`;

  constructor(private readonly http: HttpClient) {}

  obtenerCategorias(): Observable<Categoria[]> {
    return this.http.get<Categoria[]>(this.baseURL);
  }

  crearCategoria(crearCategoria: CrearCategoriaRequest): Observable<Categoria> {
    return this.http.post<Categoria>(this.baseURL, crearCategoria);
  }
}

