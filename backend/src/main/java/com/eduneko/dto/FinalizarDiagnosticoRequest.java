package com.eduneko.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record FinalizarDiagnosticoRequest(

    @NotNull(message = "El identificador del intento es obligatorio")
    Long intentoId,

    @NotEmpty(message = "Debes enviar las respuestas del diagnóstico")
    List<@NotNull @Valid DiagnosticoRespuestaRequest> respuestas

) {
}
