import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { NekoMensaje } from '../../shared/neko-mensaje/neko-mensaje';

import {
  ProgresoService,
  LogroResponse
} from '../../services/progreso.service';

/**
 * Página independiente de logros e insignias de EduNeko.
 *
 * Consulta las insignias del estudiante autenticado utilizando el endpoint GET /api/progreso/logros.
 *
 * Los estados de desbloqueo se calculan en el backend, por lo que Angular solamente presenta la información.
 */
@Component({
  selector: 'app-logros',
  imports: [NekoMensaje],
  templateUrl: './logros.html',
  styleUrl: './logros.css'
})
export class Logros implements OnInit {

  // Insignias obtenidas desde el backend.
  logros: LogroResponse[] = [];

  // Estado de carga de la información.
  cargandoLogros = false;

  // Mensaje en caso de error al consultar las insignias.
  errorLogros = '';

  constructor(
    private readonly progresoService: ProgresoService,
    private readonly router: Router
  ) {}


   // Consultar las insignias al ingresar a la página.
  ngOnInit(): void {
    this.cargarLogros();
  }

  /**
   * Recupera el catálogo de logros del estudiante.
   *
   * La consulta utiliza la autenticación JWT configurada en la aplicación.
   */
  cargarLogros(): void {

    // Iniciar la carga y limpiar errores anteriores.
    this.cargandoLogros = true;
    this.errorLogros = '';

    this.progresoService.obtenerLogros().subscribe({

      next: (respuesta) => {

        // Guardar las insignias recibidas del backend.
        this.logros = respuesta.logros;

        // Finalizar el estado de carga.
        this.cargandoLogros = false;
      },

      error: () => {

        // Limpiar los datos para evitar información desactualizada.
        this.logros = [];

        this.cargandoLogros = false;

        this.errorLogros =
          'No fue posible consultar tus logros. Inténtalo nuevamente.';
      }
    });
  }

    obtenerMensajeNeko(): string {

    // Identificar si el estudiante desbloqueó un logro.
    const tieneLogro = (codigo: string): boolean =>
      this.logros.some(
        logro => logro.codigo === codigo && logro.desbloqueado
      );

    // Priorizar el logro de constancia.
    if (tieneLogro('CONSTANCIA')) {
      return '🔥 ¡Tres días aprendiendo! Tu constancia está dando frutos.';
    }

    // Reconocer los primeros 50 puntos XP.
    if (tieneLogro('APRENDIZ_DESTACADO')) {
      return '⭐ ¡Ya tienes 50 XP! Sigue así, estás haciendo un gran trabajo.';
    }

    // Celebrar la primera lección completada.
    if (tieneLogro('PRIMERA_LECCION')) {
      return '📚 ¡Increíble! Terminaste tu primera lección. ¡Vamos por más!';
    }

    // Celebrar el primer acierto.
    if (tieneLogro('PRIMER_ACIERTO')) {
      return '🎯 ¡Muy bien! Has conseguido tu primer acierto.';
    }

    // Mensaje para estudiantes que todavía no tienen logros.
    return '🐱 ¡Bienvenido! Cada gran aventura comienza con un pequeño paso.';
  }
  // Regresa al dashboard principal del estudiante.

  volverAlDashboard(): void {
    this.router.navigate(['/dashboard']);
  }
}