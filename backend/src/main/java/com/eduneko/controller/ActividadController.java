package com.eduneko.controller;

import com.eduneko.service.ActividadService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/actividades")
public class ActividadController {

    private final ActividadService actividadService;

    public ActividadController(
            ActividadService actividadService) {

        this.actividadService = actividadService;
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
}
