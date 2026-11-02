
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// Modelo que representa los datos de progreso enviados por el backend.
import { ProgresoLeccionResponse } from '../models/progreso.models';

@Injectable({
  providedIn: 'root'
})
export class ProgresoService {
  private readonly apiUrl = 'http://localhost:8080/api/progreso';

  constructor(
    private readonly http: HttpClient
  ) {}

  /**
   * Consulta el progreso real de una lección.
   *
   * El backend identifica al estudiante mediante su
   * autenticación JWT y verifica que tenga acceso
   * a la unidad solicitada.
   *
   * @param unidadId Identificador de la unidad del plan.
   * @returns Porcentaje, actividades correctas, puntos y estado.
   */
  obtenerProgresoLeccion(
    unidadId: number
  ): Observable<ProgresoLeccionResponse> {

    return this.http.get<ProgresoLeccionResponse>(
      `${this.apiUrl}/unidades/${unidadId}`
    );
  }
}
