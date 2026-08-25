import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';

export interface HealthResponse {
  status: string;
  application: string;
}

@Injectable({
  providedIn: 'root'
})
export class ApiService {

  private readonly apiUrl = 'http://localhost:8080/api';

  constructor(private readonly http: HttpClient) {}

  getHealth(): Observable<HealthResponse> {
    return this.http.get<HealthResponse>(
      `${this.apiUrl}/health`
    );
  }
}