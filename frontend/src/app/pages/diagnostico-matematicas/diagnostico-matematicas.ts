import { Component, OnInit, inject } from '@angular/core';
import { Router } from '@angular/router';
import { DiagnosticoService } from '../../services/diagnostico.service';
import { DiagnosticoInicioResponse } from '../../models/diagnostico.models';
import { DiagnosticoResultadoResponse } from '../../models/diagnostico.models';

@Component({
  selector: 'app-diagnostico-matematicas',
  standalone: true,
  imports: [],
  templateUrl: './diagnostico-matematicas.html',
  styleUrl: './diagnostico-matematicas.css'
})
export class DiagnosticoMatematicas implements OnInit {

  private readonly diagnosticoService = inject(DiagnosticoService);
  private readonly router = inject(Router);

  diagnostico: DiagnosticoInicioResponse | null = null;

  cargando = false;
  error = '';
  indicePreguntaActual = 0;
  finalizando = false;
  errorFinalizacion = '';
  resultado: DiagnosticoResultadoResponse | null = null;

  respuestasSeleccionadas: Record<number, number> = {};

  ngOnInit(): void {
    this.verificarEstadoDiagnostico();
  }

  verificarEstadoDiagnostico(): void {
  this.cargando = true;
  this.error = '';

  this.diagnosticoService.obtenerResultadoMatematicas().subscribe({
    next: (respuesta) => {
      this.resultado = respuesta;
      this.diagnostico = null;
      this.cargando = false;
    },
    error: (error) => {
      if (error.status === 404) {
        this.cargarDiagnostico();
      } else {
        console.error('Error al verificar el diagnóstico:', error);
        this.error = 'No fue posible consultar el estado del diagnóstico.';
        this.cargando = false;
      }
    }
  });
  }

  cargarDiagnostico(): void {
    this.cargando = true;
    this.error = '';

    this.diagnosticoService.iniciarDiagnostico().subscribe({
      next: (respuesta) => {

        // Cargar el intento vigente desde el backend.
        this.diagnostico = respuesta;

        // Recuperar respuestas y posición guardadas
        // previamente en este navegador.
        this.recuperarProgresoLocal();

        this.cargando = false;

        console.log(
          'Diagnóstico cargado:',
          respuesta.preguntas.length,
          'preguntas'
        );
      },
      error: (error) => {
        console.error('Error al cargar diagnóstico:', error);

        this.error = 'No fue posible cargar el diagnóstico de Matemáticas.';
        this.cargando = false;
      }
    });
  }

    seleccionarRespuesta(preguntaId: number, opcionId: number): void {

    // Registrar la opción seleccionada por el estudiante.
    this.respuestasSeleccionadas[preguntaId] = opcionId;

    // Conservar temporalmente las respuestas en el navegador.
    this.guardarProgresoLocal();
  }

  guardarProgresoLocal(): boolean {

  // No guardar si no existe un diagnóstico activo.
  if (!this.diagnostico || this.resultado) {
    return false;
  }

  // Identificar el intento para evitar mezclar diagnósticos.
  const clave = `eduneko_diagnostico_${this.diagnostico.intentoId}`;

  // Preparar los datos que se conservarán.
  const progreso = {
    intentoId: this.diagnostico.intentoId,
    respuestas: this.respuestasSeleccionadas,
    indicePreguntaActual: this.indicePreguntaActual
  };

  try {

    // Guardar las respuestas y la posición actual.
    localStorage.setItem(clave, JSON.stringify(progreso));

    return true;

  } catch (error) {

    console.error(
      'No fue posible guardar el progreso del diagnóstico:',
      error
    );

    return false;
  }
}

  recuperarProgresoLocal(): void {

  if (!this.diagnostico || this.resultado) {
    return;
  }

  const clave = `eduneko_diagnostico_${this.diagnostico.intentoId}`;

  try {
    const datosGuardados = localStorage.getItem(clave);

    if (!datosGuardados) {
      return;
    }

    const progreso = JSON.parse(datosGuardados);

    // Verificar que corresponda al intento actual.
    if (
      !progreso ||
      progreso.intentoId !== this.diagnostico.intentoId
    ) {
      return;
    }

    // Recuperar únicamente respuestas correspondientes
    // a preguntas y opciones válidas del diagnóstico.
    const respuestasValidas: Record<number, number> = {};

    for (const pregunta of this.diagnostico.preguntas) {

      const opcionId = progreso.respuestas?.[pregunta.id];

      const opcionValida = pregunta.opciones.some(
        opcion => opcion.id === opcionId
      );

      if (opcionValida) {
        respuestasValidas[pregunta.id] = opcionId;
      }
    }

    this.respuestasSeleccionadas = respuestasValidas;

    // Restaurar la posición únicamente si es válida.
    const indice = progreso.indicePreguntaActual;

    if (
      Number.isInteger(indice) &&
      indice >= 0 &&
      indice < this.diagnostico.preguntas.length
    ) {
      this.indicePreguntaActual = indice;
    }

    } catch (error) {
      console.error(
        'No fue posible recuperar el progreso del diagnóstico:',
        error
      );
    }
  }

  siguientePregunta(): void {

  if (
    this.diagnostico &&
    this.indicePreguntaActual < this.diagnostico.preguntas.length - 1
  ) {
    this.indicePreguntaActual++;

      // Guardar la nueva posición del diagnóstico.
      this.guardarProgresoLocal();
    }
  }

  preguntaAnterior(): void {

  if (this.indicePreguntaActual > 0) {
    this.indicePreguntaActual--;

      // Guardar la nueva posición del diagnóstico.
      this.guardarProgresoLocal();
    }
  }

  guardarYSalir(): void {

  // Evitar salir mientras se finaliza el diagnóstico.
  if (this.finalizando || !this.diagnostico || this.resultado) {
    return;
  }

  // Guardar el progreso y comprobar el resultado.
  const guardadoExitoso = this.guardarProgresoLocal();

  // Impedir la salida si el navegador no pudo guardar.
  if (!guardadoExitoso) {
    window.alert(
      'No fue posible guardar tu progreso. ' +
      'Permanece en el diagnóstico e inténtalo nuevamente.'
    );
    return;
  }

  // Solicitar confirmación antes de regresar.
  const confirmar = window.confirm(
    'Tu progreso se ha guardado correctamente ' +
    'en este navegador.\n\n' +
    '¿Deseas regresar al dashboard?'
  );

  if (!confirmar) {
    return;
  }

    // Regresar al dashboard del estudiante.
    this.router.navigate(['/dashboard']);
  }

  estaSeleccionada(preguntaId: number, opcionId: number): boolean {
    return this.respuestasSeleccionadas[preguntaId] === opcionId;
  }

  get totalRespondidas(): number {
  if (!this.diagnostico) {
    return 0;
  }

  return this.diagnostico.preguntas.filter(
    pregunta => this.respuestasSeleccionadas[pregunta.id] !== undefined
    ).length;
  }

  get porcentajeProgreso(): number {
    if (!this.diagnostico || this.diagnostico.preguntas.length === 0) {
      return 0;
    }

    return Math.round(
      (this.totalRespondidas / this.diagnostico.preguntas.length) * 100
    );
  }

  get todasRespondidas(): boolean {
    return this.diagnostico !== null &&
      this.totalRespondidas === this.diagnostico.preguntas.length;
  }

  finalizarDiagnostico(): void {

  if (
    !this.diagnostico ||
    !this.todasRespondidas ||
    this.finalizando ||
    this.resultado
  ) {
    return;
  }

  const confirmar = window.confirm(
    '¿Estás seguro de que deseas finalizar el diagnóstico? ' +
    'Una vez enviado, no podrás modificar tus respuestas.'
  );

  if (!confirmar) {
    return;
  }

  this.finalizando = true;
  this.errorFinalizacion = '';

  const respuestas = this.diagnostico.preguntas.map(pregunta => ({
    preguntaId: pregunta.id,
    opcionId: this.respuestasSeleccionadas[pregunta.id]
  }));

  const datos = {
    intentoId: this.diagnostico.intentoId,
    respuestas
  };

  this.diagnosticoService.finalizarDiagnostico(datos).subscribe({
  next: (respuesta) => {

    // El backend confirmó que el diagnóstico finalizó.
    this.resultado = respuesta;

    // Eliminar las respuestas temporales del intento
    // para evitar recuperar un diagnóstico ya finalizado.
    if (this.diagnostico) {

      const clave =
        `eduneko_diagnostico_${this.diagnostico.intentoId}`;

      try {
        localStorage.removeItem(clave);
      } catch (error) {
        console.error(
          'No fue posible limpiar el progreso local:',
          error
        );
      }
    }

    // Restablecer el estado de finalización.
    this.finalizando = false;
  },
    error: (error) => {
      console.error('Error al finalizar diagnóstico:', error);

      this.errorFinalizacion =
        'No fue posible confirmar la finalización del diagnóstico. ' +
        'Verifica el estado del intento antes de volver a enviarlo.';

      this.finalizando = false;
      }
    });
  }

  obtenerNivelTema(totalAciertos: number): string {
    if (totalAciertos === 3) {
      return 'Fortaleza observada';
    }

    if (totalAciertos === 2) {
      return 'En desarrollo';
    }

  return 'Requiere refuerzo';
  }

  obtenerTemasPriorizados() {
  if (!this.resultado) {
    return [];
  }

  return [...this.resultado.resultadosPorTema]
    .sort((a, b) => a.totalAciertos - b.totalAciertos);
  }

  irAlPlanAprendizaje(): void {

  // Evitar continuar si el diagnóstico no está completado.
  if (!this.resultado) {
    return;
  }

  // Navegar a la pantalla del plan de Matemáticas.
    this.router.navigate(['/plan/matematicas']);
  }
}