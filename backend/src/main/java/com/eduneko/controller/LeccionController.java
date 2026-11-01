package com.eduneko.controller;

import com.eduneko.service.LeccionService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/lecciones")
public class LeccionController {

    private final LeccionService leccionService;

    public LeccionController(LeccionService leccionService) {
        this.leccionService = leccionService;
    }

    @GetMapping("/unidades/{unidadId}/contexto")
    public ResponseEntity<Map<String, Object>> obtenerContextoUnidad(
            @PathVariable Long unidadId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                leccionService.obtenerContextoUnidad(
                        unidadId,
                        authentication.getName()
                )
        );
    }

    @GetMapping("/unidades/{unidadId}")
    public ResponseEntity<Map<String, Object>> obtenerLeccion(
            @PathVariable Long unidadId,
            Authentication authentication
    ) {
        return ResponseEntity.ok(
                leccionService.obtenerLeccion(
                        unidadId,
                        authentication.getName()
                )
        );
    }
    
    @PostMapping("/unidades/{unidadId}/generar")
    public ResponseEntity<Map<String, Object>> generarLeccion(
        @PathVariable Long unidadId,
        Authentication authentication) {

       Map<String, Object> leccion =
               leccionService.generarLeccion(
                    unidadId,
                    authentication.getName()
               );

       return ResponseEntity.ok(leccion);
    }
}
