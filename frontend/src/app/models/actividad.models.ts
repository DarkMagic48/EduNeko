export type TipoActividad =
  | 'OPCION_MULTIPLE'
  | 'RESPUESTA_NUMERICA';

export interface ActividadResponse {
  id: number;
  leccion_id: number;
  pregunta: string;
  tipo: TipoActividad;
  opciones: string[] | null;
  orden: number;
  /**
 * Respuesta previamente registrada por el estudiante.
 * Es null cuando todavía no ha respondido.
 */
respuestaEstudiante?: string | null;

/**
 * Indica si el estudiante ya respondió correctamente
 * esta pregunta en alguno de sus intentos.
 */
respondidaCorrectamente?: boolean;

/**
 * Puntos acumulados por esta pregunta.
 * No representa puntos nuevos por volver a entrar.
 */
puntosObtenidos?: number;
}

export interface EvaluacionActividadResponse {
  actividadId: number;
  esCorrecta: boolean;
  respuestaCorrecta: string;
  explicacion: string | null;
  puntosObtenidos: number;
  puntosYaOtorgados: boolean;
}
