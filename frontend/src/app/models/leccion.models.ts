export type EstadoLeccion =
  | 'BLOQUEADA'
  | 'DISPONIBLE'
  | 'EN_PROGRESO'
  | 'COMPLETADA';

export interface LeccionResponse {
  id: number;
  plan_unidad_id: number;
  titulo: string;
  introduccion: string;
  explicacion: string;
  ejemplo_resuelto: string;
  orden: number;
  estado: EstadoLeccion;
}