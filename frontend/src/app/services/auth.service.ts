import { HttpClient } from "@angular/common/http";
import { Injectable } from "@angular/core";
import { Observable } from "rxjs";

import {
    LoginRequest,
    LoginResponse,
    RegistroRequest,
    RegistroResponse
} from '../models/auth.models';

@Injectable({
    providedIn: 'root'
})
export class AuthService {

    private readonly authUrl = 'http://localhost:8080/api/auth';

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
        );
    }

}