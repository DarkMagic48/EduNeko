package com.eduneko.dto;

import java.util.List;

public record DiagnosticoResultadoResponse(
    Long intentoId,
    Long materiaId,
    String materiaNombre,
    Short gradoEscolar,
    String estado,
    Integer totalPreguntas,
    Integer totalAciertos,
    Double porcentajeAciertos,
    List<DiagnosticoResultadoTemaResponse> resultadosPorTema
) {
}
