package com.eduneko.dto;

public class RegistroUsuarioResponse {
    
    private Long id;
    private String nombre;
    private String correo;
    private String mensaje;

    public RegistroUsuarioResponse(

            Long id,
            String nombre,
            String correo,
            String mensaje) {

        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
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

    public String getMensaje() {
        return mensaje;
    }
}
