package com.eduneko.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduneko.dto.ActualizarPerfilEstudianteRequest;
import com.eduneko.entity.Estudiante;
import com.eduneko.service.EstudianteService;

import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/estudiante")
public class EstudianteController {
    
    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    @PutMapping("/perfil")
    public ResponseEntity<Map<String, Object>> actualizarPerfil(
            Authentication authentication,
            @Valid @RequestBody ActualizarPerfilEstudianteRequest request) {

        Estudiante estudiante = estudianteService.actualizarPerfil(
                authentication.getName(),
                request
        );

        return ResponseEntity.ok(
                Map.of(
                        "edad", estudiante.getEdad(),
                        "gradoEscolar", estudiante.getGradoEscolar(),
                        "mensaje", "Perfil académico actualizado correctamente"
                )
        );
    }
}
