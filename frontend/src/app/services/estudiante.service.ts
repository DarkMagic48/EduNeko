import { Injectable } from "@angular/core";
import { HttpClient } from "@angular/common/http";
import { Observable } from "rxjs";

import {
    ActualizarPerfilEstudianteRequest,
    PerfilEstudianteResponse,
    PreferenciasEstudianteResponse,
    ActualizarPerfilEstudianteResponse,
    ActualizarPreferenciasRequest,
    ActualizarPreferenciasResponse,
    Materia,
    SeleccionarMateriasRequest,
    SeleccionarMateriasResponse
} from '../models/estudiante.models';

export interface EstadoOnboardingResponse {
    onboardingCompletado: boolean;
}

@Injectable({
    providedIn: 'root'
})
export class EstudianteService {

    private readonly estudianteUrl = 'http://localhost:8080/api/estudiante';

    constructor(private readonly http: HttpClient) {}

    obtenerPerfil(): Observable<PerfilEstudianteResponse> {
        return this.http.get<PerfilEstudianteResponse>(
            `${this.estudianteUrl}/perfil`
        );
    }

    obtenerPreferencias(): Observable<PreferenciasEstudianteResponse> {
        return this.http.get<PreferenciasEstudianteResponse>(
            `${this.estudianteUrl}/preferencias`
        );
    }

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

    obtenerMateriasDisponibles(): Observable<Materia[]> {
        return this.http.get<Materia[]>(
            'http://localhost:8080/api/materias'
        );
    }

    seleccionarMaterias(
        datos: SeleccionarMateriasRequest
    ): Observable<SeleccionarMateriasResponse> {

        return this.http.put<SeleccionarMateriasResponse>(
            `${this.estudianteUrl}/materias`,
            datos
        );
    }

    obtenerMateriasSeleccionadas(): Observable<Materia[]> {

        return this.http.get<Materia[]>(
            `${this.estudianteUrl}/materias`
        );
    }

    obtenerEstadoOnboarding(): Observable<EstadoOnboardingResponse> {

        return this.http.get<EstadoOnboardingResponse>(
            `${this.estudianteUrl}/estado-onboarding`
        );
    }
}