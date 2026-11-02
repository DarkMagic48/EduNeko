import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { 
  ActividadResponse,
  EvaluacionActividadResponse
 } from '../models/actividad.models';

@Injectable({
  providedIn: 'root'
})
export class ActividadService {

  private readonly apiUrl =
    'http://localhost:8080/api/actividades';

  constructor(
    private readonly http: HttpClient
  ) {}

  obtenerActividades(
    unidadId: number
  ): Observable<ActividadResponse[]> {

    return this.http.get<ActividadResponse[]>(
      `${this.apiUrl}/unidades/${unidadId}`
    );
  }

  generarActividades(
    unidadId: number
  ): Observable<ActividadResponse[]> {

    return this.http.post<ActividadResponse[]>(
      `${this.apiUrl}/unidades/${unidadId}/generar`,
      {}
    );
  }

  responderActividad(
    unidadId: number,
    actividadId: number,
    respuesta: string
  ): Observable<EvaluacionActividadResponse> {

    return this.http.post<EvaluacionActividadResponse>(
      `${this.apiUrl}/unidades/${unidadId}/${actividadId}/responder`,
      { respuesta }
    );
  }
}