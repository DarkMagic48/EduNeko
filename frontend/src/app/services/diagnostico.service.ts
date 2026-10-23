import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

import { 
    DiagnosticoInicioResponse,
    FinalizarDiagnosticoRequest,
    DiagnosticoResultadoResponse 
} from '../models/diagnostico.models';

@Injectable({
  providedIn: 'root'
})
export class DiagnosticoService {

  private readonly diagnosticoUrl =
    'http://localhost:8080/api/diagnosticos/matematicas';

  constructor(private readonly http: HttpClient) {}

  iniciarDiagnostico(): Observable<DiagnosticoInicioResponse> {
  return this.http.post<DiagnosticoInicioResponse>(
    `${this.diagnosticoUrl}/iniciar`,
    {}
    );
  }

  finalizarDiagnostico(
  datos: FinalizarDiagnosticoRequest
    ): Observable<DiagnosticoResultadoResponse> {
    return this.http.post<DiagnosticoResultadoResponse>(
        `${this.diagnosticoUrl}/finalizar`,
        datos
    );
  }

  obtenerResultadoMatematicas(): Observable<DiagnosticoResultadoResponse> {
    return this.http.get<DiagnosticoResultadoResponse>(
      `${this.diagnosticoUrl}/resultado`
    );
  }
}