export interface RegistroRequest {
    nombre: string;
    correo: string;
    password: string;
    confirmarPassword: string;
}

export interface RegistroResponse {
    id: number;
    nombre: string;
    correo: string;
    mensaje: string;
}

export interface LoginRequest {
    correo: string;
    password: string;
}

export interface LoginResponse {
    id: number;
    nombre: string;
    correo: string;
    rol: string;
    token: string;
    mensaje: string;
}

export interface UsuarioActualResponse {
    correo: string;
}