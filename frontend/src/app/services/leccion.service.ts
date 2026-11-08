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

  obtenerContextoUnidad(unidadId: number): Observable<{
    preferenciasAprendizaje: {
      prefiere_auditivo: boolean;
      prefiere_practica: boolean;
      prefiere_visual: boolean;
      prefiere_lectura_escritura: boolean;
    };
  }> {
    return this.http.get<{
      preferenciasAprendizaje: {
        prefiere_auditivo: boolean;
        prefiere_practica: boolean;
        prefiere_visual: boolean;
        prefiere_lectura_escritura: boolean;
      };
    }>(
      `${this.apiUrl}/unidades/${unidadId}/contexto`
    );
  }

  generarLeccion(unidadId: number): Observable<LeccionResponse> {
    return this.http.post<LeccionResponse>(
      `${this.apiUrl}/unidades/${unidadId}/generar`,
      {}
    );
  }

    /**
   * Solicita al backend enriquecer una lección existente
   * con uno de los seis recursos visuales disponibles.
   *
   * El backend:
   * - Conserva el contenido educativo original.
   * - No reemplaza recursos visuales existentes.
   * - Valida la estructura y coherencia matemática.
   * - Devuelve la lección con el recurso actualizado.
   */
  enriquecerLeccionVisualmente(
    unidadId: number
  ): Observable<LeccionResponse> {

    return this.http.post<LeccionResponse>(
      `${this.apiUrl}/unidades/${unidadId}/enriquecer-visual`,
      {}
    );
  }
}