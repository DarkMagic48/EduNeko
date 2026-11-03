package com.eduneko.controller;

import com.eduneko.service.ProgresoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/progreso")
public class ProgresoController {

    private final ProgresoService progresoService;

    public ProgresoController(ProgresoService progresoService) {
        this.progresoService = progresoService;
    }

    @GetMapping("/unidades/{unidadId}")
    public ResponseEntity<ProgresoService.ProgresoLeccionResponse>
            obtenerProgresoLeccion(
                    @PathVariable Long unidadId,
                    Authentication authentication) {

        var progreso = progresoService.obtenerProgresoLeccion(
                unidadId,
                authentication.getName()
        );

        return ResponseEntity.ok(progreso);
    }

    @GetMapping("/puntos")
    public ResponseEntity<Map<String, Integer>> obtenerPuntosTotales(
            Authentication authentication) {

        // Obtener los puntos del estudiante autenticado.
        int puntosTotales = progresoService.obtenerPuntosTotales(
                authentication.getName()
        );

        // Devolver una respuesta JSON sencilla para Angular.
        return ResponseEntity.ok(
                Map.of("puntosTotales", puntosTotales)
        );
    }

    @GetMapping("/rachas")
    public ResponseEntity<ProgresoService.RachasResponse> obtenerRachas(
            Authentication authentication) {

        // Obtener las rachas del estudiante autenticado.
        var rachas = progresoService.obtenerRachas(
                authentication.getName()
        );

        // Devolver la información en formato JSON.
        return ResponseEntity.ok(rachas);
    }

    @GetMapping("/logros")
    public ResponseEntity<ProgresoService.LogrosResponse> obtenerLogros(
            Authentication authentication) {

        // Consultar los logros del estudiante autenticado.
        var logros = progresoService.obtenerLogros(
                authentication.getName()
        );

        // Devolver el catálogo de insignias y su estado.
        return ResponseEntity.ok(logros);
    }
}