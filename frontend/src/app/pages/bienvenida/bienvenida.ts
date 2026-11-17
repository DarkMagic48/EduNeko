import { Component } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-bienvenida',
  imports: [],
  templateUrl: './bienvenida.html',
  styleUrl: './bienvenida.css'
})
export class Bienvenida {

  constructor(
    private readonly router: Router,
    private readonly authService: AuthService
  ) {}

  volverAcceso(): void {
    // Retroceder a login implica finalizar la sesión de forma explícita.
    this.authService.logout();
    void this.router.navigate(['/login']);
  }

  /**
   * Inicia la configuración académica del estudiante.
   *
   * Reutiliza el onboarding existente para conservar
   * las funcionalidades de perfil, preferencias
   * y selección de materias.
   */
  comenzarConfiguracion(): void {

    // Navegar al flujo de configuración inicial.
    this.router.navigate(['/onboarding']);
  }
}
