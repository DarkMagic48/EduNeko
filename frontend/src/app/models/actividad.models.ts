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
}

export interface EvaluacionActividadResponse {
  actividadId: number;
  esCorrecta: boolean;
  respuestaCorrecta: string;
  explicacion: string | null;
  puntosObtenidos: number;
  puntosYaOtorgados: boolean;
}