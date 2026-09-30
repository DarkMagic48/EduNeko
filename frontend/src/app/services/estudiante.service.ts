import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { from, Observable } from "rxjs";

import {
    ActualizarPerfilEstudianteRequest,
    ActualizarPerfilEstudianteResponse,
    ActualizarPreferenciasRequest,
    ActualizarPreferenciasResponse
} from '../models/estudiante.models';

@Injectable({
    providedIn: 'root'
})
export class EstudianteService {

    private readonly estudianteUrl = 'http://localhost:8080/api/estudiante';

    constructor(private readonly http: HttpClient) {}

    actualizarPerfil(
        datos: ActualizarPerfilEstudianteRequest
    ): Observable<ActualizarPerfilEstudianteResponse>{

        return this.http.put<ActualizarPerfilEstudianteResponse>(
            `${this.estudianteUrl}/perfil`,
            datos
        );
    }

    actualizarPreferencias(
        datos: ActualizarPreferenciasRequest
    ): Observable<ActualizarPreferenciasResponse> {

        return this.http.put<ActualizarPreferenciasResponse>(
            `${this.estudianteUrl}/preferencias`,
            datos
        );
    }
}