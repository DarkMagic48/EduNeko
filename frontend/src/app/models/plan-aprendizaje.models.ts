
export interface PlanPdaResponse {
  id: number;
  descripcion: string;
  orden: number;
}

export interface PlanUnidadResponse {
  id: number;
  titulo: string;
  descripcion: string | null;
  objetivo: string;
  temaDiagnostico: string | null;
  orden: number;
  prioridad: number;
  estado: string;
  progresoPorcentaje: number;
  procesosAprendizaje: PlanPdaResponse[];
}

export interface PlanAprendizajeResponse {
  planId: number;
  materiaId: number;
  materiaNombre: string;
  gradoEscolar: number;
  estado: string;
  version: number;
  unidades: PlanUnidadResponse[];
}

export interface GenerarPlanResponse {
  planId: number;
  mensaje: string;
}
