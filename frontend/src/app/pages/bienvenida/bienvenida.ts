import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-bienvenida',
  imports: [],
  templateUrl: './bienvenida.html',
  styleUrl: './bienvenida.css'
})
export class Bienvenida {

  constructor(
    private readonly router: Router
  ) {}

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
