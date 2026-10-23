export interface DiagnosticoOpcionResponse {
  id: number;
  texto: string;
}

export interface DiagnosticoPreguntaResponse {
  id: number;
  tema: string;
  dificultad: 'BASICO' | 'INTERMEDIO' | 'AVANZADO';
  enunciado: string;
  opciones: DiagnosticoOpcionResponse[];
}

export interface DiagnosticoInicioResponse {
  intentoId: number;
  materiaId: number;
  materiaNombre: string;
  gradoEscolar: number;
  estado: string;
  preguntas: DiagnosticoPreguntaResponse[];
}

export interface DiagnosticoRespuestaRequest {
  preguntaId: number;
  opcionId: number;
}

export interface FinalizarDiagnosticoRequest {
  intentoId: number;
  respuestas: DiagnosticoRespuestaRequest[];
}

export interface DiagnosticoResultadoTemaResponse {
  tema: string;
  totalPreguntas: number;
  totalAciertos: number;
  porcentajeAciertos: number;
}

export interface DiagnosticoResultadoResponse {
  intentoId: number;
  materiaId: number;
  materiaNombre: string;
  gradoEscolar: number;
  estado: string;
  totalPreguntas: number;
  totalAciertos: number;
  porcentajeAciertos: number;
  resultadosPorTema: DiagnosticoResultadoTemaResponse[];
}