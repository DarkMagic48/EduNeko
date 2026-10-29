package com.eduneko.dto;

import java.math.BigDecimal;
import java.util.List;

public record PlanUnidadResponse(
        Long id,
        String titulo,
        String descripcion,
        String objetivo,
        String temaDiagnostico,
        Integer orden,
        Integer prioridad,
        String estado,
        BigDecimal progresoPorcentaje,
        List<PlanPdaResponse> procesosAprendizaje
) {
}
