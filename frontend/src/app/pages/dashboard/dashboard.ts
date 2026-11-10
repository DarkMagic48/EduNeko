import { Component } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { OnInit } from '@angular/core';

import { AuthService } from '../../services/auth.service';
import { ProgresoService } from '../../services/progreso.service';
import { EstudianteService } from '../../services/estudiante.service';
import { Materia } from '../../models/estudiante.models';
import { DiagnosticoService } from '../../services/diagnostico.service';
import { HttpErrorResponse } from '@angular/common/http';

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
  materiasSeleccionadas: Materia[] = [];
  cargandoMaterias = false;
  errorMaterias = '';
  verificandoDiagnostico = false;
  errorAccesoMatematicas = '';

  constructor(
    private readonly authService: AuthService,
    private readonly router: Router,
    private readonly progresoService: ProgresoService,
    private readonly estudianteService: EstudianteService,
    private readonly diagnosticoService: DiagnosticoService
  ) {}
  /**
   * Al abrir el dashboard, consulta los puntos acumulados del estudiante autenticado.
   */
  ngOnInit(): void {
    this.cargarPuntosTotales();
    this.cargarRachas();
    this.cargarMateriasSeleccionadas();
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
  
  /**
   * Recupera las materias seleccionadas por el estudiante
   * autenticado y las almacena para mostrarlas en el Dashboard.
   */
  cargarMateriasSeleccionadas(): void {

      // Indicar que la consulta está en proceso.
      this.cargandoMaterias = true;
      this.errorMaterias = '';

      this.estudianteService.obtenerMateriasSeleccionadas()
          .subscribe({

              next: (materias) => {

                  // Guardar las materias devueltas por el backend.
                  this.materiasSeleccionadas = materias;

                  // Finalizar el estado de carga.
                  this.cargandoMaterias = false;
              },

              error: () => {

                  // Evitar mostrar información incorrecta.
                  this.materiasSeleccionadas = [];

                  this.cargandoMaterias = false;

                  // Informar si la consulta no pudo completarse.
                  this.errorMaterias =
                      'No fue posible consultar tus materias. Inténtalo nuevamente.';
              }
          });
  }

  cerrarSesion(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }

  irPlanMatematicas(): void {

    // Evitar solicitudes duplicadas mientras se consulta.
    if (this.verificandoDiagnostico) {
        return;
    }

    this.verificandoDiagnostico = true;
    this.errorAccesoMatematicas = '';

    // Consultar si existe un resultado de diagnóstico.
    this.diagnosticoService.obtenerResultadoMatematicas()
        .subscribe({

            next: () => {

                // El estudiante ya completó el diagnóstico.
                this.verificandoDiagnostico = false;

                // Continuar con su plan personalizado.
                this.router.navigate(['/plan/matematicas']);
            },

            error: (error: HttpErrorResponse) => {

                this.verificandoDiagnostico = false;

                if (error.status === 404) {

                    // No existe diagnóstico completado.
                    // Dirigir al estudiante a la evaluación.
                    this.router.navigate([
                        '/diagnostico/matematicas'
                    ]);

                } else {

                    // No confundir un fallo del servidor o
                    // de conexión con un diagnóstico pendiente.
                    this.errorAccesoMatematicas =
                        'No fue posible verificar tu diagnóstico. Inténtalo nuevamente.';
                }
            }
        });
    }
}