package com.eduneko.dto;

import java.util.List;

public record DiagnosticoInicioResponse(
    Long intentoId,
    Long materiaId,
    String materiaNombre,
    Short gradoEscolar,
    String estado,
    List<DiagnosticoPreguntaResponse> preguntas
) {    
}
