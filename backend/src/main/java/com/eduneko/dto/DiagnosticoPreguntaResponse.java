package com.eduneko.dto;

import java.util.List;

public record DiagnosticoPreguntaResponse(
    Long id,
    String tema,
    String dificultad,
    String enunciado,
    List<DiagnosticoOpcionResponse> opciones
) {
}
