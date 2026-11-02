package com.eduneko.controller;

import com.eduneko.service.ActividadService;
import com.eduneko.service.EvaluacionActividadService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/actividades")
public class ActividadController {

    private final ActividadService actividadService;
    private final EvaluacionActividadService evaluacionActividadService;

    public ActividadController(
            ActividadService actividadService,
            EvaluacionActividadService evaluacionActividadService) {

        this.actividadService = actividadService;
        this.evaluacionActividadService = evaluacionActividadService;
    }

    @GetMapping("/unidades/{unidadId}")
    public ResponseEntity<List<Map<String, Object>>> obtenerActividades(
            @PathVariable Long unidadId,
            Authentication authentication) {

        List<Map<String, Object>> actividades =
                actividadService.obtenerActividades(
                        unidadId,
                        authentication.getName()
                );

        return ResponseEntity.ok(actividades);
    }

    @PostMapping("/unidades/{unidadId}/generar")
    public ResponseEntity<List<Map<String, Object>>> generarActividades(
            @PathVariable Long unidadId,
            Authentication authentication) {

        List<Map<String, Object>> actividades =
                actividadService.generarActividadesParaUnidad(
                        unidadId,
                        authentication.getName()
                );

        return ResponseEntity.ok(actividades);
    }
    
    @PostMapping("/unidades/{unidadId}/{actividadId}/responder")
    public ResponseEntity<Map<String, Object>> responderActividad(
                @PathVariable Long unidadId,
                @PathVariable Long actividadId,
                @RequestBody RespuestaActividadRequest request,
                Authentication authentication) {

        Map<String, Object> resultado =
                evaluacionActividadService.evaluar(
                        unidadId,
                        actividadId,
                        request.respuesta(),
                        authentication.getName()
                );

        return ResponseEntity.ok(resultado);
    }

        public record RespuestaActividadRequest(
                String respuesta
        ) {
    }
}
