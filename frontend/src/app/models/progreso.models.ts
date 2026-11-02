/**
 * Representa el progreso real de un estudiante
 * dentro de una lección de EduNeko.
 *
 * Los datos provienen del backend y se calculan
 * a partir de los intentos registrados en PostgreSQL.
 */
export interface ProgresoLeccionResponse {

  // Identificador de la lección.
  leccionId: number;

  // Identificador de la unidad del plan de aprendizaje.
  unidadId: number;

  // Cantidad total de actividades de la lección.
  totalActividades: number;

  // Actividades respondidas correctamente al menos una vez.
  actividadesCorrectas: number;

  // Porcentaje de avance entre 0 y 100.
  progresoPorcentaje: number;

  // Puntos acumulados en esta lección.
  puntosObtenidos: number;

  // Indica si el estudiante completó todas las actividades.
  completada: boolean;
}