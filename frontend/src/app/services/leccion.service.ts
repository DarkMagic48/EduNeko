import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { LeccionResponse } from '../models/leccion.models';

@Injectable({
  providedIn: 'root'
})
export class LeccionService {

  private readonly apiUrl = 'http://localhost:8080/api/lecciones';

  constructor(
    private readonly http: HttpClient
  ) {}

  obtenerLeccion(unidadId: number): Observable<LeccionResponse> {
    return this.http.get<LeccionResponse>(
      `${this.apiUrl}/unidades/${unidadId}`
    );
  }

  generarLeccion(unidadId: number): Observable<LeccionResponse> {
    return this.http.post<LeccionResponse>(
      `${this.apiUrl}/unidades/${unidadId}/generar`,
      {}
    );
  }
}