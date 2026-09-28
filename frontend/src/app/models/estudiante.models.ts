export interface ActualizarPerfilEstudianteRequest {
    edad: number;
    gradoEscolar: number;
}

export interface ActualizarPerfilEstudianteResponse {
    edad: number;
    gradoEscolar: number;
    mensaje: string;
}