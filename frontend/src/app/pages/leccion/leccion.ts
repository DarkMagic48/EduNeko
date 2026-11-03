
import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { LeccionService } from '../../services/leccion.service';
import { LeccionResponse } from '../../models/leccion.models';
import { ActividadService } from '../../services/actividad.service';
import {
  ActividadResponse,
  EvaluacionActividadResponse
} from '../../models/actividad.models';
import { ProgresoService } from '../../services/progreso.service';
import { ProgresoLeccionResponse } from '../../models/progreso.models';

@Component({
  selector: 'app-leccion',
  imports: [CommonModule],
  templateUrl: './leccion.html',
  styleUrl: './leccion.css'
})
export class Leccion implements OnInit {

  // 1. DATOS GENERALES DE LA LECCIÓN

  leccion: LeccionResponse | null = null;
  unidadId: number | null = null;

  cargando = true;
  generando = false;
  error = '';

  // 2. ACTIVIDADES INTERACTIVAS
  actividades: ActividadResponse[] = [];

  cargandoActividades = false;
  generandoActividades = false;
  errorActividades = '';

  // Almacena la respuesta seleccionada en cada actividad.
  respuestasSeleccionadas: Record<number, string> = {};

  // Guarda el resultado de evaluación de cada actividad.
  resultadosEvaluacion: Record<
    number,
    EvaluacionActividadResponse
  > = {};

  // Controla qué actividades están siendo evaluadas.
  evaluandoActividades: Record<number, boolean> = {};

  // Guarda los errores individuales de evaluación.
  erroresEvaluacion: Record<number, string> = {};

  // 3. PROGRESO DEL ESTUDIANTE

  // Datos obtenidos directamente desde PostgreSQL.
  progreso: ProgresoLeccionResponse | null = null;

  // Indica si estamos consultando el progreso.
  cargandoProgreso = false;

  // Mensaje de error independiente del resto de la lección.
  errorProgreso = '';

  constructor(
    private readonly leccionService: LeccionService,
    private readonly actividadService: ActividadService,
    private readonly progresoService: ProgresoService,
    private readonly route: ActivatedRoute,
    private readonly router: Router
  ) {}

  // 4. INICIALIZACIÓN DEL COMPONENTE
  ngOnInit(): void {

    // Obtener el identificador de la unidad desde la URL.
    const id = Number(
      this.route.snapshot.paramMap.get('unidadId')
    );

    // Evitar solicitudes con identificadores inválidos.
    if (!Number.isSafeInteger(id) || id <= 0) {
      this.cargando = false;
      this.error = 'La unidad solicitada no es válida.';
      return;
    }

    this.unidadId = id;

    // Cargar la lección correspondiente a la unidad.
    this.cargarLeccion();
  }

  // 5. CARGA DE LA LECCIÓN
  cargarLeccion(): void {

    if (this.unidadId === null) {
      return;
    }

    // Restablecer el estado antes de cargar la lección.
    this.cargando = true;
    this.generando = false;
    this.error = '';

    this.leccion = null;
    this.actividades = [];

    this.respuestasSeleccionadas = {};
    this.resultadosEvaluacion = {};
    this.evaluandoActividades = {};
    this.erroresEvaluacion = {};

    // Limpiar el progreso anterior para evitar datos obsoletos.
    this.progreso = null;
    this.errorProgreso = '';

    this.leccionService.obtenerLeccion(this.unidadId).subscribe({

      next: (respuesta) => {

        this.leccion = respuesta;
        this.cargando = false;

        // Cargar las actividades de la lección.
        this.cargarActividades();

        // Consultar el progreso previamente registrado.
        this.cargarProgreso();
      },

      error: (err: HttpErrorResponse) => {

        this.cargando = false;

        if (err.status === 404) {

          // Si todavía no existe una lección, generarla.
          this.generarLeccion();

        } else if (err.status === 403) {

          this.error = 'No tienes acceso a esta unidad.';

        } else {

          this.error = 'No fue posible cargar la lección.';
        }
      }
    });
  }

  // 6. GENERACIÓN DE UNA LECCIÓN NUEVA
  generarLeccion(): void {

    if (this.unidadId === null || this.generando) {
      return;
    }

    this.generando = true;
    this.error = '';

    this.leccionService.generarLeccion(this.unidadId).subscribe({

      next: (respuesta) => {

        this.leccion = respuesta;
        this.generando = false;

        // Preparar actividades para la nueva lección.
        this.cargarActividades();

        // Consultar el progreso inicial.
        this.cargarProgreso();
      },

      error: () => {

        this.generando = false;

        this.error =
          'No fue posible preparar tu lección. Inténtalo nuevamente.';
      }
    });
  }

  // Permite reintentar la carga de la lección.
  reintentar(): void {
    this.cargarLeccion();
  }

  // 7. CARGA DE ACTIVIDADES
  cargarActividades(): void {

    if (
      this.unidadId === null ||
      this.cargandoActividades ||
      this.generandoActividades
    ) {
      return;
    }

    this.cargandoActividades = true;
    this.errorActividades = '';

    this.actividadService.obtenerActividades(
      this.unidadId
    ).subscribe({

      next: (actividades) => {

        this.cargandoActividades = false;

        if (actividades.length > 0) {

          // Reutilizar actividades existentes.
          this.actividades = actividades;
          // Recuperar las opciones seleccionadas anteriormente a partir de los intentos guardados en PostgreSQL
          this.restaurarRespuestasGuardadas(actividades);

        } else {

          // Generar actividades solamente si no existen.
          this.generarActividades();
        }
      },

      error: () => {

        this.cargandoActividades = false;

        this.errorActividades =
          'No fue posible cargar las actividades.';
      }
    });
  }
  // 8. GENERACIÓN DE ACTIVIDADES CON IA
  generarActividades(): void {

    if (
      this.unidadId === null ||
      this.generandoActividades
    ) {
      return;
    }

    this.generandoActividades = true;
    this.errorActividades = '';

    this.actividadService.generarActividades(
      this.unidadId
    ).subscribe({

      next: (actividades) => {

        this.actividades = actividades;
        this.generandoActividades = false;

        // Actualizar el progreso inicial una vez creadas
        // las actividades, ya que ahora existe un total
        // de ejercicios que el estudiante puede resolver.
        this.cargarProgreso();
      },

      error: () => {

        this.generandoActividades = false;

        this.errorActividades =
          'No fue posible preparar los ejercicios. Inténtalo nuevamente.';
      }
    });
  }
  /**
   * Recupera las respuestas previamente registradas en PostgreSQL
   * y reconstruye el estado de las preguntas en Angular.
   *
   * Las respuestas correctas se identifican como completadas,
   * mientras que las incorrectas pueden volver a intentarse.
   */
  restaurarRespuestasGuardadas(
    actividades: ActividadResponse[]
  ): void {

    // Limpiar los datos anteriores antes de restaurar el historial.
    this.respuestasSeleccionadas = {};
    this.resultadosEvaluacion = {};
    this.erroresEvaluacion = {};

    for (const actividad of actividades) {

      // Restaurar la opción que el estudiante había seleccionado.
      if (actividad.respuestaEstudiante) {
        this.respuestasSeleccionadas[actividad.id] =
          actividad.respuestaEstudiante;
      }

      // Las preguntas acertadas anteriormente se identifican
      // mediante la información recuperada del backend.
      // No reconstruimos artificialmente una evaluación:
      // el historial se mostrará directamente en la interfaz.
    }
  }

  // 9. SELECCIÓN DE RESPUESTAS
  seleccionarRespuesta(
    actividadId: number,
    respuesta: string
  ): void {

    // Evitar cambios mientras se evalúa la actividad.
    if (this.evaluandoActividades[actividadId]) {
      return;
    }

    //Una pregunta que ya fue respondida correctamente, conserva su respuesta y sus puntos registrados
    const actividad = this.actividades.find(
      item => item.id === actividadId
    );

    if (actividad?.respondidaCorrectamente) {
      return;
    }

    // Guardar la opción seleccionada.
    this.respuestasSeleccionadas[actividadId] = respuesta;

    // Limpiar la retroalimentación anterior de esta actividad.
    delete this.resultadosEvaluacion[actividadId];
    delete this.erroresEvaluacion[actividadId];
  }

  // 10. EVALUACIÓN DE RESPUESTAS
  comprobarRespuesta(actividadId: number): void {

    if (this.unidadId === null) {
      return;
    }

    // Evitar reevaluar preguntas que ya fueron acertadas.
    const actividad = this.actividades.find(
      item => item.id === actividadId
    );

    if (actividad?.respondidaCorrectamente) {
      return;
    }

    const respuesta =
      this.respuestasSeleccionadas[actividadId];

    if (
      !respuesta ||
      this.evaluandoActividades[actividadId]
    ) {
      return;
    }

    this.evaluandoActividades[actividadId] = true;
    this.erroresEvaluacion[actividadId] = '';

    // Enviar la respuesta al backend para evaluarla.
    this.actividadService.responderActividad(
      this.unidadId,
      actividadId,
      respuesta
    ).subscribe({

      next: (resultado) => {

        // Guardar la retroalimentación de la actividad.
        this.resultadosEvaluacion[actividadId] = resultado;

        // Mantener actualizado el estado local de la pregunta.
        // Así no es necesario recargar todas las actividades.
        if (resultado.esCorrecta) {

          const actividad = this.actividades.find(
            item => item.id === actividadId
          );

          if (actividad) {
            actividad.respondidaCorrectamente = true;
            actividad.respuestaEstudiante =
              this.respuestasSeleccionadas[actividadId];
            actividad.puntosObtenidos =
              resultado.puntosObtenidos;
          }
        }

        this.evaluandoActividades[actividadId] = false;

        // Consultar nuevamente el progreso después de
        // registrar el intento y actualizar el plan.
        this.cargarProgreso();
      },

      error: () => {

        this.evaluandoActividades[actividadId] = false;

        this.erroresEvaluacion[actividadId] =
          'No fue posible evaluar tu respuesta. Inténtalo nuevamente.';
      }
    });
  }

  // 11. CONSULTA DEL PROGRESO REAL
  cargarProgreso(): void {

    if (this.unidadId === null || !this.leccion) {
      return;
    }

    this.cargandoProgreso = true;
    this.errorProgreso = '';

    // El backend calcula el progreso a partir de los
    // intentos almacenados en PostgreSQL.
    this.progresoService.obtenerProgresoLeccion(
      this.unidadId
    ).subscribe({

      next: (respuesta) => {

        // Actualizar los datos que mostrará la interfaz.
        this.progreso = respuesta;
        this.cargandoProgreso = false;
      },

      error: () => {

        // Un error de progreso no debe impedir que el
        // estudiante continúe trabajando en la lección.
        this.cargandoProgreso = false;

        this.errorProgreso =
          'No fue posible consultar tu progreso.';
      }
    });
  }

  // 12. NAVEGACIÓN
  volverAlPlan(): void {

    // Regresar al plan personalizado de Matemáticas.
    this.router.navigate(['/plan/matematicas']);
  }
}
