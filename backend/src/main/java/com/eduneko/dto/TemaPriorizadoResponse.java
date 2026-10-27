package com.eduneko.dto;

import java.util.List;

public record TemaPriorizadoResponse(
        String tema,
        Integer totalPreguntas,
        Integer totalAciertos,
        Double porcentajeAciertos,
        String nivelDesempeno,
        Integer prioridad,
        List<ContenidoCurricularResponse> contenidosCurriculares
) {
}