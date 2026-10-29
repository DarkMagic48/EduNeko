
import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import {
  GenerarPlanResponse,
  PlanAprendizajeResponse
} from '../models/plan-aprendizaje.models';

@Injectable({
  providedIn: 'root'
})
export class PlanAprendizajeService {

  private readonly planesUrl =
    'http://localhost:8080/api/planes/matematicas';

  constructor(private readonly http: HttpClient) {}

  generarPlanMatematicas(): Observable<GenerarPlanResponse> {
    return this.http.post<GenerarPlanResponse>(
      `${this.planesUrl}/generar`,
      {}
    );
  }

  obtenerPlanActualMatematicas(): Observable<PlanAprendizajeResponse> {
    return this.http.get<PlanAprendizajeResponse>(
      `${this.planesUrl}/actual`
    );
  }
}
