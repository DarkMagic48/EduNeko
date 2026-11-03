import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { OnInit } from '@angular/core';

import { AuthService } from '../../services/auth.service';
import { ProgresoService } from '../../services/progreso.service';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {

  puntosTotales = 0;
  cargandoPuntos = false;
  errorPuntos = '';
  rachaActual = 0;
  rachaMaxima = 0;
  cargandoRachas = false;
  errorRachas = '';

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly progresoService: ProgresoService
  ) {}
  /**
   * Al abrir el dashboard, consulta los puntos acumulados del estudiante autenticado.
   */
  ngOnInit(): void {
    this.cargarPuntosTotales();
    this.cargarRachas();
  }
  /**
   * Obtiene los puntos registrados en PostgreSQL mediante el servicio de progreso.
   */
  cargarPuntosTotales(): void {

    // Activar el indicador de carga y limpiar errores.
    this.cargandoPuntos = true;
    this.errorPuntos = '';

    this.progresoService.obtenerPuntosTotales().subscribe({

      next: (respuesta) => {

        // Guardar el total recibido desde el backend.
        this.puntosTotales = respuesta.puntosTotales;
        this.cargandoPuntos = false;
      },

      error: () => {

        // No mostrar un total incorrecto si falla la consulta.
        this.puntosTotales = 0;
        this.cargandoPuntos = false;
        this.errorPuntos =
          'No fue posible consultar tus puntos. Inténtalo nuevamente.';
      }
    });
  }

  cargarRachas(): void {

  // Iniciar la consulta y limpiar errores anteriores.
  this.cargandoRachas = true;
  this.errorRachas = '';

  this.progresoService.obtenerRachas().subscribe({

    next: (respuesta) => {

      // Guardar los valores reales recibidos del backend.
      this.rachaActual = respuesta.rachaActual;
      this.rachaMaxima = respuesta.rachaMaxima;

      this.cargandoRachas = false;
    },

    error: () => {

        // Evitar mostrar valores anteriores si falla la consulta.
        this.rachaActual = 0;
        this.rachaMaxima = 0;

        this.cargandoRachas = false;
        this.errorRachas =
          'No fue posible consultar tus rachas. Inténtalo nuevamente.';
      }
    });
  }

  cerrarSesion(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  irPlanMatematicas(): void {
    this.router.navigate(['/plan/matematicas']);
  }
}