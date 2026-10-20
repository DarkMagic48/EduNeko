package com.eduneko.dto;

public record DiagnosticoResultadoTemaResponse(
    String tema,
    Integer totalPreguntas,
    Integer totalAciertos,
    Double porcentajeAciertos
) {
}
