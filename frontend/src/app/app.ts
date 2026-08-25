import { Component } from '@angular/core';
import { ApiService } from './services/api.service';

@Component({
  selector: 'app-root',
  standalone: true,
  templateUrl: './app.html',
  styleUrl: './app.css'
})
export class App {

  estadoBackend = 'Comprobando...';

  constructor(private readonly apiService: ApiService) {
    this.apiService.getHealth().subscribe({
      next: response => {
        this.estadoBackend =
          `${response.application}: ${response.status}`;
      },
      error: error => {
        console.error(error);
        this.estadoBackend =
          'No se pudo conectar con el backend';
      }
    });
  }
}