import { Component, OnInit, OnDestroy } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router } from '@angular/router';
import { HttpErrorResponse } from '@angular/common/http';
import { LeccionService } from '../../services/leccion.service';
import { LeccionResponse, ElementoPatronFiguras2D, PlanoCartesianoDatos, GraficaEstadisticaDatos } from '../../models/leccion.models';
import { ActividadService } from '../../services/actividad.service';
import {
  ActividadResponse,
  EvaluacionActividadResponse
} from '../../models/actividad.models';
import { ProgresoService } from '../../services/progreso.service';
import { ProgresoLeccionResponse } from '../../models/progreso.models';
import { NekoMensaje } from '../../shared/neko-mensaje/neko-mensaje';

@Component({
  selector: 'app-leccion',
  imports: [CommonModule, NekoMensaje],
  templateUrl: './leccion.html',
  styleUrl: './leccion.css'
})
export class Leccion implements OnInit, OnDestroy {

  // 1. DATOS GENERALES DE LA LECCIÓN

  leccion: LeccionResponse | null = null;
  unidadId: number | null = null;

  // =====================================================
  // EXPLORACIÓN INTERACTIVA DE FRACCIONES
  // =====================================================

  // Almacena los valores que modifica el estudiante.
  // No modifica los datos originales generados por IA.
  fraccionInteractiva: {
    numerador: number;
    denominador: number;
  } | null = null;

  /**
   * Inicializa la exploración con los valores del ejemplo.
   * Se utiliza una copia independiente de los datos originales.
   */
  iniciarExploracionFraccion(
    numerador: number,
    denominador: number
  ): void {

    // Impedir iniciar la exploración si el estudiante
    // no tiene activada la preferencia práctica.
    if (!this.prefierePractica) {
      return;
    }

    // Validar los límites admitidos por el componente.
    if (
      !Number.isInteger(numerador) ||
      !Number.isInteger(denominador) ||
      denominador < 1 ||
      denominador > 12 ||
      numerador < 0 ||
      numerador > denominador * 4
    ) {
      return;
    }

    // Crear una copia para la interacción del estudiante.
    this.fraccionInteractiva = {
      numerador,
      denominador
    };
  }

  /**
   * Actualiza el numerador durante la exploración.
   */
  cambiarNumeradorFraccion(valor: number): void {

    if (!this.fraccionInteractiva || !Number.isInteger(valor)) {
      return;
    }

    const maximo = this.fraccionInteractiva.denominador * 4;

    this.fraccionInteractiva.numerador =
      Math.max(0, Math.min(valor, maximo));
  }

  /**
   * Actualiza el denominador y conserva un numerador válido.
   */
  cambiarDenominadorFraccion(valor: number): void {

    if (
      !this.fraccionInteractiva ||
      !Number.isInteger(valor) ||
      valor < 1 ||
      valor > 12
    ) {
      return;
    }

    this.fraccionInteractiva.denominador = valor;

    // Ajustar el numerador si supera el límite permitido.
    this.fraccionInteractiva.numerador = Math.min(
      this.fraccionInteractiva.numerador,
      valor * 4
    );
  }

  /**
   * Finaliza la exploración y elimina los valores temporales.
   */
  cerrarExploracionFraccion(): void {
    this.fraccionInteractiva = null;
  }

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

  // 4. CONTROL DE REPRODUCCIÓN DE AUDIO

  // Verifica si el navegador permite sintetizar voz.
  audioDisponible: boolean =
    typeof window !== 'undefined' &&
    'speechSynthesis' in window &&
    'SpeechSynthesisUtterance' in window;

  // Indica si se está reproduciendo una explicación.
  reproduciendoAudio: boolean = false;
  seccionAudioActiva: 'explicacion' | 'ejemplo' | null = null;

  // Solo los estudiantes con preferencia auditiva activada
  // podrán visualizar los controles de reproducción.
  prefiereAuditivo: boolean = false;

  //Solo los estudiantes con preferencia practica activada podran utilizar los controles de exploracion matematica.
  prefierePractica: boolean = false;

  /**
 * Reproduce en voz alta el contenido de una lección.
 * Utiliza la API de síntesis de voz del navegador, sin realizar llamadas adicionales a OpenAI.
 * @param texto Contenido educativo que se leerá.
 */
  reproducirAudio(
  texto: string,
  seccion: 'explicacion' | 'ejemplo'
): void {

  // Verificar que el navegador admita audio
  // y que exista contenido para reproducir.
  if (!this.audioDisponible || !texto?.trim()) {
    return;
  }

  // Detener cualquier reproducción anterior.
  window.speechSynthesis.cancel();

  // Crear la lectura del contenido educativo.
  const lectura = new SpeechSynthesisUtterance(texto);

  lectura.lang = 'es-MX';
  lectura.rate = 0.9;
  lectura.pitch = 1;

  // Identificar la sección que está reproduciendo audio.
  this.seccionAudioActiva = seccion;
  this.reproduciendoAudio = true;

  // Restablecer el estado cuando termine la lectura.
  lectura.onend = () => {
    if (this.seccionAudioActiva === seccion) {
      this.reproduciendoAudio = false;
      this.seccionAudioActiva = null;
    }
  };

  // Restablecer el estado si ocurre un error.
  lectura.onerror = () => {
    if (this.seccionAudioActiva === seccion) {
      this.reproduciendoAudio = false;
      this.seccionAudioActiva = null;
    }
  };

    // Iniciar la reproducción.
    window.speechSynthesis.speak(lectura);
  }

  /**
   * Consulta las preferencias de aprendizaje del estudiante.
   *
   * Habilita la interfaz de audio únicamente cuando la preferencia auditiva está activada.
   */
  cargarPreferenciaAuditiva(): void {

    // Mantener ocultos los controles hasta confirmar la preferencia.
    this.prefiereAuditivo = false;
    this.prefierePractica = false;

    if (this.unidadId === null) {
      return;
    }

    this.leccionService.obtenerContextoUnidad(this.unidadId)
      .subscribe({
        next: (contexto) => {
          this.prefiereAuditivo =
            contexto.preferenciasAprendizaje?.prefiere_auditivo === true;

          this.prefierePractica =
            contexto.preferenciasAprendizaje?.prefiere_practica === true;

          //Si la practica no esta habilitada, cerrar cualquier exploracion existente.
          if (!this.prefierePractica) {
            this.cerrarExploracionFraccion();
          }
        },
        error: (error) => {
          console.error(
            'No fue posible consultar las preferencias de aprendizaje.',
            error
          );

          // Desactivar las funcionalidades condicionadas si no es posible verificar las preferencias.
          this.prefiereAuditivo = false;
          this.prefierePractica = false;

          this.detenerAudio();
          this.cerrarExploracionFraccion();
        }
      });
  }

  /**
 * Detiene la explicación que se está reproduciendo.
 */
  detenerAudio(): void {

    if (!this.audioDisponible) {
      return;
    }

    window.speechSynthesis.cancel();

    this.reproduciendoAudio = false;
    this.seccionAudioActiva = null;
  }

  /**
   * Detiene el audio al abandonar la página
   * para evitar que continúe sonando en otra pantalla.
   */
  ngOnDestroy(): void {
    this.detenerAudio();
  }

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

    //Consultar las preferencias de aprendizaje del estudiante.
    this.cargarPreferenciaAuditiva();
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

        // 1. Mostrar inmediatamente la lección existente.
        this.leccion = respuesta;
        this.cargando = false;

        // 2. Recuperar actividades y progreso sin modificarlos.
        this.cargarActividades();
        this.cargarProgreso();

        // 3. Si ya existe un recurso visual, conservarlo.
        // No es necesario solicitar otro a OpenAI.
        if (respuesta.recurso_visual) {
          return;
        }

        // 4. Intentar enriquecer la lección existente
        // utilizando los seis recursos visuales disponibles.
        this.leccionService
          .enriquecerLeccionVisualmente(this.unidadId!)
          .subscribe({

            next: (leccionEnriquecida) => {

              // 5. Actualizar solamente el recurso visual.
              // No se reemplazan la explicación, las actividades
              // ni el progreso del estudiante.
              if (
                this.leccion &&
                this.leccion.id === leccionEnriquecida.id
              ) {
                this.leccion = {
                  ...this.leccion,
                  recurso_visual: leccionEnriquecida.recurso_visual
                };
              }
            },

            error: (error) => {

              // Una falla del enriquecimiento no debe
              // impedir que el estudiante estudie.
              console.error(
                'No fue posible enriquecer la lección existente.',
                error
              );
            }
          });
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

  // 1. Generar, revisar, corregir y guardar la lección.
  this.leccionService.generarLeccion(this.unidadId).subscribe({

    next: (respuesta) => {

      // Mostrar inmediatamente la lección generada.
      this.leccion = respuesta;
      this.generando = false;

      // Preparar las actividades y consultar el progreso.
      this.cargarActividades();
      this.cargarProgreso();

      // 2. Si ya existe un recurso visual, no es necesario
      // solicitar otro ni realizar una llamada adicional a OpenAI.
      if (respuesta.recurso_visual) {
        return;
      }

      // 3. Solicitar el enriquecimiento visual de la lección.
      // El backend selecciona y valida uno de los seis
      // componentes visuales disponibles.
      this.leccionService
        .enriquecerLeccionVisualmente(this.unidadId!)
        .subscribe({

          next: (leccionEnriquecida) => {

            // Actualizar únicamente el recurso visual.
            // Se conserva el contenido educativo mostrado.
            if (this.leccion) {
              this.leccion = {
                ...this.leccion,
                recurso_visual:
                  leccionEnriquecida.recurso_visual
              };
            }
          },

          error: (error) => {

            // Una falla del enriquecimiento no debe impedir
            // que el estudiante continúe con su lección.
            console.error(
              'No fue posible enriquecer visualmente la lección.',
              error
            );
          }
        });
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

  /**
   * Genera los segmentos de una barra fraccionaria.
   *
   * Ejemplo:
   * denominador = 4
   * Resultado: [0, 1, 2, 3]
   *
   * Angular utilizará estos segmentos para representar visualmente las partes iguales de una fracción.
   *
   * Se comprueba el denominador para evitar generar segmentos a partir de datos inválidos.
   */
  obtenerSegmentosFraccion(denominador: number): number[] {

    // Solo se permiten denominadores enteros entre 1 y 12.
    if (
      !Number.isInteger(denominador) ||
      denominador < 1 ||
      denominador > 12
    ) {
      return [];
    }

    // Crea un segmento por cada parte de la fracción.
    return Array.from(
      { length: denominador },
      (_, indice) => indice
    );
  }

  obtenerBarrasFraccion(
    numerador: number,
    denominador: number
  ): number[] {

    // Validar los límites establecidos para EduNeko V1.
    if (
      !Number.isInteger(numerador) ||
      !Number.isInteger(denominador) ||
      denominador < 1 ||
      denominador > 12 ||
      numerador < 0 ||
      numerador > denominador * 4
    ) {
      return [];
    }

    // Siempre mostramos al menos una barra,
    // incluso cuando el numerador es cero.
    const cantidadBarras = Math.max(
      1,
      Math.ceil(numerador / denominador)
    );

    // Generar los índices de las barras.
    return Array.from(
      { length: cantidadBarras },
      (_, indice) => indice
    );
  }

  obtenerMarcasRectaNumerica(
    minimo: number,
    maximo: number,
    intervalo: number
  ): number[] {

    // Validar números finitos y un intervalo positivo.
    if (
      !Number.isFinite(minimo) ||
      !Number.isFinite(maximo) ||
      !Number.isFinite(intervalo) ||
      minimo >= maximo ||
      intervalo <= 0
    ) {
      return [];
    }

    // Evitar más de 21 marcas en una sola recta.
    const cantidadIntervalos = (maximo - minimo) / intervalo;

    if (cantidadIntervalos > 20) {
      return [];
    }

    const marcas: number[] = [];

    // Generar marcas sin acumular errores de suma decimal.
    for (
      let indice = 0;
      indice <= Math.floor(cantidadIntervalos + 1e-9);
      indice++
    ) {
      const valor = minimo + indice * intervalo;

      // Redondear pequeñas imprecisiones decimales.
      marcas.push(Number(valor.toFixed(8)));
    }

    return marcas;
  }

  /**
   * Calcula la posición horizontal de un número
   * dentro de una recta numérica.
   *
   * Devuelve un porcentaje entre 0 y 100.
   *
   * Ejemplo:
   * mínimo = -5, máximo = 5, valor = -2
   * Resultado = 30%
   */
  obtenerPosicionRectaNumerica(
    valor: number,
    minimo: number,
    maximo: number
  ): number {

    // Evitar cálculos con valores inválidos.
    if (
      !Number.isFinite(valor) ||
      !Number.isFinite(minimo) ||
      !Number.isFinite(maximo) ||
      minimo >= maximo ||
      valor < minimo ||
      valor > maximo
    ) {
      return 0;
    }

    // Convertir el valor matemático a porcentaje.
    return ((valor - minimo) / (maximo - minimo)) * 100;
  }

  /**
   * Obtiene la dimensión más grande de una sucesión geométrica.
   *
   * Permite que todas las figuras utilicen una misma escala,
   * conservando sus proporciones relativas.
   *
   * Ejemplo:
   * Cuadrados con lados de 1, 2, 3 y 4 cm.
   * Resultado: 4.
   *
   * No modifica los datos originales.
   */
  obtenerDimensionMaximaPatron(
    elementos: ElementoPatronFiguras2D[]
  ): number {

    // Valor mínimo seguro para evitar divisiones entre cero.
    let dimensionMaxima = 1;

    // Examinar las dimensiones de todas las figuras.
    for (const elemento of elementos) {

      // Reunir las dimensiones disponibles.
      const dimensiones = [
        elemento.lado,
        elemento.base,
        elemento.altura,
        elemento.radio
      ];

      for (const dimension of dimensiones) {

        // Ignorar valores ausentes, negativos o no finitos.
        if (
          typeof dimension === 'number' &&
          Number.isFinite(dimension) &&
          dimension > 0
        ) {
          dimensionMaxima = Math.max(
            dimensionMaxima,
            dimension
          );
        }
      }
    }

    return dimensionMaxima;
  }

    /**
   * Genera las marcas numéricas para los ejes
   * horizontal y vertical del plano cartesiano.
   */
  obtenerMarcasPlano(
    minimo: number,
    maximo: number,
    intervalo: number
  ): number[] {

    // Comprobar que los valores sean válidos.
    if (
      !Number.isFinite(minimo) ||
      !Number.isFinite(maximo) ||
      !Number.isFinite(intervalo) ||
      maximo <= minimo ||
      intervalo <= 0
    ) {
      return [];
    }

    // Limitar la cuadrícula a un máximo de 21 marcas.
    const cantidadIntervalos = (maximo - minimo) / intervalo;

    if (!Number.isFinite(cantidadIntervalos) ||
        cantidadIntervalos > 20) {
      return [];
    }

    const marcas: number[] = [];

    // Calcular las marcas evitando errores acumulados.
    for (
      let i = 0;
      i <= Math.floor(cantidadIntervalos + 1e-9);
      i++
    ) {
      const valor = minimo + i * intervalo;
      marcas.push(Number(valor.toFixed(6)));
    }

    return marcas;
  }

  /**
   * Convierte una coordenada X en una posición
   * horizontal dentro del SVG de 400 × 400.
   */
  obtenerPosicionXPlano(
    x: number,
    datos: PlanoCartesianoDatos
  ): number {

    const rango = datos.maximoX - datos.minimoX;

    // Evitar divisiones entre cero o valores inválidos.
    if (
      !Number.isFinite(x) ||
      !Number.isFinite(rango) ||
      rango <= 0
    ) {
      return 30;
    }

    // El área horizontal disponible va de 30 a 370.
    const proporcion = (x - datos.minimoX) / rango;

    return 30 + proporcion * 340;
  }

  /**
   * Convierte una coordenada Y en una posición
   * vertical dentro del SVG.
   *
   * Se invierte el eje porque en SVG los valores
   * verticales aumentan hacia abajo.
   */
  obtenerPosicionYPlano(
    y: number,
    datos: PlanoCartesianoDatos
  ): number {

    const rango = datos.maximoY - datos.minimoY;

    // Evitar divisiones entre cero o valores inválidos.
    if (
      !Number.isFinite(y) ||
      !Number.isFinite(rango) ||
      rango <= 0
    ) {
      return 370;
    }

    const proporcion = (y - datos.minimoY) / rango;

    return 370 - proporcion * 340;
  }

  // =====================================================
  // RECURSO VISUAL: GRÁFICAS ESTADÍSTICAS
  //
  // Estas funciones calculan las posiciones necesarias
  // para dibujar barras y líneas mediante SVG.
  //
  // Área de dibujo:
  // X: 50 a 370
  // Y: 30 a 330
  // =====================================================

  /**
   * Obtiene el valor máximo de los datos estadísticos.
   *
   * Se utiliza para establecer la escala vertical.
   * Si todos los valores son cero, devuelve 1 para
   * evitar divisiones entre cero.
   */
  obtenerMaximoGrafica(
    datos: GraficaEstadisticaDatos
  ): number {

    if (!datos.elementos || datos.elementos.length === 0) {
      return 1;
    }

    const valores = datos.elementos.map(
      elemento => elemento.valor
    );

    // Rechazar valores incompatibles con la gráfica.
    if (
      valores.some(
        valor => !Number.isFinite(valor) || valor < 0
      )
    ) {
      return 1;
    }

    return Math.max(1, ...valores);
  }

  /**
   * Calcula la posición horizontal del centro
   * de cada categoría.
   *
   * Distribuye uniformemente hasta ocho elementos
   * dentro del área disponible.
   */
  obtenerPosicionXGrafica(
    indice: number,
    datos: GraficaEstadisticaDatos
  ): number {

    const cantidad = datos.elementos?.length ?? 0;

    if (
      cantidad < 1 ||
      cantidad > 8 ||
      indice < 0 ||
      indice >= cantidad
    ) {
      return 50;
    }

    const anchoDisponible = 320;
    const anchoCategoria = anchoDisponible / cantidad;

    return 50 + anchoCategoria * (indice + 0.5);
  }

  /**
   * Convierte un valor estadístico en su
   * posición vertical dentro del SVG.
   *
   * Los valores mayores se dibujan más arriba.
   */
  obtenerPosicionYGrafica(
    valor: number,
    datos: GraficaEstadisticaDatos
  ): number {

    if (!Number.isFinite(valor) || valor < 0) {
      return 330;
    }

    const maximo = this.obtenerMaximoGrafica(datos);

    // El área vertical tiene 300 unidades.
    const proporcion = Math.min(valor / maximo, 1);

    return 330 - proporcion * 300;
  }

  /**
   * Calcula la altura de una barra.
   *
   * La altura se obtiene restando la posición
   * vertical del valor a la línea base.
   */
  obtenerAlturaBarra(
    valor: number,
    datos: GraficaEstadisticaDatos
  ): number {

    return 330 - this.obtenerPosicionYGrafica(
      valor,
      datos
    );
  }

  /**
   * Calcula el ancho de cada barra.
   *
   * Deja espacio entre las categorías para
   * mejorar la legibilidad.
   */
  obtenerAnchoBarra(
    datos: GraficaEstadisticaDatos
  ): number {

    const cantidad = datos.elementos?.length ?? 0;

    if (cantidad < 1 || cantidad > 8) {
      return 0;
    }

    const anchoCategoria = 320 / cantidad;

    return anchoCategoria * 0.6;
  }

  /**
   * Genera cinco marcas para la escala vertical:
   * 0 %, 25 %, 50 %, 75 % y 100 % del máximo.
   *
   * Las marcas se utilizarán tanto en gráficas
   * de barras como en gráficas de líneas.
   */
  obtenerMarcasGrafica(
    datos: GraficaEstadisticaDatos
  ): number[] {

    const maximo = this.obtenerMaximoGrafica(datos);

    return Array.from(
      { length: 5 },
      (_, indice) => Number(
        ((maximo * indice) / 4).toFixed(2)
      )
    );
  }

  // 12. NAVEGACIÓN
  volverAlPlan(): void {

    // Regresar al plan personalizado de Matemáticas.
    this.router.navigate(['/plan/matematicas']);
  }
}
