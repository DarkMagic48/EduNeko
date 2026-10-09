export interface ActualizarPerfilEstudianteRequest {
    edad: number;
    gradoEscolar: number;
}

export interface ActualizarPerfilEstudianteResponse {
    edad: number;
    gradoEscolar: number;
    mensaje: string;
}

export type NivelExplicacion =
  'BREVE' |
  'EQUILIBRADA' |
  'DETALLADA';

export type NivelRetoPreferido =
  'BAJO' |
  'EQUILIBRADO' |
  'ALTO';

export type RitmoEstudio =
  'TRANQUILO' |
  'NORMAL' |
  'INTENSIVO';

export interface ActualizarPreferenciasRequest {
  nivelExplicacion: NivelExplicacion;
  nivelRetoPreferido: NivelRetoPreferido;
  ritmoEstudio: RitmoEstudio;

  prefiereLecturaEscritura: boolean;
  prefiereVisual: boolean;
  prefiereAuditivo: boolean;
  prefierePractica: boolean;
}

export interface ActualizarPreferenciasResponse {
  nivelExplicacion: NivelExplicacion;
  nivelRetoPreferido: NivelRetoPreferido;
  ritmoEstudio: RitmoEstudio;

  prefiereLecturaEscritura: boolean;
  prefiereVisual: boolean;
  prefiereAuditivo: boolean;
  prefierePractica: boolean;

  mensaje: string;
}

export interface Materia {
  id: number;
  nombre: string;
  activo: boolean;
}

export interface SeleccionarMateriasRequest {
  materiaIds: number[];
}

export type SeleccionarMateriasResponse = Materia[];