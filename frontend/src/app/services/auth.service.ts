import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable, tap } from "rxjs";

import {
    LoginRequest,
    LoginResponse,
    RegistroRequest,
    RegistroResponse,
    UsuarioActualResponse
} from '../models/auth.models';

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    private readonly authUrl = 'http://localhost:8080/api/auth';
    private readonly tokenKey = 'eduneko_token';

    constructor(private readonly http: HttpClient) {}

    registrar(datos: RegistroRequest): Observable<RegistroResponse> {
        return this.http.post<RegistroResponse>(
            `${this.authUrl}/register`,
            datos
        );
      }

    login(datos: LoginRequest): Observable<LoginResponse> {
        return this.http.post<LoginResponse>(
            `${this.authUrl}/login`,
            datos
        ).pipe(
            tap(response => {
                this.guardarToken(response.token);
            })
        );
    }

    guardarToken(token: string): void {
        sessionStorage.setItem(this.tokenKey, token);
    }

    obtenerToken(): string | null {
        return sessionStorage.getItem(this.tokenKey);
    }

    estaAutenticado(): boolean {
        const token = this.obtenerToken();
        if (!token) return false;

        try {
            // Esta lectura solo evita navegar con un token evidentemente vencido.
            // No verifica la firma: esa responsabilidad sigue siendo del backend.
            const partes = token.split('.');
            if (partes.length !== 3) return false;
            const base64 = partes[1].replace(/-/g, '+').replace(/_/g, '/');
            const contenido = JSON.parse(atob(base64)) as { exp?: number };
            return typeof contenido.exp === 'number' &&
                contenido.exp * 1000 > Date.now();
        } catch {
            return false;
        }
    }

    logout(): void {
        sessionStorage.removeItem(this.tokenKey);
    }
    
    obtenerUsuarioActual(): Observable<UsuarioActualResponse> {
        return this.http.get<UsuarioActualResponse>(
            'http://localhost:8080/api/usuario/me'
        );
    }
}