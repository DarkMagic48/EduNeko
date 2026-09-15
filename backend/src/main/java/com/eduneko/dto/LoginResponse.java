package com.eduneko.dto;

public class LoginResponse {
    
    private Long id;
    private String nombre;
    private String correo;
    private String token;
    private String mensaje;

    public LoginResponse (
            Long id,
            String nombre,
            String correo,
            String token,
            String mensaje) {

        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.token = token;
        this.mensaje = mensaje;
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getToken() {
        return token;
    }

    public String getMensaje() {
        return mensaje;
    }
}
