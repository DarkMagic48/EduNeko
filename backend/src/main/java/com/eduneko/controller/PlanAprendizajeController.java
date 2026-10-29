
package com.eduneko.controller;

import com.eduneko.service.PlanAprendizajeService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.PostMapping;
import com.eduneko.dto.PlanAprendizajeResponse;

import java.util.Map;

@RestController
@RequestMapping("/api/planes")
public class PlanAprendizajeController {

    private final PlanAprendizajeService planService;

    public PlanAprendizajeController(
            PlanAprendizajeService planService
    ) {
        this.planService = planService;
    }

    @PostMapping("/matematicas/generar")
    public ResponseEntity<Map<String, Object>> generarPlanMatematicas(
            Authentication authentication
    ) {
        Long planId = planService.generarPlanMatematicas(
                authentication.getName()
        );

        return ResponseEntity.ok(
                Map.of(
                        "planId", planId,
                        "mensaje",
                        "Plan de aprendizaje disponible correctamente"
                )
        );
    }

    @GetMapping("/matematicas/actual")
    public ResponseEntity<PlanAprendizajeResponse> obtenerPlanActualMatematicas(
                Authentication authentication
    ) {
    PlanAprendizajeResponse plan =
        planService.obtenerPlanActualMatematicas(
                authentication.getName()
        );

    return ResponseEntity.ok(plan);
   }
 
    @PostMapping("/matematicas/regenerar")
    public ResponseEntity<Map<String, Object>> regenerarPlanMatematicas(
        Authentication authentication
    ) {
    Long planId = planService.regenerarPlanMatematicas(
            authentication.getName()
    );

    return ResponseEntity.ok(Map.of(
            "planId", planId,
            "mensaje", "Plan de aprendizaje regenerado correctamente"
    ));
   }

}