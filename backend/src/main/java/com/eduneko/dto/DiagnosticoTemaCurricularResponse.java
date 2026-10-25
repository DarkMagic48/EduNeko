package com.eduneko.dto;

import java.util.List;

public record DiagnosticoTemaCurricularResponse(
        Integer gradoEscolar,
        String temaDiagnostico,
        List<ContenidoCurricularResponse> contenidosCurriculares
) {   
}
