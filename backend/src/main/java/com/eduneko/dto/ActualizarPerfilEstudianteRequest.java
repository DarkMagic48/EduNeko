package com.eduneko.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class ActualizarPerfilEstudianteRequest {
    
    @NotNull(message = "La edad es obligatoria")
    @Min(value = 10, message = "La edad mínima es 10 años")
    @Max(value = 18, message = "La edad máxima es 18 años")
    private Short edad;

    @NotNull(message = "El grado escolar es obligatorio")
    @Min(value = 1, message = "El grado escolar debe ser entre 1 y 3")
    @Max(value = 3, message = "El grado escolar debe ser entre 1 y 3")
    private Short gradoEscolar;

    public Short getEdad() {
        return edad;
    }

    public void setEdad(Short edad) {
        this.edad = edad;
    }

    public Short getGradoEscolar() {
        return gradoEscolar;
    }

    public void setGradoEscolar(Short gradoEscolar) {
        this.gradoEscolar = gradoEscolar;
    }
}
