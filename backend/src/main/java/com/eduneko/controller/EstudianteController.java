package com.eduneko.controller;

import java.util.Map;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

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

        /**
         * Obtiene las materias seleccionadas por el estudiante
         * autenticado durante el onboarding.
         *
         * GET /api/estudiante/materias
         *
         * Este endpoint es de solo lectura y se utilizará
         * para mostrar las materias reales en el Dashboard.
         */
        @GetMapping("/materias")
        public ResponseEntity<List<Materia>> obtenerMateriasSeleccionadas(
                Authentication authentication) {

        // Obtener el correo del usuario autenticado.
        String correo = authentication.getName();

        // Consultar las materias registradas en PostgreSQL.
        List<Materia> materias =
                materiaService.obtenerMateriasSeleccionadas(correo);

        // Devolver las materias al frontend en formato JSON.
        return ResponseEntity.ok(materias);
        }

    /**
     * Consulta el estado de onboarding del estudiante autenticado.
     *
     * Este endpoint permite al frontend decidir si debe
     * mostrar la pantalla de bienvenida o el Dashboard.
     *
     * GET /api/estudiante/estado-onboarding
     */
    @GetMapping("/estado-onboarding")
    public ResponseEntity<Map<String, Boolean>> obtenerEstadoOnboarding(
            Authentication authentication) {

        // Obtener el correo del usuario autenticado.
        String correo = authentication.getName();

        // Consultar el estado almacenado en PostgreSQL.
        boolean onboardingCompletado =
                estudianteService.obtenerEstadoOnboarding(correo);

        // Devolver el resultado en formato JSON.
        return ResponseEntity.ok(
                Map.of(
                        "onboardingCompletado",
                        onboardingCompletado
                )
        );
    }

}