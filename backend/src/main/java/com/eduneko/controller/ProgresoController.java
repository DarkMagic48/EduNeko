package com.eduneko.controller;

import com.eduneko.service.ProgresoService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}