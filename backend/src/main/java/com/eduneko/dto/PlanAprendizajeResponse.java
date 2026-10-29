package com.eduneko.dto;

import java.util.List;

public record PlanAprendizajeResponse(
        Long planId,
        Long materiaId,
        String materiaNombre,
        Short gradoEscolar,
        String estado,
        Integer version,
        List<PlanUnidadResponse> unidades
) {    
}
