package com.eduneko.controller;

import java.util.Map;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduneko.dto.ActualizarPerfilEstudianteRequest;
import com.eduneko.dto.ActualizarPreferenciasRequest;
import com.eduneko.dto.SeleccionarMateriasRequest;
import com.eduneko.entity.Estudiante;
import com.eduneko.entity.PreferenciaAprendizaje;
import com.eduneko.entity.Materia;
import com.eduneko.service.EstudianteService;
import com.eduneko.service.MateriaService;

import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/estudiante")
public class EstudianteController {
    
    private final EstudianteService estudianteService;
    private final MateriaService materiaService;

    public EstudianteController(EstudianteService estudianteService, MateriaService materiaService) {
        this.estudianteService = estudianteService;
        this.materiaService = materiaService;
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

    @PutMapping("/preferencias")
    public ResponseEntity<Map<String, Object>> actualizarPreferencias(
            Authentication authentication,
            @Valid @RequestBody ActualizarPreferenciasRequest request) {
        
        PreferenciaAprendizaje preferencias = 
                estudianteService.actualizarPreferencias(authentication.getName(), request);
        
        return ResponseEntity.ok(
            Map.of(
                    "nivelExplicacion",
                    preferencias.getNivelExplicacion(),

                    "nivelRetoPreferido",
                    preferencias.getNivelRetoPreferido(),

                    "ritmoEstudio",
                    preferencias.getRitmoEstudio(),

                    "prefiereLecturaEscritura",
                    preferencias.isPrefiereLecturaEscritura(),

                    "prefiereVisual",
                    preferencias.isPrefiereVisual(),

                    "prefiereAuditivo",
                    preferencias.isPrefiereAuditivo(),

                    "prefierePractica",
                    preferencias.isPrefierePractica(),

                    "mensaje",
                    "Preferencias de aprendizaje actualizadas correctamente"
            )
    );
  }
        @PutMapping("/materias")
        public ResponseEntity<List<Materia>> seleccionarMaterias(
                Authentication authentication,
                @Valid @RequestBody SeleccionarMateriasRequest request) {

        List<Materia> materias =
                materiaService.seleccionarMaterias(
                        authentication.getName(),
                        request);

        return ResponseEntity.ok(materias);
   }
}