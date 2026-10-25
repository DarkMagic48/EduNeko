package com.eduneko.dto;

import java.util.List;

public record ContenidoCurricularResponse(
        Long id,
        String nombre,
        Integer orden,
        List<ProcesoAprendizajeResponse> procesosAprendizaje
) {
}
