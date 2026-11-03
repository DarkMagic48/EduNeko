import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

// Modelo que representa los datos de progreso enviados por el backend.
import { ProgresoLeccionResponse } from '../models/progreso.models';

export interface PuntosTotalesResponse {
  puntosTotales: number;
}
export interface RachasResponse {
  rachaActual: number;
  rachaMaxima: number;
}
export interface LogroResponse {
  codigo: string;
  nombre: string;
  descripcion: string;
  desbloqueado: boolean;
}
export interface LogrosResponse {
  logros: LogroResponse[];
}

@Injectable({
  providedIn: 'root'
})
export class ProgresoService {
  private readonly apiUrl = 'http://localhost:8080/api/progreso';

  constructor(
    private readonly http: HttpClient
  ) {}

  /**
   * El backend identifica al estudiante mediante su autenticación JWT y verifica que tenga acceso a la unidad solicitada.
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

  /**
   * El backend identifica al estudiante mediante su JWT.
   * Los puntos incluyen todas las actividades evaluadas y se calculan a partir de los registros de PostgreSQL.
   * @returns Total de puntos acumulados del estudiante.
   */
  obtenerPuntosTotales(): Observable<PuntosTotalesResponse> {

    return this.http.get<PuntosTotalesResponse>(
      `${this.apiUrl}/puntos`
    );
  }

  obtenerRachas(): Observable<RachasResponse> {

    return this.http.get<RachasResponse>(
      `${this.apiUrl}/rachas`
    );
  }

  obtenerLogros(): Observable<LogrosResponse> {

    return this.http.get<LogrosResponse>(
      `${this.apiUrl}/logros`
    );
  }
}
