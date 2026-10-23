import { Component, OnInit, inject } from '@angular/core';
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
        this.diagnostico = respuesta;
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
    this.respuestasSeleccionadas[preguntaId] = opcionId;
  }

  siguientePregunta(): void {
    if (
      this.diagnostico &&
      this.indicePreguntaActual < this.diagnostico.preguntas.length - 1
    ) {
      this.indicePreguntaActual++;
    }
  }

  preguntaAnterior(): void {
    if (this.indicePreguntaActual > 0) {
      this.indicePreguntaActual--;
    }
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
      this.resultado = respuesta;
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
}