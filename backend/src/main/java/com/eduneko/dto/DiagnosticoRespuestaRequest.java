package com.eduneko.dto;

import jakarta.validation.constraints.NotNull;

public record DiagnosticoRespuestaRequest(

    @NotNull(message = "El identificador de la pregunta es obligatorio")
    Long preguntaId,

    @NotNull(message = "Debes seleccionar una opción de respuesta")
    Long opcionId

) {
}
